package com.blank.humanity.discordbot.commands;

import java.util.Arrays;
import java.util.Objects;

import org.bouncycastle.util.encoders.Base64;

import net.dv8tion.jda.api.utils.AttachmentOption;

public record FileSendRequest(String name, byte[] data,
    AttachmentOption[] attachmentOptions) {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FileSendRequest request = (FileSendRequest) o;

        return request.name.equals(name) && Arrays.equals(request.data, data)
            && Arrays.equals(request.attachmentOptions, attachmentOptions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + Arrays.hashCode(attachmentOptions);
        return result;
    }

    @Override
    public String toString() {
        return "FileSendRequest{" + "name=" + name + ", data="
            + Base64.toBase64String(data) + ", attachmentOptions="
            + Arrays.toString(attachmentOptions) + "}";
    }

}
