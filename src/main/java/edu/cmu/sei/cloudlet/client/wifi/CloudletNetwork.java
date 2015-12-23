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
package edu.cmu.sei.cloudlet.client.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Represents a cloudlet network.
 */
public class CloudletNetwork
{
    private static final String LOG_TAG = "CloudletNetwork";

    // Prefix to identify a valid network.
    public static final String PREFIX = "cloudlet-";

    // The network's ssid.
    private String ssid;

    // The cloudlet network name.
    private String name;

    /**
     * Checks if a given network SSID follows the pattern for cloudlet networks.
     * @param ssid the SSID to check
     * @return true if it is valid, false otherwise.
     */
    public static boolean isValidNetwork(String ssid)
    {
        return ssid.startsWith(PREFIX);
    }

    /**
     * Constructor
     * @param ssid network SSID.
     */
    public CloudletNetwork(String ssid)
    {
        this.ssid = ssid;

        // Remove the prefix.
        name = ssid.substring(PREFIX.length());
    }

    // Getter
    public String getSSID()
    {
        return ssid;
    }

    // Getter
    public String getName()
    {
        return name;
    }

    /**
     * Attempts to connect to this network, if it was known.
     * @param context the Android context.
     * @return
     */
    public boolean connect(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        String quotedSSID = "\"" + ssid + "\"";
        List<WifiConfiguration> savedNetworks = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration config : savedNetworks)
        {
            if(quotedSSID.equals(config.SSID))
            {
                // Actually connect to the network.
                Log.v(LOG_TAG, "Attempting connection to known network " + quotedSSID);
                boolean disableOtherNetworks = true;
                return wifiManager.enableNetwork(config.networkId, disableOtherNetworks);
            }
        }

        // Network is not known.
        Log.w(LOG_TAG, "Network was not previously known: " + quotedSSID);
        return false;
    }
}