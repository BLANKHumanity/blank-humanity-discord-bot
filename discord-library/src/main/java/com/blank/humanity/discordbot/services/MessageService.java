package com.blank.humanity.discordbot.services;

import com.blank.humanity.discordbot.utils.FormattingData;

public interface MessageService {

    public String format(FormattingData formattingData);

    public String[] format(FormattingData... formattingDatas);

}
