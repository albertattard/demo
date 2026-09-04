package com.oracle.jsc.logs;

import java.util.regex.Pattern;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public final class PatternLayoutSanitizingEncoder extends MessageConverter {

    private static final Pattern CC_NUMBER_PATTERN = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
    private static final Pattern SIXTEEN_DIGITS_PATTERN = Pattern.compile("\\d{16}");

    @Override
    public String convert(ILoggingEvent event) {
        return sanitize(super.convert(event));
    }
    
    private String sanitize(String msg) {
        String out = msg;
        if (CC_NUMBER_PATTERN.matcher(out).find()) {
            out = CC_NUMBER_PATTERN.matcher(msg).replaceAll("xxxx-xxxx-xxxx-xxxx");
        }

        if (SIXTEEN_DIGITS_PATTERN.matcher(out).find()) {
            out = SIXTEEN_DIGITS_PATTERN.matcher(out).replaceAll("xxxxxxxxxxxxxxxx");
        }

        return out;
    }
}
