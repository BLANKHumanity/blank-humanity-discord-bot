package com.blank.humanity.discordbot.services;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.NamedFormatter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private Validator validator;

    @Autowired
    private Environment environment;
    
    /**
     * Takes in {@linkplain FormattingData} and uses it to generate a formatted
     * Message according to the Configuration of the specified
     * {@linkplain MessageType}.<br>
     * Also checks for Validity of the FormattingData, MessageType can specify
     * {@linkplain FormatDataKey}s that need to be filled in. If any are missing
     * a Format Error Message will be returned.
     * 
     * @param formattingData The {@linkplain FormattingData} that is used to
     *                       generate the Message
     * @return A formatted Message
     */
    public String format(FormattingData formattingData) {
        Set<ConstraintViolation<FormattingData>> constraintViolation = validator
            .validate(formattingData);
        if (!constraintViolation.isEmpty()) {
            log
                .error("Format Error\n" + constraintViolation
                    .stream()
                    .map(ConstraintViolation<FormattingData>::getMessage)
                    .collect(Collectors.joining("\n")));
            return "Format Error\n" + constraintViolation
                .stream()
                .map(ConstraintViolation<FormattingData>::getMessage)
                .collect(Collectors.joining("\n"));
        }
        String messageFormat = formattingData
            .messageType()
            .getMessageFormat(environment);
        return NamedFormatter
            .namedFormat(messageFormat, formattingData.getDataPairings());
    }

    /**
     * Takes in {@linkplain FormattingData}s and uses it to generate a formatted
     * Messages according to the Configuration of the specified
     * {@linkplain MessageType}.<br>
     * Also checks for Validity of the FormattingData, MessageType can specify
     * {@linkplain FormatDataKey}s that need to be filled in. If any are missing
     * a Format Error Message will be returned.
     * 
     * @param formattingDatas The {@linkplain FormattingData}s that are used to
     *                       generate the Messages
     * @return All formatted Messages
     */
    public String[] format(FormattingData... formattingDatas) {
        return Arrays
            .stream(formattingDatas)
            .map(this::format)
            .toArray(size -> new String[size]);
    }
}
