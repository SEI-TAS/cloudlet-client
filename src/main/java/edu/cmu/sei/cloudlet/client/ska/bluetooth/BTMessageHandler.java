/*
KVM-based Discoverable Cloudlet (KD-Cloudlet)
Copyright (c) 2015 Carnegie Mellon University.
All Rights Reserved.

THIS SOFTWARE IS PROVIDED "AS IS," WITH NO WARRANTIES WHATSOEVER. CARNEGIE MELLON UNIVERSITY EXPRESSLY DISCLAIMS TO THE FULLEST EXTENT PERMITTEDBY LAW ALL EXPRESS, IMPLIED, AND STATUTORY WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF PROPRIETARY RIGHTS.

Released under a modified BSD license, please see license.txt for full terms.
DM-0002138

KD-Cloudlet includes and/or makes use of the following Third-Party Software subject to their own licenses:
MiniMongo
Copyright (c) 2010-2014, Steve Lacy
All rights reserved. Released under BSD license.
https://github.com/MiniMongo/minimongo/blob/master/LICENSE

Bootstrap
Copyright (c) 2011-2015 Twitter, Inc.
Released under the MIT License
https://github.com/twbs/bootstrap/blob/master/LICENSE

jQuery JavaScript Library v1.11.0
http://jquery.com/
Includes Sizzle.js
http://sizzlejs.com/
Copyright 2005, 2014 jQuery Foundation, Inc. and other contributors
Released under the MIT license
http://jquery.org/license
*/
package edu.cmu.sei.cloudlet.client.ska.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.cmu.sei.cloudlet.client.security.PairingHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IInDataHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IOutDataHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IInFileHandler;

/**
 * Handles message receiving, sending, and processing.
 * Created by Sebastian on 2015-05-20.
 */
public class BTMessageHandler {
    private final String TAG = "MessageHandler";

    private static final String CMD_SEND_DATA = "send_data";
    private static final String CMD_RECEIVE_DATA = "receive_data";
    private static final String CMD_RECEIVE_FILE = "receive_file";

    private static final String REPLY_ACK = "ack";

    private static final int CHUNK_SIZE = 4096;
    private static final int MAX_FILE_SIZE = 10*1024*1024;

    private final Context mContext;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;

    private IInDataHandler mInDataHandler;
    private IOutDataHandler mOutDataHandler;
    private IInFileHandler mFileDataHandler;

    public BTMessageHandler(BluetoothSocket socket, Context context) {
        mContext = context;

        try {
            mInStream = socket.getInputStream();
            mOutStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error getting stream.", e);
        }

        // TODO: this adds an unnecessary dependency here. Should be moved out somehow.
        mInDataHandler = new PairingHandler();
        mOutDataHandler = (IOutDataHandler) mInDataHandler;
        mFileDataHandler = (IInFileHandler) mInDataHandler;
    }

    /**
     * Receives messages until disconnected.
     */
    public void receiveMessages() {
        Log.v(TAG, "Receiving messages");
        int num_bytes;
        byte[] buffer = new byte[CHUNK_SIZE];

        while (true) {
            try {
                num_bytes = mInStream.read(buffer);
                Log.v(TAG, "Num bytes: " + num_bytes);

                if(num_bytes > 0) {
                    byte[] message_bytes = new byte[num_bytes];
                    System.arraycopy(buffer, 0, message_bytes, 0, num_bytes);
                    String message = new String(message_bytes);
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
     * @param message a Json string.
     */
    private void handleMessage(String message) {
        try {
            JSONObject parsedMessage = new JSONObject(message);

            // Get the command and remove it since we will pass the same structure with the data
            // to specific handlers.
            String command = parsedMessage.getString("bt_command");
            parsedMessage.remove("bt_command");

            if(command.equals(CMD_SEND_DATA)) {
                Log.v(TAG, "Data request");
                String data = mOutDataHandler.getData(parsedMessage, mContext);
                Log.v(TAG, data);
                sendMessage(data);
                Log.v(TAG, "Finished sending data");
            }
            else if(command.equals(CMD_RECEIVE_DATA)) {
                Log.v(TAG, "Receiving data");
                Log.v(TAG, parsedMessage.toString());
                String result = mInDataHandler.handleData(parsedMessage, mContext);

                //sendMessage(REPLY_ACK);
                Log.v(TAG, "Finished processing received data");

                Log.v(TAG, "Returning result.");
                sendMessage(result);
            }
            else if(command.equals(CMD_RECEIVE_FILE)) {
                Log.v(TAG, "Receiving a file");
                String fileName = parsedMessage.getString("file_id");
                Log.v(TAG, fileName);
                sendMessage(REPLY_ACK);

                // Get the actual file data contents.
                byte[] fileData = receiveFile();
                //sendMessage(REPLY_ACK);

                String result = mFileDataHandler.storeFile(fileData, fileName, mContext);
                Log.v(TAG, "Finished processing received file");

                Log.v(TAG, "Returning result.");
                sendMessage(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
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

            while(fileTotalSize > 0) {
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
