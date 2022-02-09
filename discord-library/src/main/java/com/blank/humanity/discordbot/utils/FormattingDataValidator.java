package com.blank.humanity.discordbot.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

public class FormattingDataValidator
	implements ConstraintValidator<ValidateFormattingData, FormattingData> {

    @Override
    public boolean isValid(FormattingData formattingData,
	    ConstraintValidatorContext cxt) {
	FormatDataKey[] messageTypeSpecificKeys = formattingData
		.messageType()
		.getAvailableDataKeys();
	return Arrays
		.stream(messageTypeSpecificKeys)
		.filter(key -> formattingData.get(key) == null)
		.map(key -> "FormatData Key '" + key.getKey()
			+ "' is unspecified, even though it is required!")
		.map(cxt::buildConstraintViolationWithTemplate)
		.map(ConstraintViolationBuilder::addConstraintViolation)
		.count() == 0;
    }

}
