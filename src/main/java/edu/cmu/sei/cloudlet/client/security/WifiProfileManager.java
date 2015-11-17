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
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.net.wifi.WifiEnterpriseConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import edu.cmu.sei.ams.cloudlet.android.AndroidCredentialsManager;
import edu.cmu.sei.ams.cloudlet.android.DeviceIdManager;

/**
 * Handles the creation of Wi-Fi profiles for secure communication with a cloudlet.
 * Created by Sebastian on 2015-05-27.
 */
public class WifiProfileManager {
    private static final String TAG = "WifiProfileManager";

    public static final String SSID_FILE_NAME = "ssid";
    public static final String SERVER_CERT_PATH_FILE_NAME = "server_cert_path";
    public static final String AUTH_PASSWORD_FILE_NAME = "auth_password";

    /**
     * Creates a WPA2-Enterprise configuration with the give data.
     * @param ssid the network ssid
     * @param serverFilePath the path to the server certifiate to use
     * @param deviceId the device id to be set
     * @param password the device password to be set
     * @param context Android's context
     */
    public static int setupWPA2WifiProfile(String ssid, String serverFilePath, String deviceId,
                                        String password, Context context) throws CertificateException, FileNotFoundException {
        // Create a cert object from the certificate file.
        CertificateFactory certificateGenerator = CertificateFactory.getInstance("X.509");
        X509Certificate serverCertificate = (X509Certificate) certificateGenerator.generateCertificate(new FileInputStream(serverFilePath));
        //Log.v(TAG, "Certificate: " + serverCertificate);

        // Create basic network configuration.
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;
        wifiConfig.status = WifiConfiguration.Status.DISABLED;

        // Configure EAP-TTLS and PAP specific parameters.
        // Set security methods to use.
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);

        // Set client and server credentials.
        enterpriseConfig.setIdentity(deviceId);
        enterpriseConfig.setCaCertificate(serverCertificate);
        enterpriseConfig.setPassword(password);

        // Store the security profile in the Wi-Fi profile.
        wifiConfig.enterpriseConfig = enterpriseConfig;

        // Get access to WiFi profiles.
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        // First remove it, if it exists.
        WifiProfileManager.removeNetworkProfile(wifiManager, ssid);

        // Store the profile.
        int netword_profile_id = wifiManager.addNetwork(wifiConfig);
        if(netword_profile_id == -1) {
            // Android won't give us the detailed error. The issue may be that the keystore is locked.
            // In modern Android OSs, the only way the keystore can be locked is if the user does
            // not have a pattern, password or PIN. We will call an asynchronous activity to
            // unlock the keystore, but it will actually prompt the user to set up a pattern,
            // password or PIN. After the user does this, the keystore will be unlocked while the
            // pairing service is in use. However, since the activity to set this up is asynchronous,
            // this method will fail, and we will have to wait for our method to be called again
            // so it can actually store the profile. For more details, see:
            // http://nelenkov.blogspot.cl/2012/05/storing-application-secrets-in-androids.html
            Intent unlockIntent = new Intent("com.android.credentials.UNLOCK");
            unlockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(unlockIntent);

            // There is no way the user will be fast enough to set things up, and it is not easy
            // to sleep since we don't know how long it will take him. Therefore, just fail
            // and hope next time we are called the user has its pattern, PIN or password set up.
            String errorMessage = "Wi-Fi configuration could not be stored.";
            Log.e(TAG, errorMessage);
            throw new RuntimeException(errorMessage);
        }
        else {
            // Ensure it is enabled by default, and that this new config has been stored.
            wifiManager.enableNetwork(netword_profile_id, true);
            wifiManager.saveConfiguration();
            Log.v(TAG, "Wi-Fi configuration stored with id " + netword_profile_id);
            return netword_profile_id;
        }
    }

    /**
     * Re-generates the network profile from data stored in local files from a previous pairing process.
     * @param context
     */
    public static void reGenerateProfile(String cloudletName, Context context)
    {
        try {
            AndroidCredentialsManager credentialsManager = new AndroidCredentialsManager();
            String networkId = credentialsManager.loadDataFromFile(cloudletName, SSID_FILE_NAME);
            String serverCertificatePath = credentialsManager.loadDataFromFile(cloudletName, SERVER_CERT_PATH_FILE_NAME);
            String password = credentialsManager.loadDataFromFile(cloudletName, AUTH_PASSWORD_FILE_NAME);

            // Now add it.
            int netId = WifiProfileManager.setupWPA2WifiProfile(networkId, serverCertificatePath,
                    DeviceIdManager.getDeviceId(context), password, context);
            Log.v(TAG, "Wi-Fi profile successfully created.");
        } catch (Exception e) {
            Log.e(TAG, "Error creating Wi-Fi profile.");
            e.printStackTrace();
        }
    }

    /**
     * Removes a given network profile by SSID.
     * @param wifiManager the link to the WiFiManager
     * @param ssid the SSID of the profile to remove.
     * @return true if it was found and removed.
     */
    private static boolean removeNetworkProfile(WifiManager wifiManager, String ssid)
    {
        // First try to remove an existing profile with the same SSID, if any.
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration conf : list)
        {
            if(conf.SSID.equals(ssid))
            {
                Log.v(TAG, "Removing SSID: " + conf.SSID);
                wifiManager.removeNetwork(conf.networkId);
                wifiManager.saveConfiguration();
                return true;
            }
        }

        return false;
    }
}
