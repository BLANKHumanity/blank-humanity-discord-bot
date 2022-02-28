package com.blank.humanity.discordbot.utils;

import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.config.messages.MessageType;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

@Data
@Builder
@ValidateFormattingData(message = "Formatting Data is invalid!")
@Accessors(fluent = true)
public class FormattingData {

    @NotNull
    private MessageType messageType;

    @Delegate
    @Singular
    private Map<FormatDataKey, Object> dataPairings;

    @Default
    private boolean success = false;

    public Map<String, Object> getDataPairings() {
	return dataPairings
		.entrySet()
		.stream()
		.collect(Collectors
			.toMap(entry -> entry.getKey().getKey(),
				Map.Entry::getValue));
    }

}
