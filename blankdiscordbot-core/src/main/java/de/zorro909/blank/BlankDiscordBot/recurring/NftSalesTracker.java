package de.zorro909.blank.BlankDiscordBot.recurring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import de.zorro909.blank.BlankDiscordBot.config.NftSalesTrackerConfig;
import de.zorro909.blank.BlankDiscordBot.config.commands.CommandConfig;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTrade;
import de.zorro909.blank.BlankDiscordBot.services.etherscan.EtherscanService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

@Component
public class NftSalesTracker {

    @Autowired
    private JDA jda;

    @Autowired
    private NftSalesTrackerConfig salesTrackerConfig;

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private EtherscanService etherscanService;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void checkNftSales() {
	salesTrackerConfig
		.getContractWatchList()
		.stream()
		.flatMap(etherscanService::retrieveNewerNftTrades)
		.forEach(this::sendNftTradeNotification);
    }

    public void sendNftTradeNotification(NftTokenTrade trade) {
	EmbedBuilder builder = new EmbedBuilder();

	BigDecimal price = new BigDecimal(new BigInteger(trade.getPrice(), 16))
		.divide(BigDecimal.valueOf(10).pow(18));

	byte[] imageData = fetchImage(trade.getTokenId());

	TextChannel channel = jda
		.getGuildById(commandConfig.getGuildId())
		.getTextChannelById(salesTrackerConfig.getSalesChannel());

	builder
		.setTitle(trade.getTokenName() + " Sold -- " + price.toString()
			+ "ETH", "https://etherscan.io/tx/" + trade.getTransactionHash())
		.setDescription(trade.getTokenName() + " " + trade.getTokenId())
		.setImage("attachment://nft_" + trade.getId() + "_"
			+ trade.getTokenId() + ".png")
		.addField("Amount", price.toString() + "ETH", true)
		.addField("From",
			"[" + trade.getFromAddress() + "](https://opensea.io/"
				+ trade.getFromAddress() + ")",
			true)
		.addField("To", "[" + trade.getToAddress()
			+ "](https://opensea.io/" + trade.getToAddress() + ")",
			true);
	MessageEmbed embed = builder.build();

	channel
		.sendMessageEmbeds(embed)
		.addFile(imageData,
			"nft_" + trade.getId() + "_" + trade.getTokenId()
				+ ".png")
		.queue();
    }

    public byte[] fetchImage(long tokenId) {
	RestTemplate rest = new RestTemplate();
	ResponseEntity<IPFSMetadata> metadata;
	try {
	    metadata = rest
		    .getForEntity(
			    "https://ipfs.io/ipfs/QmfHWVaX3NDvamvVQKH5wYWSL2w8tMSshvhngwpKSMduNp/"
				    + tokenId,
			    IPFSMetadata.class);
	    System.out.println(metadata);
	} catch (Exception e) {
	    return new byte[0];
	}

	if (metadata.getStatusCode() != HttpStatus.OK) {
	    return new byte[0];
	}

	String cid = metadata
		.getBody()
		.getImage()
		.substring("ipfs://".length());

	return rest.getForObject("https://ipfs.io/ipfs/" + cid, byte[].class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    protected static class IPFSMetadata {

	private String image;

    }

}
