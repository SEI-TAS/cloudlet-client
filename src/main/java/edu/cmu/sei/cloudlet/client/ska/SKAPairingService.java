package edu.cmu.sei.cloudlet.client.ska;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Sebastian on 2015-05-20.
 */
public class SKAPairingService {

    private static final String TAG = "SKAPairingService";

    private static final String SKA_PAIRING_SERVICE_NAME = "SKAPairingService";
    private static final UUID SKA_PAIRING_SERVICE_ID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    private PairingThread mPairingThread;

    public SKAPairingService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void start() {
        Log.v(TAG, "Starting SKAPairingService.");
        if (mPairingThread == null) {
            mPairingThread = new PairingThread();
            mPairingThread.start();
        }
    }

    public void stop() {
        Log.v(TAG, "Stopping SKAPairingService.");
        if (mPairingThread != null) {
            mPairingThread.cancel();
            mPairingThread = null;
        }
    }

    private class PairingThread extends Thread {
        private BluetoothServerSocket mListeningSocket;

        public PairingThread() {
            // Create a new listening server socket
            try {
                mListeningSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SKA_PAIRING_SERVICE_NAME, SKA_PAIRING_SERVICE_ID);
            } catch (IOException e) {
                Log.e(TAG, "Error opening Bluetooth socket", e);
                mListeningSocket = null;
            }
        }

        public void cancel() {
            try {
                Log.v(TAG, "Stopping pairing thread.");
                mListeningSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth socket", e);
            }
        }

        public void run() {
            setName("PairingThread");
            Log.v(TAG, "Starting pairing thread.");

            BluetoothSocket receivingSocket = null;
            while(true) {
                try {
                    receivingSocket = mListeningSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Error accepting socket connection, or socket closed");
                    break;
                }

                if(receivingSocket != null) {
                    // Start a thread to handle the request. Or handle it here if we want just 1 connection...
                    // Receive commands.
                }

            }
        }
    }

}
