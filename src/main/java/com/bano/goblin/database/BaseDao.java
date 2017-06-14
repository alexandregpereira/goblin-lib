package com.bano.goblin.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * Created by Alexandre on 04/05/2017.
 */

public abstract class BaseDao<T extends Model> {

    public static final Param ALL = new Param(null, null, null, null);

    private Field[] mFields;

    protected abstract T buildObj(Cursor c);
    protected abstract ContentValues getContentValues(T obj);
    public abstract String getTableName();
    public abstract String getIdFieldName();
    protected abstract SQLiteQueryBuilder getQueryBuilder();
    protected abstract Field[] createFields();

    @WorkerThread
    public ArrayList<T> getList(Param param){
        Cursor c = query(param);
        return cursorToList(c);
    }

    @WorkerThread
    public HashSet<T> getSet(Param param){
        Cursor c = query(param);
        Set<T> set = new HashSet<>();
        return (HashSet<T>) cursorToSet(c, set);
    }

    @WorkerThread
    public int getCount(Param param){
        Param paramCount = new Param(new String[]{"count(*)"}, param.selection, param.selectionArgs, null);
        Cursor c = query(paramCount);
        if(c == null) return 0;
        int count = 0;
        if(c.moveToFirst()){
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    @WorkerThread
    private Cursor query(Param param){
        SQLiteQueryBuilder sqLiteQueryBuilder = getQueryBuilder();
        Cursor c;
        if(sqLiteQueryBuilder != null){
            c = sqLiteQueryBuilder.query(
                    Database.getDatabase(),
                    param.projection,
                    param.selection,
                    param.selectionArgs,
                    null, null,
                    param.sortOrder
            );
        }
        else {
            c = Database.getDatabase().query(
                    getTableName(),
                    param.projection,
                    param.selection,
                    param.selectionArgs,
                    null, null,
                    param.sortOrder
            );
        }

        return c;
    }

    @WorkerThread
    public boolean insert(T t){
        long id = Database.getDatabase().insert(getTableName(), null, getContentValues(t));
        return id != -1;
    }

    @WorkerThread
    protected static int bulkUpdate(LinkedHashSet<ParamUpdate> params){
        SQLiteDatabase db =Database.getDatabase();

        db.beginTransaction();
        int rowsUpdated = 0;
        try {
            for(ParamUpdate param : params) {
                ContentValues contentValues = param.contentValues;
                rowsUpdated += db.update(param.tableName, contentValues,
                        param.selection,
                        param.selectionArgs);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return rowsUpdated;
    }

    @WorkerThread
    public boolean update(T t){
        long rows = Database.getDatabase().update(getTableName(), getContentValues(t),
                getIdFieldName() + " = ?", new String[]{t.id});
        return rows > 0;
    }

    @WorkerThread
    protected boolean update(ParamUpdate param){
        long rows = Database.getDatabase().update(getTableName(), param.contentValues,
                param.selection, param.selectionArgs);
        return rows > 0;
    }

    @WorkerThread
    public boolean delete(T t){
        long rows = Database.getDatabase().delete(getTableName(),
                getIdFieldName() + " = ?", new String[]{t.id});
        return rows > 0;
    }

    @WorkerThread
    public boolean delete(ParamUpdate param){
        long rows = Database.getDatabase().delete(getTableName(),
                param.selection, param.selectionArgs);
        return rows > 0;
    }

    private ArrayList<T> cursorToList(Cursor c){
        ArrayList<T> tList = new ArrayList<>();

        if(c != null) {
            if (c.moveToFirst()) {
                do{
                    T t = buildObj(c);
                    tList.add(t);
                }while (c.moveToNext());
            }

            c.close();
        }

        return tList;
    }

    private T cursorToObj(Cursor c){
        if(c != null) {
            if (c.moveToFirst()) {
                return buildObj(c);
            }
            c.close();
        }

        return null;
    }

    protected Set<T> cursorToSet(Cursor c, Set<T> tList){
        if(c != null) {
            if (c.moveToFirst()) {
                do{
                    T t = buildObj(c);
                    tList.add(t);
                }while (c.moveToNext());
            }

            c.close();
        }

        return tList;
    }

    private Field[] getFields() {
        if(mFields == null) mFields = createFields();
        return mFields;
    }

    String getCreateTableScript(){
        String createScript = "CREATE TABLE " +
                getTableName() + " ( ";
        int i = 0;
        for(Field field : getFields()){
            String separator = ", ";
            if(i == getFields().length - 1) separator = "";
            createScript += field.fieldName + " " + field.type + separator;
            ++i;
        }

        return createScript + ")";
    }

    public T get(String id) {
        Cursor c= Database.getDatabase().query(
                getTableName(), null,
                getIdFieldName() + " = ?",
                new String[]{id}, null, null, null
        );
        return cursorToObj(c);
    }

    public interface OnDataLoadListListener<T>{
        void onLoaded(ArrayList<T> list);
    }

    public interface OnDataLoadSetListener<T>{
        void onLoaded(HashSet<T> list);
    }

    public interface OnDataLoadCountListener{
        void onLoaded(int count);
    }

    public interface OnDataLoadListener<T>{
        void onLoaded(T obj);
    }

    public interface OnDataSaveListener<T>{
        void onSaved(T obj);
    }

    public interface OnDataDeleteListener<T>{
        void onDeleted(T obj, boolean deleted);
    }
}
