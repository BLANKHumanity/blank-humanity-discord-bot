package com.blank.humanity.discordbot.entities.user.fake;

import com.blank.humanity.discordbot.services.BlankUserService;

public enum FakeUserType {
    DICE_JACKPOT;

    public FakeUser getFakeUser(BlankUserService userService) {
	long id = ordinal();
	return new FakeUser(userService, this, userService.getUser(id, id));
    }

}
