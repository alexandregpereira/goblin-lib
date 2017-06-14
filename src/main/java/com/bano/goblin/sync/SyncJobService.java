package com.bano.goblin.sync;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 *
 * Created by Alexandre on 01/06/2017.
 */

public class SyncJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        switch (job.getTag()){
            case SyncServerHelper.SEND_SYNC_TAG:
                SyncServerService.startSendSyncObj(this);
                break;
            case SyncServerHelper.SYNC_TAG:
                SyncServerService.startSync(this);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
