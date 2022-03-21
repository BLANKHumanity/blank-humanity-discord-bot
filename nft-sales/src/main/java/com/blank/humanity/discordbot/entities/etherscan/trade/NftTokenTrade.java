package com.blank.humanity.discordbot.entities.etherscan.trade;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogEntry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = { "fromAddress", "toAddress",
        "nftContract", "tokenId", "price", "transactionHash" }) })
public class NftTokenTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    private String retrieverId;

    @NotNull
    private String fromAddress;

    @NotNull
    private String toAddress;

    @NotNull
    private String tokenName;

    @NotNull
    private String nftContract;

    @NotNull
    private long tokenId;

    @NotNull
    private String price;

    @NotNull
    private String transactionHash;

    @NotNull
    private long block;

    public static NftTokenTrade fromLogEntry(TransactionLogEntry log,
        NftTokenTransferEvent transferEvent) {
        return NftTokenTrade
            .builder()
            .fromAddress(transferEvent.getFrom())
            .toAddress(transferEvent.getTo())
            .tokenName(transferEvent.getTokenName())
            .nftContract(transferEvent.getContractAddress())
            .tokenId(transferEvent.getTokenID())
            .price(log.getDataPoint(2))
            .transactionHash(transferEvent.getHash())
            .block(transferEvent.getBlockNumber())
            .build();
    }

}
