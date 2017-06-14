package com.bano.goblin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.bano.goblin.sync.SyncParam;

import java.util.LinkedHashSet;

/**
 *
 * Created by Alexandre on 04/05/2017.
 */

public class Database {

    public static final int DATABASE_VERSION = 17;

    private static DatabaseOpenHelper mOpenHelper;

    public static void initializeDatabase(Context context, @NonNull LinkedHashSet<BaseDao> baseList,
                                          @NonNull LinkedHashSet<SyncParam> syncParamList){
        if(mOpenHelper == null){
            mOpenHelper = new DatabaseOpenHelper(context, baseList, syncParamList);
        }
    }

    public static LinkedHashSet<BaseDao> getBaseList(){
        return mOpenHelper.getBaseList();
    }

    public static LinkedHashSet<SyncParam> getSyncParamList(){
        return mOpenHelper.getSyncParamList();
    }

    public static boolean isAlive(){
        return mOpenHelper != null;
    }

    @WorkerThread
    static SQLiteDatabase getDatabase(){
        return mOpenHelper.getWritableDatabase();
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "Cyclop.db";
        private LinkedHashSet<BaseDao> mBaseList;
        private LinkedHashSet<SyncParam> mSyncParamList;

        DatabaseOpenHelper(Context context, LinkedHashSet<BaseDao> baseList, LinkedHashSet<SyncParam> syncParamList) {
            super(
                    context,
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
            mBaseList = baseList;
            mSyncParamList = syncParamList;
        }

        protected LinkedHashSet<BaseDao> getBaseList(){
            return mBaseList;
        }

        protected LinkedHashSet<SyncParam> getSyncParamList(){
            return mSyncParamList;
        }

        /**
         * Does setup of the database. The system automatically invokes this method when
         * SQLiteDatabase.getWriteableDatabase() or SQLiteDatabase.getReadableDatabase() are
         * invoked and no db instance is available.
         *
         * @param db the database instance in which to create the tables.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates the tables in the backing database for this provider
            for(BaseDao baseDao : mBaseList){
                db.execSQL(baseDao.getCreateTableScript());
            }
        }

        /**
         * Executes the queries to drop all of the tables from the database.
         *
         * @param db A handle to the provider's backing database.
         */
        private void dropTables(SQLiteDatabase db) {
            // If the table doesn't exist, don't throw an error
            for(BaseDao baseDao : mBaseList) {
                db.execSQL("DROP TABLE IF EXISTS " + baseDao.getTableName());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DatabaseOpenHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all the existing data");

            // Drops all the existing tables in the database
            dropTables(db);

            // Invokes the onCreate callback to build new tables
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DatabaseOpenHelper.class.getName(),
                    "Downgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all the existing data");

            // Drops all the existing tables in the database
            dropTables(db);

            // Invokes the onCreate callback to build new tables
            onCreate(db);
        }
    }
}
