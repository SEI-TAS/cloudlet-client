package edu.cmu.sei.cloudlet.client.ska.adb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.cmu.sei.cloudlet.client.ibc.IBCRepoManager;

public class StoreCertificateService extends Service {
    private final String TAG = "StoreCertificateService";

    private final String CERT_FILE = FileHandler.FILES_PATH + "server_certificate.cer";

    public StoreCertificateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        byte[] data = FileHandler.readFromFile(CERT_FILE);
        IBCRepoManager.storeServerCertificate(data);

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
