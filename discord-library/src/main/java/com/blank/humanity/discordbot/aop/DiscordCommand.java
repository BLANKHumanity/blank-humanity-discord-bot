package com.blank.humanity.discordbot.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Component
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordCommand {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

    boolean hidden() default false;

}
