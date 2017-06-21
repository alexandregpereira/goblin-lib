package com.bano.goblin.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bano.goblin.database.BaseSyncableDao;
import com.bano.goblin.database.BulkParam;
import com.bano.goblin.database.ModelSyncable;
import com.bano.goblin.http.HttpRequest;
import com.bano.goblin.http.HttpService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public final class SyncServerHelper {

    static final String SEND_SYNC_TAG = "SEND_SYNC_TAG";
    static final String SYNC_TAG = "SYNC_TAG";

    static void sendBroadcast(Context context, @SyncServerService.SyncCodeFlags int code){
        Intent intent = new Intent(SyncServerService.RECEIVER_FILTER);
        intent.putExtra(SyncServerService.RESULT_CODE, code);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    static HashSet<Future<SyncParam>> getFutureHashSet(ExecutorService executorService, HashSet<SyncParam> syncParams) {
        HashSet<Future<SyncParam>> futureHashSet = new HashSet<>();
        for(SyncParam syncParam : syncParams){
            futureHashSet.add(executorService.submit(new LoadableObjectCallable(syncParam)));
        }

        return futureHashSet;
    }

    static HashSet<SyncParam> getHttpResponseContainerHashSet(HashSet<Future<SyncParam>> futureHashSet) throws ExecutionException, InterruptedException {
        HashSet<SyncParam> jsonHashSet = new HashSet<>();
        for(Future<SyncParam> future : futureHashSet){
            SyncParam syncParam = future.get();
            if(syncParam != null) jsonHashSet.add(syncParam);
        }
        return jsonHashSet;
    }

    static int deleteAllSynchronizedData(LinkedHashSet<SyncParam> syncParamHashSet) {
        int rows = 0;
        for (SyncParam syncParam : syncParamHashSet){
            rows += syncParam.baseDao.deleteAllSyncRealized();
        }

        return rows;
    }

    @SuppressWarnings("unchecked")
    static int insertObjectContainer(HashSet<SyncParam> syncParamHashSet) {
        long init = System.currentTimeMillis();
        HashMap<String, HashSet<BulkParam>> tableBulkParamMap = new HashMap<>();
        for (SyncParam syncParam : syncParamHashSet){
            tableBulkParamMap.put(syncParam.baseDao.getTableName(), syncParam.getBulkParamHashSet());
        }

        int rows = BaseSyncableDao.bulkInsertOrUpdate(tableBulkParamMap);

        long end = System.currentTimeMillis();
        Log.d(SyncServerService.TAG, "insertObjectContainer: " + (end - init));
        return rows;
    }

    @WorkerThread
    public static boolean sendSyncObjects(Context context, LinkedHashSet<SyncParam> syncParams) {
        if(!NetworkUtil.isConnected(context)){
            scheduleJob(context, SEND_SYNC_TAG);
            return false;
        }

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(SEND_SYNC_TAG);

        if(syncParams == null){
            Log.e("SyncService", "erro ao enviar syncParamns: null");
            return false;
        }

        boolean result = true;
        for(SyncParam syncParam : syncParams){
            boolean sent = sendSyncObj(syncParam);
            if(result) result = sent;
        }

        return result;
    }

    static void scheduleJob(Context context, String tag) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(tag);

        Job myJob = dispatcher.newJobBuilder()
                .setService(SyncJobService.class) // the JobService that will be called
                .setTag(tag)        // uniquely identifies the job
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.NOW)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    @SuppressWarnings("unchecked")
    private static boolean sendSyncObj(SyncParam syncParam) {
        HashSet<ModelSyncable> modelHashSet = (HashSet<ModelSyncable>) syncParam.baseDao.getSetBySyncStatus();
        if (modelHashSet == null || modelHashSet.isEmpty()) return true;

        HashSet<ModelSyncable> modelsToUpdate = new HashSet<>();
        for (ModelSyncable model : modelHashSet) {
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation().create();

            String rawJson = gson.toJson(model);
            if(!model.getIdServer().contains("-")){
                syncParam.insertHttpRequest.setId(model.getIdServer());
                syncParam.insertHttpRequest.setMethod(HttpRequest.PUT_METHOD);
            }
            else {
                syncParam.insertHttpRequest.setMethod(HttpRequest.POST_METHOD);
            }
            syncParam.insertHttpRequest.setRawJson(rawJson);
            String jsonResponse = HttpService.consume(syncParam.insertHttpRequest);
            if (jsonResponse == null) return false;

            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.isNull("id")) return false;

                String idServer = jsonObject.getString("id");
                if (idServer.isEmpty()) return false;
                model.setIdServer(idServer);
                model.setSyncStatus(ModelSyncable.SYNC_REALIZED_STATUS);
                modelsToUpdate.add(model);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return !modelsToUpdate.isEmpty() && syncParam.baseDao.updateAfterSync(modelsToUpdate);
    }

    @WorkerThread
    public static boolean checkDatabasePendents(@NonNull Context context, @NonNull LinkedHashSet<SyncParam> syncParams){
        if(!sendSyncObjects(context, syncParams)) return false;
        deleteAllSynchronizedData(syncParams);

        return true;
    }
}
