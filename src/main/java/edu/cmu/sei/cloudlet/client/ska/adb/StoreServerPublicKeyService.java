package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class StoreServerPublicKeyService extends Service {
    private final String TAG = "StoreServerPublicKeyService";

    private final String SERVER_KEY_FILE = ADBConstants.ADB_FILES_PATH + "server_key.pub";

    public StoreServerPublicKeyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        byte[] data = FileHandler.readFromFile(SERVER_KEY_FILE);
        IBCAuthManager.storeServerPublicKey(data);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
