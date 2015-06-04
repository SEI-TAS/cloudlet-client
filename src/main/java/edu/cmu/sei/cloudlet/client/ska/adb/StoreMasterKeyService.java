package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.cmu.sei.cloudlet.client.ibc.IBCRepoManager;

public class StoreMasterKeyService extends Service {
    private final String TAG = "StoreMasterKeyService";

    private final String MKEY_FILE = FileHandler.FILES_PATH + "master_key.txt";

    public StoreMasterKeyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        byte[] data = FileHandler.readFromFile(MKEY_FILE);
        IBCRepoManager.storeMasterKey(data);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
