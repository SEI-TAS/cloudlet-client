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
package edu.cmu.sei.cloudlet.client.ibc;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;
import edu.cmu.sei.cloudlet.client.ska.adb.IInDataHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IInFileHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IOutDataHandler;

/**
 * Created by Sebastian on 2015-07-02.
 */
public class IBCDataHandler implements IInDataHandler, IOutDataHandler, IInFileHandler {
    private final String TAG = "IBCInDataHandler";

    private static final String DEVICE_ID = "device_id";

    private static final String SSID_ID = "ssid";
    private static final String SERVER_CERT_NAME = "server_cert_name";
    private static final String PASSWORD = "password";

    @Override
    public String getData(JSONObject data, Context context) {
        JSONObject outputData = new JSONObject();

        Iterator<String> iter = data.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            if(DEVICE_ID.equals(key)) {
                String deviceId = IBCAuthManager.getDeviceId(context);
                try {
                    outputData.put(DEVICE_ID, deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return outputData.toString();
    }

    @Override
    public void handleData(JSONObject data, Context context) {
        String networkId = "";
        String certName = "";
        String password = "";

        Iterator<String> iter = data.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                if(SSID_ID.equals(key)) {
                    networkId = data.getString(key);
                    Log.v(TAG, "Received ssid: " + networkId);
                }
                if(SERVER_CERT_NAME.equals(key)) {
                    certName = data.getString(key);
                    Log.v(TAG, "Received cert filename: " + certName);
                }
                if(PASSWORD.equals(key)) {
                    password = data.getString(key);
                    Log.v(TAG, "Received password: " + password);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!networkId.equals("") && !certName.equals("") && !password.equals("")) {
            IBCAuthManager.setupWifiProfile(networkId, certName, password, context);
        }
    }

    @Override
    public void storeFile(byte[] fileContents, String fileName, Context context) {
        IBCAuthManager.storeFile(fileContents, fileName);
    }
}
