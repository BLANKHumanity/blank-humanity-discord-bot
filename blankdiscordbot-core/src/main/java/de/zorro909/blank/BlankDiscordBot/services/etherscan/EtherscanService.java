package de.zorro909.blank.BlankDiscordBot.services.etherscan;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import de.zorro909.blank.BlankDiscordBot.database.NftTokenTradeDao;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanRequest;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs.TransactionLogEntry;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs.TransactionLogsRequest;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs.TransactionLogsResponse;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTrade;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTransferEvent;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTransferEventsRequest;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTransferEventsResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.MathType;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.local.SynchronizedBucket;

@Service
public class EtherscanService {

    private RestTemplate restTemplate;

    @Autowired
    private NftTokenTradeDao nftTokenTradeDao;

    private final String etherscanApiKey = "5RMG9WW8C3GTHEXEW8SSKN3UHMJP76MZTM";

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

    public Stream<NftTokenTrade> retrieveNewerNftTrades(String tokenContract) {
	long lastTradeBlock = nftTokenTradeDao
		.findLastKnownBlock(tokenContract)
		.orElse(0l) + 1;

	return retrieveNftTrades(tokenContract, lastTradeBlock, -1)
		.map(trade -> {
		    ExampleMatcher matcher = ExampleMatcher
			    .matching()
			    .withIgnorePaths("id");
		    if (nftTokenTradeDao.exists(Example.of(trade, matcher))) {
			return null;
		    }
		    return nftTokenTradeDao.saveAndFlush(trade);
		})
		.filter(Objects::nonNull);
    }

    /**
     * Retrieves all NftTrades between blockStart and blockEnd of a particular
     * token contract
     * 
     * @param tokenContract
     * @param blockStart
     * @param blockEnd      Last Block to count, or -1 for latest
     * @return
     */
    public Stream<NftTokenTrade> retrieveNftTrades(String tokenContract,
	    long blockStart, long blockEnd) {
	EtherscanRequest transferEventsRequest = new NftTokenTransferEventsRequest()
		.contractaddress(tokenContract)
		.startblock(blockStart)
		.apikey(etherscanApiKey);

	return Stream
		.of(true)
		.sequential()
		.map((b) -> consumeThenExecute(
			NftTokenTransferEventsResponse.class,
			transferEventsRequest))
		.filter(Objects::nonNull)
		// .peek(System.out::println)
		.filter(response -> response.getStatusCode() == HttpStatus.OK)
		.map(ResponseEntity::getBody)
		.map(NftTokenTransferEventsResponse::getResult)
		.flatMap(List::stream)
		.filter(event -> !event.getFrom().substring(2).matches("0{40}"))
		.flatMap(this::mapTransferEventToTokenTrade)
		.filter(Objects::nonNull)
		.peek(System.out::println);
    }

    private Stream<NftTokenTrade> mapTransferEventToTokenTrade(
	    NftTokenTransferEvent transferEvent) {
	EtherscanRequest logsRequest = new TransactionLogsRequest()
		.address("0x7be8076f4ea4a4ad08075c2508e481d6c946d12b")
		.topic0("0xc4109843e0b7d514e4c093114b863f8e7d8d9a458c372cd51bfe526b588006c9")
		.topic1(padding(transferEvent.getFrom()))
		.topic2(padding(transferEvent.getFrom()))
		.topic1_2_opr("or")
		.fromBlock(transferEvent.getBlockNumber())
		.toBlock(transferEvent.getBlockNumber())
		.apikey(etherscanApiKey);

	System.out.println(logsRequest);
	ResponseEntity<TransactionLogsResponse> response = consumeThenExecute(
		TransactionLogsResponse.class, logsRequest);

	// System.out.println(response.toString());

	if (response.getStatusCode() != HttpStatus.OK) {
	    return null;
	}

	return response
		.getBody()
		.getResult()
		.stream()
		.filter(log -> log
			.getTransactionHash()
			.equalsIgnoreCase(transferEvent.getHash()))
		.filter(doTradersMatch(transferEvent.getFrom().substring(2),
			transferEvent.getTo().substring(2)))
		.map(log -> NftTokenTrade.fromLogEntry(log, transferEvent));
    }

    private String padding(String unpadded) {
	String padding = "0000000000000000000000000000000000000000000000000000000000000000";
	unpadded = unpadded.startsWith("0x") ? unpadded.substring(2) : unpadded;
	return "0x" + padding.substring(unpadded.length()) + unpadded;
    }

    private Predicate<? super TransactionLogEntry> doTradersMatch(String from,
	    String to) {
	return log -> {
	    if (log.getDataPoint(0).matches("0{64}")) {
		// BuyHash empty => Taker is Seller
		if (log.getTopics().get(1).contains(from)) {
		    // Maker is Buyer
		    return log.getTopics().get(2).contains(to);
		}
	    } else {
		// BuyHash exists => Maker is Seller
		if (log.getTopics().get(2).contains(from)) {
		    // Taker is Buyer
		    return log.getTopics().get(1).contains(to);
		}
	    }
	    return false;
	};
    }

    private <T> ResponseEntity<T> consumeThenExecute(Class<T> responseEntity,
	    EtherscanRequest request) {
	etherscanApiBucket.consumeUninterruptibly(2);
	try {
	    return restTemplate.getForEntity(request.toUrl(), responseEntity);
	} catch (Exception e) {
	    System.err.println("Error occured during get: " + request.toUrl());
	    e.printStackTrace();
	    return null;
	}
    }

}
