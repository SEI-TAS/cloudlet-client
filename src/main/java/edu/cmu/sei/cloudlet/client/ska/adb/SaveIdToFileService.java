package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class SaveIdToFileService extends Service {
    private final String TAG = "SaveIdToFileService";

    private final String ID_FILE = ADBConstants.ADB_FILES_PATH + "id.txt";

    public SaveIdToFileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        String deviceId = IBCAuthManager.getDeviceId(this);
        Log.v(TAG, "Id: " + deviceId);

        Log.v(TAG, "Writing to file.");
        FileHandler.writeToFile(ID_FILE, deviceId);
        Log.v(TAG, "Finished writing to file.");

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
