package de.zorro909.blank.BlankDiscordBot.config.messages;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder
@Data
public class CustomMessageType implements MessageType {

    @Singular(value = "key")
    private List<FormatDataKey> availableDataKeys;

    @NotNull
    private String format;

    public FormatDataKey[] getAvailableDataKeys() {
	return availableDataKeys.toArray(new FormatDataKey[] {});
    }

    @Override
    public String getMessageFormat(Environment env) {
	return format;
    }

}
