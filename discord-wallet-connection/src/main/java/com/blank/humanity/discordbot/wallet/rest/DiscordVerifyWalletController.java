package com.blank.humanity.discordbot.wallet.rest;

import java.util.Optional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.blank.humanity.discordbot.wallet.DiscordWalletService;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;

import lombok.NonNull;

@Controller
@Path("wallet/discord")
public class DiscordVerifyWalletController {

    @Autowired
    private DiscordWalletService discordWalletService;

    @Path("registerVerifiedWallet")
    @POST
    public Response registerVerifiedWallet(
	    @NonNull DiscordVerfiedWalletRegistrationDto discordWalletRegistrationDto) {
	Optional<DiscordVerifiedWallet> wallet = discordWalletService
		.registerVerifiedWallet(discordWalletRegistrationDto.getSignature(),
			discordWalletRegistrationDto.getSalt());

	if (wallet.isEmpty()) {
	    return Response.status(Status.BAD_REQUEST).build();
	}
	return Response.ok().build();
    }

}
