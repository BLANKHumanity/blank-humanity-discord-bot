package com.blank.humanity.discordbot.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.config.commands.CommandDefinition;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
@Aspect
@Slf4j
public class CommandArgumentAdvice {

    @Autowired
    private Environment environment;

    @Around("@annotation(argument) && args(commandData, definition)")
    public SlashCommandData interceptCommandData(
        ProceedingJoinPoint proceedingJoinPoint, Argument argument,
        SlashCommandData commandData, CommandDefinition definition)
        throws Throwable {

        if (argument.beforeMethod()) {
            addArgument(commandData, definition, argument, environment);
        }

        commandData = (SlashCommandData) proceedingJoinPoint.proceed();

        if (!argument.beforeMethod()) {
            addArgument(commandData, definition, argument, environment);
        }

        return commandData;
    }

    @Around("@within(argument) && args(commandData, definition)")
    public SlashCommandData interceptCommandDataClassWide(
        ProceedingJoinPoint proceedingJoinPoint, Argument argument,
        SlashCommandData commandData, CommandDefinition definition)
        throws Throwable {
        return interceptCommandData(proceedingJoinPoint, argument, commandData,
            definition);
    }

    @Around("@annotation(arguments) && args(commandData, definition)")
    public SlashCommandData interceptCommandData(
        ProceedingJoinPoint proceedingJoinPoint, Argument.Arguments arguments,
        SlashCommandData commandData, CommandDefinition definition)
        throws Throwable {

        for (int i = 0; i < arguments.value().length; i++) {
            Argument argument = arguments.value()[i];
            if (argument.beforeMethod()) {
                addArgument(commandData, definition, argument, environment);
            }
        }

        commandData = (SlashCommandData) proceedingJoinPoint.proceed();

        for (int i = 0; i < arguments.value().length; i++) {
            Argument argument = arguments.value()[i];
            if (!argument.beforeMethod()) {
                addArgument(commandData, definition, argument, environment);
            }
        }

        return commandData;
    }

    @Around("@within(arguments) && args(commandData, definition)")
    public SlashCommandData interceptCommandDataClassWide(
        ProceedingJoinPoint proceedingJoinPoint, Argument.Arguments arguments,
        SlashCommandData commandData, CommandDefinition definition)
        throws Throwable {
        return interceptCommandData(proceedingJoinPoint, arguments, commandData,
            definition);
    }

    public static void addArgument(SlashCommandData commandData,
        CommandDefinition definition, Argument argument,
        Environment environment) {
        OptionData optionData = new OptionData(argument.type(), argument.name(),
            definition.getOptionDescription(argument.name()),
            argument.required(), argument.autocomplete());

        if (argument.minValue() != Long.MIN_VALUE) {
            optionData.setMinValue(argument.minValue());
        }
        if (argument.maxValue() != Long.MAX_VALUE) {
            optionData.setMaxValue(argument.maxValue());
        }

        if (argument.type().canSupportChoices()) {
            String[] choices = null;
            if (argument.choices().length > 0) {
                choices = argument.choices();
            } else if (!argument.loadChoices().isBlank()) {
                choices = environment
                    .getProperty(argument.loadChoices(), String[].class);
            }

            if (choices != null) {
                Arrays
                    .stream(argument.choices())
                    .forEach(str -> optionData.addChoice(str, str));
            }
        }

        boolean optionAlreadyExists = commandData
            .getOptions()
            .stream()
            .anyMatch(option -> option
                .getName()
                .equalsIgnoreCase(optionData.getName()));

        if (optionAlreadyExists) {
            log
                .warn("Ignoring already existing Argument '"
                    + optionData.getName() + "' for Command '"
                    + commandData.getName() + "'!");
            return;
        }
        commandData.addOptions(optionData);
    }

}
