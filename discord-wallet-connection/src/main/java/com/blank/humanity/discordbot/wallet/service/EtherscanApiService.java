package com.blank.humanity.discordbot.wallet.service;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.blank.humanity.discordbot.wallet.config.EtherscanApiConfig;
import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanRequest;
import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanResponse;
import com.blank.humanity.discordbot.wallet.entities.etherscan.logs.TransactionLogsRequest;
import com.blank.humanity.discordbot.wallet.entities.etherscan.logs.TransactionLogsResponse;
import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTransferEventsRequest;
import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTransferEventsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.MathType;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.local.SynchronizedBucket;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class EtherscanApiService {

    private RestTemplate restTemplate;

    @Autowired
    private EtherscanApiConfig etherscanApiConfig;

    @Autowired
    private ObjectMapper mapper;

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
            .of(retryExecute(NftTokenTransferEventsResponse.class,
                transferEventsRequest))
            .map(Supplier::get)
            .filter(Objects::nonNull);
    }

    public Stream<TransactionLogsResponse> fetchTransactionLogs(
        TransactionLogsRequest request) {
        request.apikey(etherscanApiConfig.getApiKey());

        return Stream
            .of(retryExecute(TransactionLogsResponse.class, request))
            .map(Supplier::get)
            .filter(Objects::nonNull);
    }

    private <T extends EtherscanResponse> Supplier<T> retryExecute(
        Class<T> responseEntity, EtherscanRequest request) {
        return () -> Mono
            .defer(() -> execute(responseEntity, request))
            .retry(3)
            .block();
    }

    private <T extends EtherscanResponse> Mono<T> execute(
        Class<T> responseEntity, EtherscanRequest request) {
        try {
            etherscanApiBucket.consumeUninterruptibly(2);
            ResponseEntity<String> response = restTemplate
                .getForEntity(request.toUrl(), String.class);

            String body = response.getBody();
            if (body != null) {
                return readValue(responseEntity, body);
            }
            return Mono
                .error(new ResponseStatusException(response.getStatusCode()));
        } catch (Exception exception) {
            return Mono.error(exception);
        }
    }

    private <T extends EtherscanResponse> Mono<T> readValue(
        Class<T> responseEntity, String body) throws JsonProcessingException {
        try {
            T actualValue = mapper.readValue(body, responseEntity);
            return Mono.just(actualValue);
        } catch (JsonProcessingException jsonException) {
            log.error(body);
            return Mono.error(jsonException);
        }
    }

}
