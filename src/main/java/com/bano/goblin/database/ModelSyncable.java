package com.bano.goblin.database;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * Created by Alexandre on 09/05/2017.
 */

public abstract class ModelSyncable extends Model{

    @IntDef(value = {SYNC_REALIZED_STATUS, SYNC_PENDENT_STATUS,
            SYNC_INCOMPLETE_STATUS, SYNC_ERROR_STATUS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncStatusFlags {}

    public static final int SYNC_REALIZED_STATUS = 0;
    public static final int SYNC_PENDENT_STATUS = 1;
    public static final int SYNC_INCOMPLETE_STATUS = 2;
    public static final int SYNC_ERROR_STATUS = 3;

    private String idServer;
    private int syncStatus;

    protected ModelSyncable(){
        super();
        idServer = id;
    }

    protected ModelSyncable(String id, String idServer){
        super(id);
        this.idServer = idServer;
        if(idServer == null) setIdServer(id);
    }

    protected ModelSyncable(Parcel in) {
        super(in);
        idServer = in.readString();
    }

    public String getIdServer() {
        return idServer;
    }

    public void setIdServer(String idServer) {
        this.idServer = idServer;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(@SyncStatusFlags int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public boolean isErrorSyncStatus(){
        return this.syncStatus == ModelSyncable.SYNC_INCOMPLETE_STATUS
                || this.syncStatus == ModelSyncable.SYNC_ERROR_STATUS;
    }

    public boolean isIncompleteSyncStatus(){
        return this.syncStatus == ModelSyncable.SYNC_INCOMPLETE_STATUS;
    }

    public boolean isPendentSyncStatus(){
        return this.syncStatus == ModelSyncable.SYNC_PENDENT_STATUS;
    }
}
