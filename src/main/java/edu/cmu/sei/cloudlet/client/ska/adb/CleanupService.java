package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class CleanupService extends Service {
    private final String TAG = "CleanupService";

    public CleanupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.v(TAG, "Received cleanup request.");

        Log.v(TAG, "Removing output file.");
        File file = new File(ADBConstants.OUT_FILE_PATH);
        file.delete();
        Log.v(TAG, "Finished deleting output file.");

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
