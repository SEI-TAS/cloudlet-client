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
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.net.wifi.WifiEnterpriseConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.cmu.sei.cloudlet.client.utils.FileHandler;

/**
 * Assumes device can store information of only 1 cloudlet at one time.
 * Created by Sebastian on 2015-05-27.
 */
public class IBCAuthManager {
    private static final String TAG = "IBCAuthManager";

    private static final String IBC_FOLDER_PATH = FileHandler.APP_BASE_FOLDER + "ibc/";

    /**
     * Returns the ID for the device.
     * @return a String representing a unique id for the device.
     */
    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    /**
     * Stores an IBC related file.
     * @param fileContents
     */
    public static void storeFile(byte[] fileContents, String fileId) {
        Log.v(TAG, "File contents for file " + fileId + ": " + new String(fileContents));
        FileHandler.writeToFile(IBC_FOLDER_PATH + fileId, fileContents);
    }

    /**
     * Creates a WPA2-Enterprise configuration with the current files.
     * @param context
     */
    public static void setupWifiProfile(String ssid, String serverFileName, String password, Context context) {
        // Create a cert object from the certificate file.
        X509Certificate serverCertificate = null;
        try {
            CertificateFactory certificateGenerator = CertificateFactory.getInstance("X.509");
            Collection certs = certificateGenerator.generateCertificates(new FileInputStream(IBC_FOLDER_PATH + serverFileName));

            // We know our file will have only 1 certificate, so we get the first one.
            serverCertificate = (X509Certificate) certs.iterator().next();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            Log.e(TAG, "No valid certificates found");
            return;
        }

        // Create basic network configuration.
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;

        // Configure EAP-TTLS and PAP specific parameters.
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
        enterpriseConfig.setCaCertificate(serverCertificate);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
        enterpriseConfig.setIdentity(getDeviceId(context));
        enterpriseConfig.setPassword(password);
        wifiConfig.enterpriseConfig = enterpriseConfig;

        // Store the profile.
        // TODO: test if this fails when there is already a profile with the same SSID.
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiManager.addNetwork(wifiConfig);
    }
}
