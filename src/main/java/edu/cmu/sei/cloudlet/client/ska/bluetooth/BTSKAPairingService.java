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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Sebastian on 2015-05-20.
 */
public class BTSKAPairingService {

    private static final String TAG = "SKAPairingService";

    private static final String SKA_PAIRING_SERVICE_NAME = "SKAPairingService";
    private static final UUID SKA_PAIRING_SERVICE_ID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private PairingThread mPairingThread;

    public BTSKAPairingService(Context context) {
        mContext = context;
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
                    BTMessageHandler handler = new BTMessageHandler(receivingSocket, mContext);
                    handler.receiveMessages();

                    Log.v(TAG, "Stopped receiving messages.");
                }

            }
        }
    }

}
