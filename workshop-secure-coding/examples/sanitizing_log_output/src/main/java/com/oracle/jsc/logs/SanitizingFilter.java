package com.oracle.jsc.logs;

import java.util.regex.Pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public final class SanitizingFilter extends Filter<ILoggingEvent> {

    private static final Pattern CC_NUMBER_PATTERN = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
    private static final Pattern SIXTEEN_DIGITS_PATTERN = Pattern.compile("\\d{16}");

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = event.getFormattedMessage();

        if (CC_NUMBER_PATTERN.matcher(message).find() ) {
            return FilterReply.DENY;
        }
        if (SIXTEEN_DIGITS_PATTERN.matcher(message).find() ) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

}
