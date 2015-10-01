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
package edu.cmu.sei.cloudlet.client.ui;

import edu.cmu.sei.cloudlet.client.R;
import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Displays information about the current connection and status.
 * @author secheverria
 *
 */
public class ConnectionInfoFragment extends Fragment
{
    // Used to identify logging statements.
    private static final String LOG_TAG = ConnectionInfoFragment.class.getName();
    
    // Wi-Fi connection info.
    private TextView wifiSsidText;       
    private TextView wifiIpText;    
    private String wifiSsid;
    private String wifiIpAddress;
    
    // Selected cloudlet info.
    private TextView cloudletNameText;       
    private TextView cloudletIpText;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        // Inflate the layout for this fragment.        
        View inflatedView = inflater.inflate(R.layout.connection_info, container, false);
        
        // Wi-Fi info.
        wifiSsidText = (TextView) inflatedView.findViewById(R.id.wifiSsid);       
        wifiIpText = (TextView) inflatedView.findViewById(R.id.wifiIp);
        updateConnectionInfo();

        return inflatedView;
    }
    
    public void onResume() 
    {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        
        updateConnectionInfo();
    }
    
    public void onWindowFocusChanged (boolean hasFocus)
    {
        if(hasFocus)
        {
            updateConnectionInfo();
        }
    }    
    
    /**
     * Loads the SSID of the Wi-Fi network we are currently connected to, as well as the IP address
     * associated to that connection.
     */
    public void updateConnectionInfo()
    {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        
        if(info == null)
        {
            Log.w(LOG_TAG, "Not connected to Wi-Fi.");
            wifiSsid = "disconnected";
            wifiIpAddress = "disconnected";
            return;
        }

        wifiSsid = info.getSSID();
        int ipAddress = info.getIpAddress();
        
        String ip = String.format("%d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));
        
        wifiIpAddress = ip;
        
        // Update the UI.
        refreshConnectionUIText();
    }
    
    /**
     * Refreshes the GUI to show the current information about the Wi-Fi connection.
     */
    private void refreshConnectionUIText()
    {
        wifiSsidText.setText(wifiSsid);
        wifiIpText.setText(wifiIpAddress);
    }

    /**
     * Returns the Wi-Fi IP address.
     * @return
     */
    public String getWifiIpAddress()
    {
        return wifiIpAddress;
    }
}
