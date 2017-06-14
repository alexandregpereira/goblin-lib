package com.bano.goblin.database;

import android.content.ContentValues;

/**
 * 
 * Created by Alexandre on 12/05/2017.
 */

public class BulkParam {
    
    final ContentValues contentValues;
    final String idFieldName;
    final String idServerFieldName;

    public BulkParam(ContentValues contentValues, String idFieldName, String idServerFieldName) {
        this.contentValues = contentValues;
        this.idFieldName = idFieldName;
        this.idServerFieldName = idServerFieldName;
    }
}
