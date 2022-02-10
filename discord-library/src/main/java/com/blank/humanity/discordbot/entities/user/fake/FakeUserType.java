package com.blank.humanity.discordbot.entities.user.fake;

import com.blank.humanity.discordbot.services.BlankUserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FakeUserType {
    DICE_JACKPOT("`Dice Jackpot`");

    @Getter
    private String displayName;

    public FakeUser getFakeUser(BlankUserService userService) {
	long id = ordinal();
	return new FakeUser(userService, this, userService.getUser(id, id));
    }

}
