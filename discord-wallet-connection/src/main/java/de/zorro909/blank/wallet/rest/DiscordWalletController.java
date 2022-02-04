package de.zorro909.blank.wallet.rest;

import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import de.zorro909.blank.wallet.DiscordWalletService;
import de.zorro909.blank.wallet.entities.DiscordWallet;
import lombok.NonNull;

@Controller
@Path("wallet/discord")
public class DiscordWalletController {

    @Autowired
    private DiscordWalletService discordWalletService;

    @Path("registerWallet")
    @POST
    public Response registerWallet(
	    @NonNull DiscordWalletRegistrationDto discordWalletRegistrationDto) {
	Optional<DiscordWallet> wallet = discordWalletService
		.registerWallet(discordWalletRegistrationDto.getSignature(),
			discordWalletRegistrationDto.getSalt());

	if (wallet.isEmpty()) {
	    return Response.status(Status.BAD_REQUEST).build();
	}
	return Response.ok().build();
    }

}
