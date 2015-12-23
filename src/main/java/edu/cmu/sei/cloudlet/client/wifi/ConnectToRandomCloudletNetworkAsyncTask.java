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
import android.util.Log;

import java.util.List;

import edu.cmu.sei.ams.cloudlet.android.CloudletAsyncTask;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;

/**
 * Created by Sebastian on 2015-12-22.
 */
public class ConnectToRandomCloudletNetworkAsyncTask  extends CloudletAsyncTask<Boolean>
{
    private static final String LOG_TAG = "ConnectNetworkAsyncTask";

    private static final String TITLE = "Connecting";
    private static final String MESSAGE = "Connecting to cloudlet network...";

    /**
     * Constructor
     * @param context
     * @param callback
     * @param ssid
     */
    public ConnectToRandomCloudletNetworkAsyncTask(Context context, CloudletCallback<Boolean> callback)
    {
        super(context, callback, TITLE, MESSAGE);
    }

    /**
     * What the task actually does, which is to connect to a newtork.
     * @param params
     * @return
     */
    @Override
    protected Boolean doInBackground(Void... params)
    {
        try
        {
            // Find the valid networks.
            CloudletNetworkFinder finder = new CloudletNetworkFinder(mContext);
            List<CloudletNetwork> networks = finder.findNetworks();

            // Connect to first one in list.
            if(networks.size() > 0)
            {
                return networks.get(0).connect(mContext);
            }

            return false;
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG, "Error connecting to cloudlet network: ", e);
            this.mException = e;
            return false;
        }
    }
}