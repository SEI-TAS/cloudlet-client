package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class StoreFileService extends Service {
    private final String TAG = "StoreFileService";

    HashMap<String, String> mFiles = new HashMap<String, String>();

    public StoreFileService() {
        mFiles.put(IBCAuthManager.SERVER_PUBLIC_KEY_ID, "server_key.pub");
        mFiles.put(IBCAuthManager.DEVICE_PRIVATE_KEY_ID, "device.key");
        mFiles.put(IBCAuthManager.SERVER_CERTIFICATE_ID, "server.pem");
        mFiles.put(IBCAuthManager.DEVICE_CERTIFICATE_ID, "device.pem");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        // File path depends on the file we are getting.
        String fileId = intent.getStringExtra("file_id");
        String filePath = ADBConstants.ADB_FILES_PATH + mFiles.get(fileId);
        Log.v(TAG, "Received request to store file of type " + fileId);

        byte[] data = FileHandler.readFromFile(filePath);
        IBCAuthManager.storeFile(data, fileId);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
