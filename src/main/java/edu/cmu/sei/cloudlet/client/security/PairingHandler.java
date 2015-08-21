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
package edu.cmu.sei.cloudlet.client.security;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.cmu.sei.ams.cloudlet.android.CredentialsManager;
import edu.cmu.sei.cloudlet.client.ska.adb.IInDataHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IInFileHandler;
import edu.cmu.sei.cloudlet.client.ska.adb.IOutDataHandler;

/**
 * Created by Sebastian on 2015-07-02.
 */
public class PairingHandler implements IInDataHandler, IOutDataHandler, IInFileHandler {
    private final String TAG = "PairingHandler";

    private static final String DEVICE_ID = "device_id";

    private static final String COMMAND = "command";
    private static final String SSID_ID = "ssid";
    private static final String SERVER_CERT_NAME = "server_cert_name";
    private static final String PASSWORD = "password";

    private static final String CMD_WIFI_PROFILE = "wifi-profile";

    /**
     * Handles a data request. Only handles known data keys.
     * @param data a Json object where the keys are the data being requested.
     * @param context Android's context.
     * @return a Json object containing key-pairs for all the keys requested.
     */
    @Override
    public String getData(JSONObject data, Context context) {
        JSONObject outputData = new JSONObject();

        Iterator<String> iter = data.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            if(DEVICE_ID.equals(key)) {
                String deviceId = CredentialsManager.getDeviceId(context);
                try {
                    outputData.put(DEVICE_ID, deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return outputData.toString();
    }

    /**
     * Handles receiving new data commands.
     * Currently, only handles 1 command, set up Wi-Fi profile, containing 3 keys: network id,
     * cert name and password.
     * @param data a Json object containing key-value data pairs to be handled locally.
     * @param context Android's context.
     */
    @Override
    public void handleData(JSONObject data, Context context) {
        try {
            String command = data.getString(COMMAND);
            if(CMD_WIFI_PROFILE.equals(command)) {
                // Get all required data.
                String networkId = data.getString(SSID_ID);
                Log.v(TAG, "Received ssid: " + networkId);
                String certName = data.getString(SERVER_CERT_NAME);
                Log.v(TAG, "Received cert filename: " + certName);
                String password = data.getString(PASSWORD);
                Log.v(TAG, "Received password: " + password);

                // A network profile will only be created if we got all three data in the same message.
                if(!networkId.equals("") && !certName.equals("") && !password.equals("")) {
                    String serverCertificatePath = CredentialsManager.CREDENTIALS_FOLDER_PATH + certName;
                    WifiProfileManager.setupWifiProfile(networkId, serverCertificatePath,
                            CredentialsManager.getDeviceId(context), password, context);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing command.");
            e.printStackTrace();
        }
    }

    /**
     * Handles a request to store a file locally.
     * @param fileContents the file bytes
     * @param fileName the file name/id (the file will be stored with this filename).
     * @param context Android's context.
     */
    @Override
    public void storeFile(byte[] fileContents, String fileName, Context context) {
        CredentialsManager.storeFile(fileContents, fileName);
    }
}