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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds valid cloudlet networks.
 * Created by Sebastian on 2015-12-11.
 */
public class CloudletNetworkFinder extends BroadcastReceiver
{
    Context context;

    boolean registered = false;
    boolean scanFinished = false;
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
     * Getter.
     * @return The current list of valid networks last found.
     */
    public List<CloudletNetwork> getNetworks()
    {
        return networks;
    }

    /**
     * Checks if a given scan has finished.
     * @return true if it has finished.
     */
    public boolean hasScanFinished()
    {
        return scanFinished;
    }

    /**
     * Synch method to block till networks are found.
     * @return
     */
    public List<CloudletNetwork> findNetworks()
    {
        findNetworksAsync();

        // Wait till results are obtained.
        // TODO: add timeout.
        while(!hasScanFinished())
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return networks;
    }

    /**
     * Stars and asyncrhonous scan for Wi-Fi networks.
     * @return true if the scan was started, false if the scan could not be started.
     */
    public boolean findNetworksAsync()
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
        scanFinished = false;
        return wifiManager.startScan();
    }

    /**
     * Registers a wifi scan receiver.
     */
    private void registerScanReceiver()
    {
        context.registerReceiver(this, new IntentFilter(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registered = true;
    }

    /**
     * Unregisters a wifi scan receiver.
     */
    private void unregisterScanReceiver()
    {
        if (registered)
        {
            try
            {
                context.unregisterReceiver(this);
                registered = false;
            }
            catch(IllegalArgumentException exception)
            {
                Log.w("CloudletNetworkFinder", "We tried to unregister an unregistered scan receiver: " + exception.toString());
            }
        }
    }

    /**
     * Called when there is a new WiFi event.
     * @param context the Android context.
     * @param intent the Intent with the event.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Filter new scan results intents.
        String action = intent.getAction();
        boolean scanResultsAvailable = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action);
        if(scanResultsAvailable)
        {
            // Get all results.
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();

            // Filter valid networks.
            List<CloudletNetwork> newNetworks = new ArrayList<CloudletNetwork>();
            for(ScanResult result : scanResults)
            {
                if(CloudletNetwork.isValidNetwork(result.SSID))
                {
                    CloudletNetwork network = new CloudletNetwork(result.SSID);
                    newNetworks.add(network);
                }
            }

            // Unregister scan receiver, we got the results.
            unregisterScanReceiver();

            // Make the results available.
            networks = newNetworks;
            scanFinished = true;
        }
    }
}