package com.ubergrund.android.tinyjournal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 10/28/13
 * Time: 6:40 AM
 */
public class SyncService extends Service {

    private static final String TAG = "TinyJournal/SyncService";

    private static JournalSyncAdapter syncAdapter = null;
    private static final Object syncAdapterMonitor = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (syncAdapterMonitor) {
            if (syncAdapter == null)
                syncAdapter = new JournalSyncAdapter(getApplicationContext(), true);
        }
    }


    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

}
