package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.utils.FileHandler;

public class StoreDeviceCertificateService extends Service {
    private final String TAG = "StoreDeviceCertificateService";

    private final String CERT_FILE = ADBConstants.ADB_FILES_PATH + "device_certificate.cer";

    public StoreDeviceCertificateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        byte[] data = FileHandler.readFromFile(CERT_FILE);
        IBCAuthManager.storeDeviceCertificate(data);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
