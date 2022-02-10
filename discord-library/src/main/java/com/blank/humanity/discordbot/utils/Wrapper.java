package com.blank.humanity.discordbot.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.transaction.support.TransactionCallback;

public class Wrapper {
    
    private Wrapper() {
    }

    public static <T> Runnable wrap(Consumer<T> function, T input) {
	return () -> function.accept(input);
    }

    public static <T, R> Supplier<R> wrap(Function<T, R> function, T input) {
	return () -> function.apply(input);
    }

    public static <T, T2> Consumer<T2> wrap(BiConsumer<T, T2> function,
	    T input) {
	return t2 -> function.accept(input, t2);
    }

    public static <T, T2> Consumer<T> wrapSecond(BiConsumer<T, T2> function,
	    T2 input) {
	return t -> function.accept(t, input);
    }

    public static <T, T2, R> Function<T2, R> wrap(BiFunction<T, T2, R> function,
	    T input) {
	return t2 -> function.apply(input, t2);
    }

    public static <T, T2, R> Function<T, R> wrapSecond(
	    BiFunction<T, T2, R> function, T2 input) {
	return t -> function.apply(t, input);
    }

    public static <T, T2, R> Supplier<R> wrap(BiFunction<T, T2, R> function,
	    T input, T2 input2) {
	return () -> function.apply(input, input2);
    }
    
    public static <T, R> Function<T, R> supplyOut(Consumer<T> function,
	    R returnValue) {
	return t -> {
	    function.accept(t);
	    return returnValue;
	};
    }

    public static <R> Supplier<R> supplyOut(Runnable function, R event) {
	return () -> {
	    function.run();
	    return event;
	};
    }

    public static <T, R> Function<T, R> hideIn(Supplier<R> function) {
	return r -> function.get();
    }
    
    public static <T> Consumer<T> hideIn(Runnable function) {
	return r -> function.run();
    }
    
    public static <T, R> TransactionCallback<R> transactionCallback(Supplier<R> function) {
	return r -> function.get();
    }

}
