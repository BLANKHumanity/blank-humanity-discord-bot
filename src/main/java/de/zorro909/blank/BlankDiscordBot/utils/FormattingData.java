package de.zorro909.blank.BlankDiscordBot.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Delegate;

@Data
@Builder
public class FormattingData {

    public static class FormattingDataBuilder {

	Map<String, Object> dataPairings = new HashMap<String, Object>();

	public FormattingDataBuilder dataPairing(FormatDataKey key,
		Object value) {
	    this.dataPairings.put(key.getKey(), value);
	    return this;
	}

    }

    @ValidateFormattingData
    @Delegate
    private Map<String, Object> dataPairings;

    @Default
    private boolean success = false;

    public void put(FormatDataKey key, Object value) {
	dataPairings.put(key.getKey(), value);
    }

    public Object get(FormatDataKey key) {
	return dataPairings.get(key.getKey());
    }

}
