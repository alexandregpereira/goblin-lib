package com.bano.goblin.database;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public class Param {
    final String[] projection;
    final String selection;
    final String[] selectionArgs;
    final String sortOrder;

    public Param(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }
}
