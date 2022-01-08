package de.zorro909.blank.BlankDiscordBot.entities.user.fake;

import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;

public enum FakeUserType {
    DICE_JACKPOT;

    public FakeUser getFakeUser(BlankUserService userService) {
	long id = ordinal();
	return new FakeUser(userService, this, userService.getUser(id, id));
    }

}
