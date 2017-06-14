package com.bano.goblin.database;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public class Field {
    public static final String TEXT_TYPE = "TEXT";
    public static final String INTEGER_PRIMARY_KEY_TYPE = "INTEGER PRIMARY KEY";
    public static final String TEXT_PRIMARY_KEY_TYPE = "TEXT PRIMARY KEY";
    public static final String INTEGER_TYPE = "INTEGER";
    public static final String REAL_TYPE = "REAL";

    @StringDef(value = {TEXT_TYPE, INTEGER_PRIMARY_KEY_TYPE,
            TEXT_PRIMARY_KEY_TYPE, INTEGER_TYPE, REAL_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TypeFlags {}

    final String fieldName;
    final String type;

    public Field(@NonNull String fieldName, @TypeFlags String type) {
        this.fieldName = fieldName;
        this.type = type;
    }
}
