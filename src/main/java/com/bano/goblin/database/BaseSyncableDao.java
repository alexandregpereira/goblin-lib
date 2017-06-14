package com.bano.goblin.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.WorkerThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 *
 * Created by Alexandre on 04/05/2017.
 */

public abstract class BaseSyncableDao<T extends ModelSyncable> extends BaseDao<T> {

    public abstract ContentValues getSyncContentValues(T obj);
    public abstract ContentValues getSyncContentValuesAfterSync(T obj);
    public abstract String getIdServerFieldName();
    protected abstract String getSyncColumnName();

    @WorkerThread
    public HashSet<T> getSetBySyncStatus(){
        if(getSyncColumnName() == null) return null;

        Param param = new Param(null,
                getSyncColumnName() + " = " + ModelSyncable.SYNC_PENDENT_STATUS,
                null, null
                );
        return getSet(param);
    }

    @WorkerThread
    public static int bulkInsertOrUpdate(HashMap<String, HashSet<BulkParam>> tableBulkParamMap){
        SQLiteDatabase db =Database.getDatabase();

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (Map.Entry<String, HashSet<BulkParam>> entry : tableBulkParamMap.entrySet()) {
                String tableName = entry.getKey();
                for(BulkParam bulkParam : entry.getValue()) {
                    String idServer = bulkParam.contentValues.getAsString(bulkParam.idServerFieldName);
                    Cursor c = Database.getDatabase().query(
                            tableName,
                            new String[]{bulkParam.idServerFieldName, bulkParam.idFieldName},
                            bulkParam.idServerFieldName + " = ?",
                            new String[]{idServer},
                            null, null, null
                    );
                    if(c == null) continue;

                    if(!c.moveToFirst()){
                        c.close();
                        long _id = db.insert(tableName, null, bulkParam.contentValues);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    else {
                        String id = c.getString(c.getColumnIndex(bulkParam.idFieldName));
                        c.close();
                        rowsInserted += db.update(tableName, bulkParam.contentValues,
                                bulkParam.idFieldName + " = ?",
                                new String[]{id});
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return rowsInserted;
    }

    @WorkerThread
    public boolean updateAfterSync(HashSet<T> tHashSet) {
        LinkedHashSet<ParamUpdate> paramUpdates = new LinkedHashSet<>();
        for(T t : tHashSet){
            paramUpdates.add(new ParamUpdate(getTableName(), getIdFieldName() + " = ?", new String[]{t.id},
                    getSyncContentValuesAfterSync(t)));
        }

        return BaseDao.bulkUpdate(paramUpdates) > tHashSet.size();
    }

    @WorkerThread
    public int deleteAllSyncRealized(){
        String syncColumnName = getSyncColumnName();
        if(syncColumnName == null) return 0;
        return Database.getDatabase().delete(getTableName(),
                syncColumnName + " != " + ModelSyncable.SYNC_PENDENT_STATUS + " or " +
                        syncColumnName + " IS NULL"
                , null);
    }
}
