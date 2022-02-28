package com.blank.humanity.discordbot.utils.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public abstract class DiscordMenuBuilder<M extends DiscordMenu> {

    public interface DiscordMenuActionWrapper<E extends Event, M extends DiscordMenu> {
        public boolean wrap(E event, M menu, String argument);
    }

    private Map<Class<?>, List<DiscordMenuActionWrapper<?, M>>> wrappers = new HashMap<>();

    public <E extends Event> DiscordMenuBuilder<M> addWrapper(Class<E> event,
        DiscordMenuActionWrapper<E, M> wrapper) {
        wrappers.computeIfAbsent(event, k -> new ArrayList<>()).add(wrapper);
        return this;
    }

    protected <E extends Event> Predicate<E> wrapAction(Class<E> event, M menu,
        String argument) {
        return wrapAction(event, menu, e -> argument);
    }

    @SuppressWarnings("unchecked")
    protected <E extends Event> Predicate<E> wrapAction(Class<E> event, M menu,
        Function<E, String> argument) {
        // Combines all Wrappers into one, aborts as soon as any individual
        // Wrapper returns false
        DiscordMenuActionWrapper<E, M> wrapped = wrappers
            .entrySet()
            .stream()
            .filter(entry -> entry
                .getKey()
                .isAssignableFrom(
                    event))
            .flatMap(entry -> entry
                .getValue()
                .stream())
            .map(wrapper -> (DiscordMenuActionWrapper<E, M>) wrapper)
            .reduce((first, second) -> (ev, internMenu,
                arg) -> first.wrap(ev, internMenu, arg)
                    && second.wrap(ev, internMenu, arg))
            .orElseThrow();
        return e -> wrapped.wrap(e, menu, argument.apply(e));
    }

    public DiscordMenuBuilder<M> action(String labelArgument) {
        return action(labelArgument, labelArgument);
    }

    public abstract DiscordMenuBuilder<M> action(String label, String argument);

    public abstract DiscordMenuBuilder<M> action(Emoji emoji, String argument);

    @SuppressWarnings("unchecked")
    public DiscordMenuBuilder<M> selection(
        List<Pair<Object, String>> selectionsAndArguments) {
        return selection(
            selectionsAndArguments.toArray(i -> new Pair[i]));
    }

    public abstract DiscordMenuBuilder<M> selection(
        Pair<Object, String>[] selectionsAndArguments);

    public abstract M build();

    public abstract DiscordMenuBuilder allowedDiscordIds(List<Long> allowedIds);

    public abstract DiscordMenuBuilder restricted(boolean restricted);

    public abstract DiscordMenuBuilder singleUse(boolean singleUse);

    public abstract DiscordMenuBuilder timeoutTask(Runnable timeoutTask);

}
