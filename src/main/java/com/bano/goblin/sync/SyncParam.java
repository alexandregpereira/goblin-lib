package com.bano.goblin.sync;

import com.bano.goblin.database.BaseSyncableDao;
import com.bano.goblin.database.BulkParam;
import com.bano.goblin.database.Model;
import com.bano.goblin.database.ModelSyncable;
import com.bano.goblin.http.HttpRequest;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;

/**
 *
 * Created by Alexandre on 12/05/2017.
 */

public class SyncParam<T extends ModelSyncable> extends Model {

    final BaseSyncableDao<T> baseDao;
    final TypeToken<HashSet<T>> typeToken;
    final HttpRequest readHttpRequest;
    final HttpRequest insertHttpRequest;
    private HashSet<T> hashSet;

    public SyncParam(BaseSyncableDao<T> baseDao, HttpRequest readHttpRequest, HttpRequest insertHttpRequest,
                     TypeToken<HashSet<T>> typeToken) {
        this.baseDao = baseDao;
        this.typeToken = typeToken;
        this.readHttpRequest = readHttpRequest;
        this.insertHttpRequest = insertHttpRequest;
    }

    HashSet<BulkParam> getBulkParamHashSet() {
        HashSet<BulkParam> bulkParams = new HashSet<>();
        for (T t : hashSet){
            bulkParams.add(new BulkParam(baseDao.getSyncContentValues(t), baseDao.getIdFieldName(), baseDao.getIdServerFieldName()));
        }

        return bulkParams;
    }

    void setHashSet(HashSet<T> hashSet) {
        for(T t : hashSet){
            if(t.getIdServer() == null) t.setIdServer(t.id);
        }
        this.hashSet = hashSet;
    }
}
