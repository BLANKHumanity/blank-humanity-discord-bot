package de.zorro909.blank.BlankDiscordBot.services;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.zorro909.blank.BlankDiscordBot.config.CommandConfig;
import de.zorro909.blank.BlankDiscordBot.database.BlankUserDao;
import de.zorro909.blank.BlankDiscordBot.database.UserClaimDataDao;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.ClaimDataType;
import de.zorro909.blank.BlankDiscordBot.entities.UserClaimData;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@Service
public class BlankUserService {

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

    public FormattingData createSimpleFormattingData(SlashCommandEvent event) {
	return createFormattingData(getUser(event.getUser().getIdLong(),
		event.getGuild().getIdLong())).build();
    }

    public FormattingData.FormattingDataBuilder createFormattingData(
	    BlankUser user) {
	User discordUser = jda.retrieveUserById(user.getDiscordId()).complete();
	return FormattingData
		.builder()
		.dataPairing(FormatDataKey.USER, discordUser.getName())
		.dataPairing(FormatDataKey.USER_MENTION,
			discordUser.getAsMention());
    }

    @Transactional
    public FormattingData.FormattingDataBuilder claimReward(BlankUser blankUser,
	    ClaimDataType claimType, Long millisBetweenClaims,
	    Long millisStreakDelay) {
	UserClaimData claimData = fetchClaimData(blankUser, claimType);

	long milliSecondsSinceLastClaim = claimData
		.getMilliSecondsSinceLastClaim();
	if (milliSecondsSinceLastClaim >= millisBetweenClaims) {
	    int reward = new Random()
		    .nextInt(commandConfig.getMinimumReward(claimType),
			    commandConfig.getMaximumReward(claimType) + 1);
	    reward *= commandConfig.getRewardMultiplier();
	    increaseUserBalance(blankUser, reward);

	    FormattingData.FormattingDataBuilder builder = createFormattingData(
		    blankUser);

	    if (milliSecondsSinceLastClaim < millisStreakDelay) {
		if (claimData.getClaimStreak() > 0) {
		    reward *= Math
			    .pow(commandConfig.getStreakMultiplier(),
				    claimData.getClaimStreak());
		    builder
			    .dataPairing(FormatDataKey.CLAIM_STREAK,
				    claimData.getClaimStreak());
		}
		claimData.setClaimStreak(claimData.getClaimStreak() + 1);
	    } else {
		claimData.setClaimStreak(0);
	    }
	    claimData.setLastClaimTime(LocalDateTime.now());

	    return builder
		    .dataPairing(FormatDataKey.CLAIM_REWARD, reward)
		    .success(true);
	} else {
	    long needWait = millisBetweenClaims - milliSecondsSinceLastClaim;
	    long remainingHours = TimeUnit.MILLISECONDS.toHours(needWait);
	    long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(needWait)
		    % 60;
	    long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(needWait)
		    % 60;

	    return createFormattingData(blankUser)
		    .dataPairing(FormatDataKey.CLAIM_HOURS, remainingHours)
		    .dataPairing(FormatDataKey.CLAIM_MINUTES, remainingMinutes)
		    .dataPairing(FormatDataKey.CLAIM_SECONDS, remainingSeconds)
		    .success(false);
	}
    }

    public BlankUser getUser(long guildId, String username,
	    String discriminator) {
	User user = jda.getUserByTag(username, discriminator);
	if (user == null)
	    return null;

	return getUser(user.getIdLong(), guildId);
    }

    public void deleteUser(BlankUser user) {
	blankUserDao.delete(user);
    }

}
