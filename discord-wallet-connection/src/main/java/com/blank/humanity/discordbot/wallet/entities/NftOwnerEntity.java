package com.blank.humanity.discordbot.wallet.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Entity
@Data
@Builder
@Jacksonized
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = { "nftContract", "nftTokenId" }) })
public class NftOwnerEntity {

    @Id
    private long id;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String nftContract;

    @Min(0)
    @Column(nullable = false)
    private long nftTokenId;

    @Min(0)
    @Column(nullable = false)
    private long transferBlock;
    
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String owner;

}
