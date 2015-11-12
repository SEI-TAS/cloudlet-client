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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.sei.ams.cloudlet.android.utils.FileHandler;
import edu.cmu.sei.cloudlet.client.security.PairingHandler;
import edu.cmu.sei.cloudlet.client.ska.IInDataHandler;

public class InDataService extends Service {
    private final String TAG = "InDataService";

    // This will handle data received.
    private IInDataHandler mDataHandler;

    public InDataService() {
        // TODO: this adds an unnecessary dependency here. Should be moved out somehow.
        mDataHandler = new PairingHandler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.v(TAG, "Received data.");
        Bundle extras = intent.getExtras();
        Log.v(TAG, "Number of items: " + extras.size());

        JSONObject jsonData = new JSONObject();
        for(String key : extras.keySet()) {
            try {
                jsonData.put(key, extras.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String result = mDataHandler.handleData(jsonData, this);

        Log.v(TAG, "Writing result to file.");
        FileHandler.writeToFile(ADBConstants.OUT_FILE_PATH, result);
        Log.v(TAG, "Finished writing result to file.");

        // We don't need this service to run anymore.
        stopSelf();
        return START_NOT_STICKY;
    }
}
