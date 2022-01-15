package de.zorro909.blank.BlankDiscordBot.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

public class FormattingDataValidator
	implements ConstraintValidator<ValidateFormattingData, FormattingData> {

    private List<FormatDataKey> keysToCheck;

    @Override
    public void initialize(ValidateFormattingData formattingValidator) {
	keysToCheck = Arrays
		.stream(FormatDataKey.values())
		.filter(FormatDataKey::isRequired)
		.toList();
    }

    @Override
    public boolean isValid(FormattingData formattingData,
	    ConstraintValidatorContext cxt) {
	FormatDataKey[] messageTypeSpecificKeys = formattingData
		.messageType()
		.getAvailableDataKeys();
	return Stream
		.concat(Arrays.stream(messageTypeSpecificKeys),
			keysToCheck.stream())
		.filter(key -> formattingData.get(key) == null)
		.map(key -> "FormatData Key '" + key.getKey()
			+ "' is unspecified, even though it is required!")
		.map(cxt::buildConstraintViolationWithTemplate)
		.map(ConstraintViolationBuilder::addConstraintViolation)
		.count() == 0;
    }

}