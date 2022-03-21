package com.blank.humanity.discordbot.commands;

import net.dv8tion.jda.api.utils.AttachmentOption;

public record FileSendRequest(String name, byte[] data,
    AttachmentOption[] attachmentOptions) {
}
