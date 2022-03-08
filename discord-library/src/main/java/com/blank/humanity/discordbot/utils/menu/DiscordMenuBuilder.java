package com.blank.humanity.discordbot.utils.menu;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public abstract class DiscordMenuBuilder<M extends DiscordMenu> {

    private Map<Class<?>, List<DiscordMenuActionWrapper<?, M>>> wrappers = new LinkedHashMap<>();

    public <E extends Event> DiscordMenuBuilder<M> addWrapperFirst(
        Class<E> event,
        DiscordMenuActionWrapper<E, M> wrapper) {
        wrappers
            .computeIfAbsent(event, k -> new LinkedList<>())
            .add(0, wrapper);
        return this;
    }

    public <E extends Event> DiscordMenuBuilder<M> addWrapper(Class<E> event,
        DiscordMenuActionWrapper<E, M> wrapper) {
        wrappers.computeIfAbsent(event, k -> new LinkedList<>()).add(wrapper);
        return this;
    }

    protected <E extends Event> Predicate<E> wrapAction(Class<E> event, M menu,
        Object argument) {
        return wrapAction(event, menu, e -> argument);
    }

    @SuppressWarnings("unchecked")
    protected <E extends Event> Predicate<E> wrapAction(Class<E> event, M menu,
        Function<E, Object> argument) {
        // Combines all Wrappers into one, aborts as soon as any individual
        // Wrapper returns false
        DiscordMenuActionWrapper<E, M>[] wrapped = wrappers
            .entrySet()
            .stream()
            .filter(entry -> entry
                .getKey()
                .isAssignableFrom(
                    event))
            .sorted(extractKeyFromEntry(sortClassesByNearness(event)))
            .flatMap(entry -> entry
                .getValue()
                .stream())
            .map(wrapper -> (DiscordMenuActionWrapper<E, M>) wrapper)
            .toArray(size -> new DiscordMenuActionWrapper[size]);
        return newEvent -> new WrapperChain<>(wrapped, newEvent, menu)
            .doNext(argument.apply(newEvent));
    }

    private <K, V> Comparator<Entry<K, V>> extractKeyFromEntry(
        Comparator<K> comparator) {
        return (entry1, entry2) -> comparator
            .compare(entry1.getKey(), entry2.getKey());
    }

    private Comparator<Class<?>> sortClassesByNearness(
        Class<?> specificType) {
        Map<Class<?>, Integer> map = new HashMap<>();
        addSuperclassAndInterface(specificType, map, 0);

        return (class1, class2) -> map
            .computeIfAbsent(class1, clazz -> Integer.MAX_VALUE)
            .compareTo(map.computeIfAbsent(class2, clazz -> Integer.MAX_VALUE));
    }

    private void addSuperclassAndInterface(Class<?> clazz,
        Map<Class<?>, Integer> map, int depth) {
        if (clazz != Object.class && clazz != null) {
            Class<?> superClass = clazz.getSuperclass();
            Class<?>[] interfaces = clazz.getInterfaces();

            Stream
                .concat(Stream.of(interfaces), Stream.of(superClass))
                .filter(parentClass -> !map.containsKey(parentClass))
                .forEach(parentClass -> {
                    map.put(parentClass, depth);
                    addSuperclassAndInterface(parentClass, map, depth + 1);
                });
        }
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

    public abstract DiscordMenuBuilder<M> allowedDiscordIds(
        List<Long> allowedIds);

    public abstract DiscordMenuBuilder<M> singleUse(boolean singleUse);

    public abstract DiscordMenuBuilder<M> timeoutTask(Runnable timeoutTask);

}
