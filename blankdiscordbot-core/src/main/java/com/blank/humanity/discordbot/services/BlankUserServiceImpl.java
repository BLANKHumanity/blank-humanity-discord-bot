package com.blank.humanity.discordbot.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.database.BlankUserDao;
import com.blank.humanity.discordbot.database.UserClaimDataDao;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.entities.user.UserClaimData;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Service
public class BlankUserServiceImpl implements BlankUserService {

    @Autowired
    private BlankUserDao blankUserDao;

    @Autowired
    private UserClaimDataDao userClaimDataDao;

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private JDA jda;

    public BlankUser getUser(long discordId, long guildId) {
	System.out.println("Requesting User: " + discordId);
	return blankUserDao
		.findByDiscordId(discordId)
		.orElseGet(() -> registerUser(discordId, guildId));
    }

    public BlankUser getUser(SlashCommandEvent event) {
	return getUser(event.getUser().getIdLong(),
		event.getGuild().getIdLong());
    }

    private BlankUser registerUser(long discordId, long guildId) {
	System.out.println("Registering New User: " + discordId);
	BlankUser blankUser = new BlankUser();
	blankUser.setDiscordId(discordId);
	blankUser.setGuildId(guildId);
	return blankUserDao.save(blankUser);
    }

    @Transactional
    public void migrateUser(long discordId, long guildId, int money) {
	BlankUser user = getUser(discordId, guildId);
	increaseUserBalance(user, money);
	user.setMigrated(true);
    }

    @Transactional
    public void increaseUserBalance(long discordId, long guildId, int money) {
	increaseUserBalance(getUser(discordId, guildId), money);
    }

    @Transactional
    public void increaseUserBalance(BlankUser user, int money) {
	user.setBalance(user.getBalance() + money);
    }

    @Transactional
    public boolean decreaseUserBalance(long discordId, long guildId,
	    int money) {
	return decreaseUserBalance(getUser(discordId, guildId), money);
    }

    @Transactional
    public boolean decreaseUserBalance(BlankUser user, int money) {
	if (user.getBalance() < money) {
	    throw new RuntimeException("User " + user.getDiscordId()
		    + " does not have enough money (Has: " + user.getBalance()
		    + ", Needs: " + money + ")");
	}
	user.setBalance(user.getBalance() - money);
	return true;
    }

    public UserClaimData fetchClaimData(BlankUser blankUser,
	    ClaimDataType claimDataType) {
	UserClaimData data = blankUser.getClaims().get(claimDataType);
	if (data == null) {
	    data = UserClaimData
		    .builder()
		    .user(blankUser)
		    .type(claimDataType)
		    .build();
	    userClaimDataDao.save(data);
	    blankUser.getClaims().put(claimDataType, data);
	}
	return data;
    }

    public FormattingData createSimpleFormattingData(SlashCommandEvent event,
	    MessageType messageType) {
	return createFormattingData(getUser(event.getUser().getIdLong(),
		event.getGuild().getIdLong()), messageType).build();
    }

    public FormattingData.FormattingDataBuilder createFormattingData(
	    BlankUser user, MessageType messageType) {
	return addUserDetailsFormattingData(FormattingData.builder(), user,
		GenericFormatDataKey.USER, GenericFormatDataKey.USER_MENTION)
			.messageType(messageType)
			.dataPairing(EconomyFormatDataKey.BALANCE,
				user.getBalance());
    }

    public FormattingData.FormattingDataBuilder addUserDetailsFormattingData(
	    FormattingData.FormattingDataBuilder builder, BlankUser user,
	    FormatDataKey userName, FormatDataKey userMention) {
	User discordUser = jda.retrieveUserById(user.getDiscordId()).complete();
	return builder
		.dataPairing(userName, discordUser.getName())
		.dataPairing(userMention, discordUser.getAsMention());
    }

    @Transactional
    public FormattingData.FormattingDataBuilder claimReward(BlankUser blankUser,
	    ClaimDataType claimType) {
	UserClaimData claimData = fetchClaimData(blankUser, claimType);

	long milliSecondsSinceLastClaim = claimData
		.getMilliSecondsSinceLastClaim();

	LocalDateTime claimTimestamp = LocalDateTime.of(2000, 1, 1, 0, 0);

	if (milliSecondsSinceLastClaim >= claimType.getMillisBetweenClaims()) {
	    claimTimestamp = LocalDateTime.now();
	    // Allow claiming 1 hour (or 1/24 of the Delay) before the timer
	    // runs out.
	} else if (milliSecondsSinceLastClaim >= claimType
		.getMillisBetweenClaims() / (23d / 24d)) {
	    // Claim Timestamp set to when user would actually be able to claim
	    claimTimestamp = claimData
		    .getLastClaimTime()
		    .plus(claimData.getMilliSecondsSinceLastClaim(),
			    ChronoUnit.MILLIS);
	} else {
	    return _claimWaitMessage(blankUser, claimType,
		    milliSecondsSinceLastClaim);
	}

	int reward = new Random()
		.nextInt(commandConfig.getMinimumReward(claimType),
			commandConfig.getMaximumReward(claimType) + 1);
	reward *= commandConfig.getRewardMultiplier();

	FormattingData.FormattingDataBuilder builder = createFormattingData(
		blankUser, null);

	if (claimType.isStreaksEnabled()) {
	    if (milliSecondsSinceLastClaim < claimType.getMillisStreakDelay()) {
		if (claimData.getClaimStreak() > 0) {
		    reward *= Math
			    .pow(commandConfig.getStreakMultiplier(),
				    claimData.getClaimStreak());
		    builder
			    .dataPairing(EconomyFormatDataKey.CLAIM_STREAK,
				    claimData.getClaimStreak());
		}
		claimData.setClaimStreak(claimData.getClaimStreak() + 1);
	    } else {
		claimData.setClaimStreak(0);
	    }
	}
	claimData.setLastClaimTime(claimTimestamp);
	increaseUserBalance(blankUser, reward);

	return builder
		.dataPairing(EconomyFormatDataKey.CLAIM_REWARD, reward)
		.success(true);
    }

    private FormattingData.FormattingDataBuilder _claimWaitMessage(
	    BlankUser blankUser, ClaimDataType claimType,
	    long milliSecondsSinceLastClaim) {
	long needWait = claimType.getMillisBetweenClaims()
		- milliSecondsSinceLastClaim;
	long remainingHours = TimeUnit.MILLISECONDS.toHours(needWait);
	long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(needWait) % 60;
	long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(needWait) % 60;

	return createFormattingData(blankUser, null)
		.dataPairing(EconomyFormatDataKey.COOLDOWN_HOURS,
			remainingHours)
		.dataPairing(EconomyFormatDataKey.COOLDOWN_MINUTES,
			remainingMinutes)
		.dataPairing(EconomyFormatDataKey.COOLDOWN_SECONDS,
			remainingSeconds)
		.success(false);
    }

    public BlankUser getUser(long guildId, String username,
	    String discriminator) {
	User user = jda.getUserByTag(username, discriminator);
	if (user == null)
	    return null;

	return getUser(user.getIdLong(), guildId);
    }

    @Transactional
    public void deleteUser(BlankUser user) {
	blankUserDao.delete(user);
    }

    public Page<BlankUser> listUsers(Sort sortedBy, int page) {
	return blankUserDao
		.findAll(PageRequest
			.of(page, commandConfig.getUserListPageSize(),
				sortedBy));
    }

    public int getUserListPageSize() {
	return commandConfig.getUserListPageSize();
    }

    public BlankUser getUser(OptionMapping option) {
	return getUser(option.getAsMember());
    }

    public BlankUser getUser(Member member) {
	return getUser(member.getIdLong(), member.getGuild().getIdLong());
    }

    public String getUsername(BlankUser user) {
	return jda.retrieveUserById(user.getDiscordId()).complete().getName();
    }

}
