package com.blank.humanity.discordbot.services.etherscan;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.blank.humanity.discordbot.config.EtherscanApiConfig;
import com.blank.humanity.discordbot.entities.etherscan.EtherscanRequest;
import com.blank.humanity.discordbot.entities.etherscan.EtherscanResponse;
import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogsRequest;
import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogsResponse;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTransferEventsRequest;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTransferEventsResponse;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.MathType;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.local.SynchronizedBucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EtherscanApiService {

    private RestTemplate restTemplate;

    @Autowired
    private EtherscanApiConfig etherscanApiConfig;

    private BlockingBucket etherscanApiBucket;

    @PostConstruct
    protected void setupApiBucket() {
        Bandwidth callsPerSecond = Bandwidth.simple(5, Duration.ofSeconds(1));
        // Strict Limit of 100.000 Requests per Day (soft limiting at 3800 per
        // hour, slightly lower)
        Bandwidth callsPerHour = Bandwidth.simple(3800, Duration.ofHours(1));

        BucketConfiguration config = BucketConfiguration
            .builder()
            .addLimit(callsPerSecond)
            .addLimit(callsPerHour)
            .build();
        this.etherscanApiBucket = new SynchronizedBucket(config,
            MathType.INTEGER_64_BITS, TimeMeter.SYSTEM_MILLISECONDS);
        this.restTemplate = new RestTemplate();
    }

    public Stream<NftTokenTransferEventsResponse> fetchNftTokenTransferEvents(
        String tokenContract, long blockStart) {
        EtherscanRequest transferEventsRequest = new NftTokenTransferEventsRequest()
            .contractaddress(tokenContract)
            .startblock(blockStart)
            .apikey(etherscanApiConfig.getApiKey());

        return Stream
            .of(consumeThenExecute(NftTokenTransferEventsResponse.class,
                transferEventsRequest))
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .filter(response -> response.getStatusCode() == HttpStatus.OK)
            .map(ResponseEntity::getBody);
    }

    public Stream<TransactionLogsResponse> fetchTransactionLogs(
        TransactionLogsRequest request) {
        request.apikey(etherscanApiConfig.getApiKey());

        return Stream
            .of(consumeThenExecute(TransactionLogsResponse.class, request))
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .filter(response -> response.getStatusCode() == HttpStatus.OK)
            .map(ResponseEntity::getBody);
    }

    private <T extends EtherscanResponse> Supplier<ResponseEntity<T>> consumeThenExecute(
        Class<T> responseEntity,
        EtherscanRequest request) {
        return () -> {
            ResponseEntity<T> response = null;
            int tries = 3;
            while (tries > 0) {
                tries--;
                etherscanApiBucket.consumeUninterruptibly(2);
                try {
                    response = restTemplate
                        .getForEntity(request.toUrl(), responseEntity);
                    T body = response.getBody();
                    if (body != null && !body.getStatus().equals("0")) {
                        break;
                    }
                } catch (Exception exception) {
                    log
                        .error("Error occured during get: " + request.toUrl(),
                            exception);
                }
            }
            return response;
        };
    }

}
