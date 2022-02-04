package de.zorro909.blank.wallet.command;

import org.springframework.beans.factory.annotation.Autowired;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractHiddenCommand;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.wallet.DiscordWalletService;
import de.zorro909.blank.wallet.config.WalletVerifyConfig;
import de.zorro909.blank.wallet.messages.WalletFormatDataKey;
import de.zorro909.blank.wallet.messages.WalletMessageType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class VerifyCommand extends AbstractHiddenCommand {

    protected VerifyCommand() {
	super("verify");
    }

    @Autowired
    private DiscordWalletService discordWalletService;

    @Autowired
    private WalletVerifyConfig walletVerifyConfig;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = blankUserService.getUser(event);

	String salt = discordWalletService.createWalletSalt(user);

	reply(event, blankUserService
		.createFormattingData(user,
			WalletMessageType.WALLET_VERIFY_DISPLAY_LINK)
		.dataPairing(WalletFormatDataKey.WALLET_VERIFY_LINK,
			walletVerifyConfig.getVerifyBaseUrl() + "?salt=" + salt)
		.build());
    }

}
