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
