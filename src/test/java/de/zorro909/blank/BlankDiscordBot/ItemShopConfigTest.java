package de.zorro909.blank.BlankDiscordBot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemShopConfig;

@SpringBootTest
public class ItemShopConfigTest {

    @Autowired
    private ItemShopConfig itemShopConfig;

    @Test
    public void whenFactoryProvidedThenYamlPropertiesInjected() {
        assertThat(itemShopConfig.getItemsPerPage()).isEqualTo(8);
    }
}