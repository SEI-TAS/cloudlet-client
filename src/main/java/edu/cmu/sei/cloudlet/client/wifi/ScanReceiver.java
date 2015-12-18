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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for new scan results about cloudlet networks.
 * Created by Sebastian on 2015-12-18.
 */
public class ScanReceiver extends BroadcastReceiver
{
    IScanResultsHandler handler;

    /**
     * Basic constructor.
     * @param handler The handler to call when results are received.
     */
    public ScanReceiver(IScanResultsHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Process the results of a scan, and returns a list of valid Cloudlet networks.
     * @param wifiManager the Android WifiManager object.
     * @return A list of seemingly valid Cloudlet networks.
     */
    protected List<CloudletNetwork> getScanResults(WifiManager wifiManager)
    {
        List<CloudletNetwork> networks = new ArrayList<CloudletNetwork>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for(ScanResult result : scanResults)
        {
            if(CloudletNetwork.isValidNetwork(result.SSID))
            {
                CloudletNetwork network = new CloudletNetwork(result.SSID);
                networks.add(network);
            }
        }

        return networks;
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
            // Get the valid networks and notify the handler.
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<CloudletNetwork> results = getScanResults(wifiManager);
            handler.handleScanResults(results);
        }
    }
}
