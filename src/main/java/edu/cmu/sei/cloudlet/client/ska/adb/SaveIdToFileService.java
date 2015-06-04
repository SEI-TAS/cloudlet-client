package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;

import edu.cmu.sei.cloudlet.client.ibc.IBCRepoManager;

public class SaveIdToFileService extends Service {
        private final String TAG = "SaveIdToFileService";
    private final String FILES_PATH = "/sdcard/cloudlet/adb/";
    private final String ID_FILE = FILES_PATH + "id.txt";

    public SaveIdToFileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        try {
            String deviceId = IBCRepoManager.getId(this);
            Log.v(TAG, "Id: " + deviceId);

            // Create the folders.
            File folders = new File(FILES_PATH);
            folders.mkdirs();

            // Write the id to the file.
            Log.v(TAG, "Writing to file.");
            PrintWriter writer = new PrintWriter(ID_FILE, "UTF-8");
            writer.print(deviceId);
            writer.close();
            Log.v(TAG, "Finished writing to file.");
        } catch(Exception e) {
            Log.e(TAG, "Error saving id to file: " + e.toString());
        }


        // We don't need this service to run anymore.
        stopSelf();

        return START_NOT_STICKY;
    }
}
