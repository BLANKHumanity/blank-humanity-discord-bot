package de.zorro909.blank.BlankDiscordBot.config.items;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="items")
public class ItemConfiguration {

    @NotNull
    @Valid
    private List<ItemDefinition> definitions;

    public Optional<ItemDefinition> getItemDefinition(int id) {
	return definitions
		.stream()
		.filter((item) -> item.getId() == id)
		.findFirst();
    }

}
