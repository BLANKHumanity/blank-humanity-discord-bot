package de.zorro909.blank.BlankDiscordBot.entities.user.fake;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import lombok.Getter;

public class FakeUser {

    @Getter
    private FakeUserType fakeUserType;
    
    private BlankUserService blankUserService;
    
    private BlankUser user;
    
    public FakeUser(BlankUserService blankUserService, FakeUserType fakeUserType, BlankUser user) {
	this.fakeUserType = fakeUserType;
	this.user = user;
	this.blankUserService = blankUserService;
    }

    public int getBalance() {
	return user.getBalance();
    }
    
    public void increaseBalance(int amount) {
	blankUserService.increaseUserBalance(user, amount);
    }
    
    public boolean decreaseBalance(int amount) {
	return blankUserService.decreaseUserBalance(user, amount);
    }
    
    
    
}
