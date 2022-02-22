package com.blank.humanity.discordbot.services;

import javax.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.entities.user.UserClaimData;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public interface BlankUserService {

    public BlankUser getUser(long discordId, long guildId);

    public BlankUser getUser(SlashCommandInteraction event);

    public BlankUser getUser(OptionMapping option);

    public BlankUser getUser(Member member);

    @Transactional
    public void migrateUser(long discordId, long guildId, int money);

    @Transactional
    public void increaseUserBalance(long discordId, long guildId, int money);

    @Transactional
    public void increaseUserBalance(BlankUser user, int money);

    @Transactional
    public boolean decreaseUserBalance(long discordId, long guildId, int money);

    @Transactional
    public boolean decreaseUserBalance(BlankUser user, int money);

    public UserClaimData fetchClaimData(BlankUser blankUser,
        ClaimDataType claimDataType);

    public FormattingData createSimpleFormattingData(
        SlashCommandInteraction event,
        MessageType messageType);

    public FormattingData.FormattingDataBuilder createFormattingData(
        BlankUser user, MessageType messageType);

    public FormattingData.FormattingDataBuilder addUserDetailsFormattingData(
        FormattingData.FormattingDataBuilder builder, BlankUser user,
        FormatDataKey userName, FormatDataKey userMention);

    @Transactional
    public FormattingData.FormattingDataBuilder claimReward(BlankUser blankUser,
        ClaimDataType claimType);

    public BlankUser getUser(long guildId, String username,
        String discriminator);

    @Transactional
    public void deleteUser(BlankUser user);

    public Page<BlankUser> listUsers(Sort sortedBy, int page);

    public int getUserListPageSize();

    public String getUsername(BlankUser user);
}
