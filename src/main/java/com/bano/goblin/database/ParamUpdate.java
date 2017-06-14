package com.bano.goblin.database;

import android.content.ContentValues;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public class ParamUpdate {
    final String selection;
    final String[] selectionArgs;
    final ContentValues contentValues;
    final String tableName;

    public ParamUpdate(String tableName, String selection, String[] selectionArgs, ContentValues contentValues) {
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.contentValues = contentValues;
        this.tableName = tableName;
    }

    public ParamUpdate(String selection, String[] selectionArgs, ContentValues contentValues) {
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.contentValues = contentValues;
        this.tableName = null;
    }
}
