package com.bano.goblin.sync;

import android.support.annotation.NonNull;

import com.bano.goblin.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */
class LoadableObjectCallable implements Callable<SyncParam> {

    private final SyncParam mSyncParam;

    LoadableObjectCallable(@NonNull SyncParam syncParam){
        mSyncParam = syncParam;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SyncParam call() {
        String json = HttpService.consume(mSyncParam.readHttpRequest);
        if(json == null) return null;

        Gson gson = new GsonBuilder().registerTypeAdapter(Calendar.class, new DateDeserializer()).create();
        mSyncParam.setHashSet((HashSet) gson.fromJson(json, mSyncParam.typeToken.getType()));

        return mSyncParam;
    }

    private static class DateDeserializer implements JsonDeserializer<Calendar> {

        private SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.API_DATE_FORMAT_KEY, Locale.US);

        @Override
        public Calendar deserialize(JsonElement json, Type typfOfT, JsonDeserializationContext context) {
            String dateAsString = json.getAsString();

            try {
                Date date = formatter.parse(dateAsString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
