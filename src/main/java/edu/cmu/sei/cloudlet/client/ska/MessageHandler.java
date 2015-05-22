package edu.cmu.sei.cloudlet.client.ska;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles message receiving, sending, and processing.
 * Created by Sebastian on 2015-05-20.
 */
public class MessageHandler {
    private final String TAG = "MessageHandler";

    private final String CMD_GET_ID = "id";
    private final String CMD_FILE_MASTER_PUBLIC_KEY = "master_public_key";
    private final String CMD_FILE_DEVICE_PRIVATE_KEY = "device_private_key";
    private final String CMD_FILE_SERVER_CERTIFICATE = "server_certificate";
    private final String CMD_FILE_END = "file_end";

    private final String REPLY_ACK = "ack";

    private final int CHUNK_SIZE = 4096;
    private final int MAX_FILE_SIZE = 10*1024*1024;

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

    /**
     * Receives messages until disconnected.
     */
    public void receiveMessages() {
        Log.v(TAG, "Receiving messages");
        int num_bytes;
        byte[] buffer = new byte[CHUNK_SIZE];
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

    /**
     * Handles a received message.
     * @param message a command from a cloudlet.
     */
    private void handleMessage(String message) {
        if(message.equals(CMD_GET_ID)) {
            Log.v(TAG, "id request");
            String androidId = Settings.Secure.getString(
                    mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.v(TAG, androidId);

            // Send the reply.
            sendMessage(androidId);
        }
        else if (message.equals(CMD_FILE_MASTER_PUBLIC_KEY)) {
            Log.v(TAG, "master public key file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();
            String fileAsString = new String(fileData);
            Log.v(TAG, "Data in file:");
            Log.v(TAG, fileAsString);
        }
        else if (message.equals(CMD_FILE_DEVICE_PRIVATE_KEY)) {
            Log.v(TAG, "device private key file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();
            String fileAsString = new String(fileData);
            Log.v(TAG, "Data in file:");
            Log.v(TAG, fileAsString);
        }
        else if (message.equals(CMD_FILE_SERVER_CERTIFICATE)) {
            Log.v(TAG, "server certificate file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();
            String fileAsString = new String(fileData);
            Log.v(TAG, "Data in file:");
            Log.v(TAG, fileAsString);
        }
    }

    /**
     * Sends a message to the cloudlet.
     * @param message
     */
    public void sendMessage(String message) {
        byte[] send = message.getBytes();
        try {
            mOutStream.write(send);
        } catch (IOException e) {
            Log.e(TAG, "Problem sending message.", e);
        }
    }

    /**
     * Receives a file and returns it as a byte array.
     * @return A byte array with the file data.
     */
    protected byte[] receiveFile() {
        Log.v(TAG, "Receiving file");
        int numBytes;
        byte[] chunkBytes;
        byte[] fileData = new byte[MAX_FILE_SIZE];
        int fileDataSize = 0;
        byte[] buffer = new byte[CHUNK_SIZE];
        int fileTotalSize = 0;

        try {
            // First get the file size.
            numBytes = mInStream.read(buffer);
            chunkBytes = new byte[numBytes];
            System.arraycopy(buffer, 0, chunkBytes, 0, numBytes);
            fileTotalSize = Integer.parseInt(new String(chunkBytes));
            Log.v(TAG, "File size: " + fileTotalSize);
            sendMessage(REPLY_ACK);

            while(true) {
                numBytes = mInStream.read(buffer);
                Log.v(TAG, "Num bytes: " + numBytes);

                if (numBytes > 0) {
                    chunkBytes = new byte[numBytes];
                    System.arraycopy(buffer, 0, chunkBytes, 0, numBytes);

                    String partial = new String(chunkBytes);
                    Log.v(TAG, "Curr part: " + partial);

                    // Accumulate data into the overall file data buffer.
                    System.arraycopy(buffer, 0, fileData, fileDataSize, numBytes);
                    fileDataSize += numBytes;

                    // Stop receiving when we have obtained enough bytes for the given file size.
                    if(fileDataSize >= fileTotalSize) {
                        Log.v(TAG, "Finished receiving file.");
                        break;
                    }
                }
            }
        } catch (IOException e) {
                Log.e(TAG, "No longer connected.");
        }

        // Create a buffer with the exact size of the received data.
        Log.v(TAG, "File size: " + fileTotalSize + ", received: " + fileDataSize);
        byte[] realFileData = new byte[fileDataSize];
        System.arraycopy(fileData, 0, realFileData, 0, fileDataSize);
        return realFileData;
    }
}
