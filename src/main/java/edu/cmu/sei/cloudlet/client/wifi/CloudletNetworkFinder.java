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
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;


/**
 * Created by Sebastian on 2015-12-11.
 */
public class CloudletNetworkFinder implements IScanResultsHandler
{
    Context context;

    ScanReceiver scanReceiver;
    List<CloudletNetwork> networks;

    /**
     * Constructor, store the context.
     * @param context the current Android context.
     */
    public CloudletNetworkFinder(Context context)
    {
        this.context = context;
    }

    /**
     * Stars and asyncrhonous scan for Wi-Fi networks.
     * @return true if the scan was started, false if the scan could not be started.
     */
    public boolean findNetworks()
    {
        // Check that we have a Wi-Fi card.
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))
        {
            return false;
        }

        // Check Wi-Fi is enabled.
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }

        // Register the broadcast receiver.
        registerScanReceiver();

        // Start the async scan.
        networks = null;
        return wifiManager.startScan();
    }

    /**
     * Handles a list of cloudlet networks.
     * @param results A list of cloudlet networks.
     */
    @Override
    public void handleScanResults(List<CloudletNetwork> results)
    {
        // Unregister scan receiver, we got the results.
        unregisterScanReceiver();

        // Make the results available.
        networks = results;
    }

    /**
     * Registers a wifi scan receiver.
     */
    private void registerScanReceiver()
    {
        if (scanReceiver == null)
        {
            scanReceiver = new ScanReceiver(this);
        }

        context.registerReceiver(scanReceiver, new IntentFilter(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    /**
     * Unregisters a wifi scan receiver.
     */
    private void unregisterScanReceiver()
    {
        if (scanReceiver != null)
        {
            try
            {
                context.unregisterReceiver(scanReceiver);
            }
            catch(IllegalArgumentException exception)
            {
                Log.w("CloudletNetworkFinder", "We tried to unregister an unregistered scan receiver: " + exception.toString());
            }
        }
    }
}