package com.bano.goblin.http;

import android.support.annotation.NonNull;

import com.bano.goblin.util.DateUtil;

import java.util.Calendar;

/**
 * This class generates and providers callback to auth. This will be pass in header of the HttpRequest
 */

public class Token {

    public final int timeToExpireInSeconds;
    public final String field;
    public final String value;
    public final Calendar initialCalendar;

    public Token(@NonNull String field, @NonNull String value, @NonNull Integer timeToExpireInSeconds, long initialTimeInMillis) {
        this.field = field;
        this.value = value;
        this.timeToExpireInSeconds = timeToExpireInSeconds;
        initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(initialTimeInMillis);
    }

    public boolean isValidToken(){
        return value != null && initialCalendar != null
                && timeToExpireInSeconds > 0
                && DateUtil.getDifferenceInSeconds(Calendar.getInstance(), initialCalendar) <= timeToExpireInSeconds;
    }
}
