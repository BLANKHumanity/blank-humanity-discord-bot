package com.blank.humanity.discordbot.utils.menu.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.utils.menu.DiscordMenuBuilder;
import com.blank.humanity.discordbot.utils.menu.InteractionRestrictionWrapper;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class ComponentMenuBuilder extends DiscordMenuBuilder<ComponentMenu> {

    private ComponentMenu menu;

    @Autowired
    protected ComponentMenuBuilder(ComponentMenu menu) {
        this.menu = menu;
    }

    public ComponentMenuBuilder link(String url, String label) {
        menu.addLinkButton(Button.link(url, label));
        return this;
    }

    public ComponentMenuBuilder button(Button button, String argument) {
        menu
            .addButton(button,
                wrapAction(ButtonInteractionEvent.class, menu, argument));
        return this;
    }

    public ComponentMenuBuilder button(String label, String argument,
        ButtonStyle style, String id) {
        return button(Button.of(style, id, label), argument);
    }

    public ComponentMenuBuilder button(String label, String argument,
        ButtonStyle style) {
        return button(label, argument, style,
            String.valueOf(argument.hashCode()));
    }

    public ComponentMenuBuilder button(String label, String argument) {
        return button(label, argument, ButtonStyle.PRIMARY);
    }

    public ComponentMenuBuilder button(Emoji emoji, String argument,
        ButtonStyle style, String id) {
        return button(Button.of(style, id, emoji), argument);
    }

    public ComponentMenuBuilder button(Emoji emoji, String argument,
        ButtonStyle style) {
        return button(emoji, argument, style,
            String.valueOf(argument.hashCode()));
    }

    public ComponentMenuBuilder button(Emoji emoji, String argument) {
        return button(emoji, argument);
    }

    @Override
    public ComponentMenuBuilder action(String label,
        String argument) {
        return button(label, argument);
    }

    @Override
    public ComponentMenuBuilder action(Emoji emoji,
        String argument) {
        return button(emoji, argument);
    }

    public SelectMenuBuilder selection(String id) {
        return new SelectMenuBuilder(this, id);
    }

    @Override
    public ComponentMenuBuilder selection(
        Pair<Object, String>[] selectionsAndArguments) {
        List<SelectOption> options = Arrays
            .stream(selectionsAndArguments)
            .map(pair -> {
                if (pair.getLeft() instanceof Emoji emoji) {
                    return SelectOption
                        .of("", pair.getRight())
                        .withEmoji(emoji);
                }
                return SelectOption
                    .of(pair.getLeft().toString(), pair.getRight());
            })
            .toList();

        return selection("autoSelection")
            .addOptions(options)
            .finish();
    }

    public ComponentMenuBuilder selection(SelectMenu selectMenu) {
        menu
            .addSelectMenu(selectMenu,
                wrapAction(SelectMenuInteractionEvent.class, menu,
                    SelectMenuInteractionEvent::getValues));
        return this;
    }

    @Override
    public ComponentMenuBuilder allowedDiscordIds(List<Long> allowedIds) {
        addWrapperFirst(GenericComponentInteractionCreateEvent.class,
            new InteractionRestrictionWrapper<>(allowedIds));
        return this;
    }

    @Override
    public ComponentMenuBuilder singleUse(boolean singleUse) {
        menu.singleUse(singleUse);
        return this;
    }

    @Override
    public ComponentMenuBuilder timeoutTask(Runnable timeoutTask) {
        menu.timeoutTask(timeoutTask);
        return this;
    }

    public ComponentMenuBuilder newRow() {
        menu.newActionRow();
        return this;
    }

    @Override
    public ComponentMenu build() {
        return menu;
    }

    public class SelectMenuBuilder {

        private SelectMenu.Builder builder;

        private ComponentMenuBuilder menu;

        protected SelectMenuBuilder(ComponentMenuBuilder menuBuilder,
            String id) {
            this.menu = menuBuilder;
            this.builder = SelectMenu.create(id);
        }

        public SelectMenuBuilder setId(@Nonnull String customId) {
            builder.setId(customId);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setPlaceholder(@Nullable String placeholder) {
            builder.setPlaceholder(placeholder);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setMinValues(int minValues) {
            builder.setMinValues(minValues);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setMaxValues(int maxValues) {
            builder.setMaxValues(maxValues);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setRequiredRange(int min, int max) {
            this.setRequiredRange(min, max);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setDisabled(boolean disabled) {
            builder.setDisabled(disabled);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOptions(@Nonnull SelectOption... options) {
            builder.addOptions(options);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOptions(
            @Nonnull Collection<? extends SelectOption> options) {
            builder.addOptions(options);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOption(@Nonnull String label,
            @Nonnull String value) {
            builder.addOption(label, value);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOption(@Nonnull String label,
            @Nonnull String value,
            @Nonnull Emoji emoji) {
            builder.addOption(label, value, null, emoji);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOption(@Nonnull String label,
            @Nonnull String value,
            @Nonnull String description) {
            builder.addOption(label, value, description, null);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder addOption(@Nonnull String label,
            @Nonnull String value,
            @Nullable String description, @Nullable Emoji emoji) {
            builder.addOption(label, value, description, emoji);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setDefaultValues(
            @Nonnull Collection<String> values) {
            builder.setDefaultValues(values);
            return this;
        }

        @Nonnull
        public SelectMenuBuilder setDefaultOptions(
            @Nonnull Collection<? extends SelectOption> values) {
            builder.setDefaultOptions(values);
            return this;
        }

        public ComponentMenuBuilder finish() {
            return menu.selection(builder.build());
        }

    }

}
