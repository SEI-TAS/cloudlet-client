package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.UnsupportedEncodingException;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class StoreNetworkIdService extends Service {
    private final String TAG = "StoreNetworkIdService";

    private final String NET_ID_FILE = ADBConstants.ADB_FILES_PATH + "network_id.txt";

    public StoreNetworkIdService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        byte[] data = FileHandler.readFromFile(NET_ID_FILE);

        try {
            String networkId = new String(data, "UTF-8");
            IBCAuthManager.setupWifiProfile(networkId, this);
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
