package edu.cmu.sei.cloudlet.client.ska;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Sebastian on 2015-05-20.
 */
public class MessageHandler {
    private final String TAG = "MessageHandler";

    private final String CMD_GET_ID = "id";

    private final BluetoothSocket mSocket;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;

    private final Context mContext;

    public MessageHandler(BluetoothSocket socket, Context context) {
        mSocket = socket;
        mContext = context;

        try {
            mInStream = socket.getInputStream();
            mOutStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error getting stream.", e);
        }
    }

    public void receiveMessages() {
        Log.v(TAG, "Receiving messages");
        int num_bytes;
        byte[] buffer = new byte[4096];
        String message = null;

        while (true) {
            try {
                num_bytes = mInStream.read(buffer);
                Log.v(TAG, "Num bytes: " + num_bytes);

                if(num_bytes > 0) {
                    byte[] message_bytes = new byte[num_bytes];
                    System.arraycopy(buffer, 0, message_bytes, 0, num_bytes);
                    message = new String(message_bytes);
                    Log.v(TAG, "Message received: " + message);

                    handleMessage(message);
                }
            } catch (IOException e) {
                Log.e(TAG, "No longer connected.");
                break;
            }
        }
    }

    private void handleMessage(String message) {
        if(message.equals(CMD_GET_ID)) {
            Log.v("Test", "id request");
            String androidId = Settings.Secure.getString(
                    mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.v("Test", androidId);

            sendMessage(androidId);
        }
    }

    public void sendMessage(String message) {
        byte[] send = message.getBytes();
        try {
            mOutStream.write(send);
        } catch (IOException e) {
            Log.e(TAG, "Problem sending message.", e);
        }
    }
}
