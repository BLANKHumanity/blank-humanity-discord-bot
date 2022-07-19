package com.blank.humanity.discordbot.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.dv8tion.jda.api.interactions.commands.OptionType;

@Inherited
@Repeatable(Argument.Arguments.class)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

    String name();

    OptionType type() default OptionType.STRING;

    boolean required() default true;

    boolean autocomplete() default false;

    boolean beforeMethod() default true;

    long minValue() default Long.MIN_VALUE;

    long maxValue() default Long.MAX_VALUE;

    String[] choices() default {};
    
    /**
     * Loads all choices from a config list
     */
    String loadChoices() default "";

    @Inherited
    @Target({ ElementType.METHOD,
        ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Arguments {

        Argument[] value();

    }

}
