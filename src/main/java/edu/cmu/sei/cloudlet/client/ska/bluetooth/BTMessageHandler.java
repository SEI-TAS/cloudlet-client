package edu.cmu.sei.cloudlet.client.ska.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;

/**
 * Handles message receiving, sending, and processing.
 * Created by Sebastian on 2015-05-20.
 */
public class BTMessageHandler {
    private final String TAG = "MessageHandler";

    private final String CMD_GET_ID = "id";
    private final String CMD_FILE_MASTER_PUBLIC_KEY = "server_public_key";
    private final String CMD_FILE_DEVICE_PRIVATE_KEY = "device_private_key";
    private final String CMD_FILE_SERVER_CERTIFICATE = "server_certificate";
    private final String CMD_FILE_DEVICE_CERTIFICATE = "device_certificate";
    private final String CMD_NETWORK_ID = "network_id";

    private final String REPLY_ACK = "ack";

    private final int CHUNK_SIZE = 4096;
    private final int MAX_FILE_SIZE = 10*1024*1024;

    private final Context mContext;
    private final BluetoothSocket mSocket;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;

    public BTMessageHandler(BluetoothSocket socket, Context context) {
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

                    // Messages will have either a command, or a command plus data.
                    String[] messageParts = message.split("#", 2);
                    String command = messageParts[0];
                    String data = null;
                    if(messageParts.length > 1)
                        data = messageParts[1];
                    handleMessage(command, data);
                }
            } catch (IOException e) {
                Log.e(TAG, "No longer connected.");
                break;
            }
        }
    }

    /**
     * Handles a received message.
     * @param command a command from a cloudlet.
     */
    private void handleMessage(String command, String data) {
        if(command.equals(CMD_GET_ID)) {
            Log.v(TAG, "id request");
            String id = IBCAuthManager.getDeviceId(this.mContext);
            Log.v(TAG, id);

            // Send the reply.
            sendMessage(id);
        }
        // TODO: change all these commands to one file send command with the id as the data.
        else if (command.equals(CMD_FILE_MASTER_PUBLIC_KEY)) {
            Log.v(TAG, "master public key file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();

            IBCAuthManager.storeServerPublicKey(fileData);
            Log.v(TAG, "IBC master public key stored.");
        }
        else if (command.equals(CMD_FILE_DEVICE_PRIVATE_KEY)) {
            Log.v(TAG, "device private key file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();

            IBCAuthManager.storeDevicePrivateKey(fileData);
            Log.v(TAG, "IBC private key stored.");
        }
        else if (command.equals(CMD_FILE_SERVER_CERTIFICATE)) {
            Log.v(TAG, "server certificate file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();

            IBCAuthManager.storeServerCertificate(fileData);
            Log.v(TAG, "Server certificate stored.");
        }
        else if (command.equals(CMD_FILE_DEVICE_CERTIFICATE)) {
            Log.v(TAG, "device certificate file send request");
            sendMessage(REPLY_ACK);

            byte[] fileData = receiveFile();

            IBCAuthManager.storeDeviceCertificate(fileData);
            Log.v(TAG, "Device certificate stored.");
        }
        else if (command.equals(CMD_NETWORK_ID)) {
            Log.v(TAG, "Request to receive network id.");
            sendMessage(REPLY_ACK);

            IBCAuthManager.setupWifiProfile(data, mContext);
            Log.v(TAG, "Network profile created.");
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
     * @return A String with the file data.
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

        String fileAsString = new String(realFileData);
        Log.v(TAG, "Data in file:");
        Log.v(TAG, fileAsString);

        return realFileData;
    }
}
