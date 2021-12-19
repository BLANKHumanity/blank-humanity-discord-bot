package de.zorro909.blank.BlankDiscordBot.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FormattingDataValidator
	implements ConstraintValidator<ValidateFormattingData, Map> {

    private List<String> keysToCheck;

    @Override
    public void initialize(ValidateFormattingData formattingValidator) {
	keysToCheck = new ArrayList<>();
	for (FormatDataKey key : FormatDataKey.values()) {
	    if (key.isRequired()) {
		keysToCheck.add(key.getKey());
	    }
	}
    }

    @Override
    public boolean isValid(Map map, ConstraintValidatorContext cxt) {
	return keysToCheck.stream().allMatch(map::containsKey);
    }

}
