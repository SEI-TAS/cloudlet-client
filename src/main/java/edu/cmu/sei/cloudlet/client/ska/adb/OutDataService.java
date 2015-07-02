package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class OutDataService extends Service {
    private final String TAG = "OutDataService";

    private final String OUT_FILE_PATH = ADBConstants.ADB_FILES_PATH + "out_data.json";

    // This get the data requested.
    private IOutDataHandler mDataHandler;

    public OutDataService() {
        // TODO: this adds an unnecessary dependency here. Should be moved out somehow.
        mDataHandler = new IBCOutDataHandler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.v(TAG, "Received data request.");
        Bundle extras = intent.getExtras();
        Log.v(TAG, "Number of items requested: " + extras.size());

        String jsonDataAsString = mDataHandler.getData(extras, this);

        Log.v(TAG, "Writing to file.");
        FileHandler.writeToFile(OUT_FILE_PATH, jsonDataAsString);
        Log.v(TAG, "Finished writing to file.");

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
