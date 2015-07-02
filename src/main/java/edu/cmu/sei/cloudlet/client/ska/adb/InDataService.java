package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class InDataService extends Service {
    private final String TAG = "InDataService";

    // This will handle data received.
    private IInDataHandler mDataHandler;

    public InDataService() {
        // TODO: this adds an unnecessary dependency here. Should be moved out somehow.
        mDataHandler = new IBCInDataHandler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.v(TAG, "Received data.");
        Bundle extras = intent.getExtras();
        Log.v(TAG, "Number of items: " + extras.size());

        mDataHandler.handleData(extras, this);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
