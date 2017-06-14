package com.bano.goblin.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bano.goblin.database.Database;
import com.bano.goblin.util.PreferencesUtils;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public class SyncServerService extends IntentService{

    static final String TAG = "SyncServerService";

    private static final long EXECUTOR_SERVICE_TIMEOUT = 1000 * 15;
    public static final String RECEIVER_FILTER = "com.orfeus.filter.SYNC_SERVER_RECEIVER_FILTER";
    public static final String RESULT_CODE = "syncServerResultCode";

    public static final int NO_INTERNET_CODE = 12;
    public static final int LOADING_DATA_CODE = 13;
    public static final int SAVING_DATA_CODE = 14;
    public static final int SUCCESS_CODE = 15;
    public static final int INTERRUPTED_CODE = 16;
    public static final int SEND_ERROR_CODE = 17;
    public static final int SUCCESS_LOGOUT_CODE = 18;

    private static final String SYNC_ACTION = "SYNC_ACTION";
    private static final String SEND_SYNC_ACTION = "SEND_SYNC_ACTION";
    private static final String LOGOUT_ACTION = "LOGOUT_ACTION";

    @IntDef(value = {NO_INTERNET_CODE, LOADING_DATA_CODE, SAVING_DATA_CODE, SUCCESS_CODE, INTERRUPTED_CODE,
            SEND_ERROR_CODE, SUCCESS_LOGOUT_CODE})
    @Retention(RetentionPolicy.SOURCE)
    @interface SyncCodeFlags {}

    private static LinkedHashSet<SyncParam> mSyncParams;

    public SyncServerService() {
        super(SyncServerService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) return;
        String action = intent.getAction();
        if(action == null)
            throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe SyncServerService");

        if(mSyncParams == null){
            mSyncParams = Database.getSyncParamList();
        }

        if(mSyncParams == null) return;
        switch (action) {
            case LOGOUT_ACTION:
                logout();
                break;
            case SYNC_ACTION:
                sync(new LinkedHashSet<>(mSyncParams));
                break;
            case SEND_SYNC_ACTION:
                SyncServerHelper.sendSyncObjects(this, new LinkedHashSet<>(mSyncParams));
                break;
        }
        mSyncParams = null;
    }

    private void sync(LinkedHashSet<SyncParam> syncParams) {
        if(!NetworkUtil.isConnected(this)){
            SyncServerHelper.scheduleJob(this, SyncServerHelper.SYNC_TAG);
            SyncServerHelper.sendBroadcast(this, NO_INTERNET_CODE);
            return;
        }

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        dispatcher.cancel(SyncServerHelper.SYNC_TAG);

        if(!SyncServerHelper.sendSyncObjects(this, syncParams)){
            SyncServerHelper.sendBroadcast(this, SEND_ERROR_CODE);
            return;
        }

        long init = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(syncParams.size());

        SyncServerHelper.sendBroadcast(this, LOADING_DATA_CODE);
        HashSet<Future<SyncParam>> futureHashSet = SyncServerHelper.getFutureHashSet(executorService, syncParams);
        executorService.shutdown();
        try {
            executorService.awaitTermination(EXECUTOR_SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
            HashSet<SyncParam> syncParamHashSet = SyncServerHelper.getHttpResponseContainerHashSet(futureHashSet);

            SyncServerHelper.sendBroadcast(this, SAVING_DATA_CODE);
//            SyncServerHelper.deleteAllSynchronizedData(paramHashSet);
            SyncServerHelper.insertObjectContainer(syncParamHashSet);
            SyncServerHelper.sendBroadcast(this, SUCCESS_CODE);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException");
            e.printStackTrace();
            SyncServerHelper.sendBroadcast(this, INTERRUPTED_CODE);
        } catch (ExecutionException e) {
            e.printStackTrace();
            SyncServerHelper.sendBroadcast(this, INTERRUPTED_CODE);
        }

        long end = System.currentTimeMillis();
        Log.d(TAG, "sync: " + (end - init));
    }

    private void logout(){
        SyncServerHelper.sendSyncObjects(this, mSyncParams);
        SyncServerHelper.deleteAllSynchronizedData(mSyncParams);
        PreferencesUtils.putBoolean(this, PreferencesUtils.LOGGED_IN, false);
        SyncServerHelper.sendBroadcast(this, SUCCESS_LOGOUT_CODE);
    }

    public static void startSync(@NonNull Context context, @NonNull LinkedHashSet<SyncParam> syncParams){
        mSyncParams = syncParams;
        Intent intent = new Intent(context, SyncServerService.class);
        intent.setAction(SYNC_ACTION);
        context.startService(intent);
    }

    public static void startSync(@NonNull Context context){
        Intent intent = new Intent(context, SyncServerService.class);
        intent.setAction(SYNC_ACTION);
        context.startService(intent);
    }

    public static void startSendSyncObj(@NonNull Context context, @NonNull LinkedHashSet<SyncParam> syncParams){
        mSyncParams = syncParams;
        Intent intent = new Intent(context, SyncServerService.class);
        intent.setAction(SEND_SYNC_ACTION);
        context.startService(intent);
    }

    public static void startSendSyncObj(@NonNull Context context){
        Intent intent = new Intent(context, SyncServerService.class);
        intent.setAction(SEND_SYNC_ACTION);
        context.startService(intent);
    }

    public static void startLogout(@NonNull Context context, @NonNull LinkedHashSet<SyncParam> syncParams){
        mSyncParams = syncParams;
        Intent intent = new Intent(context, SyncServerService.class);
        intent.setAction(LOGOUT_ACTION);
        context.startService(intent);
    }
}
