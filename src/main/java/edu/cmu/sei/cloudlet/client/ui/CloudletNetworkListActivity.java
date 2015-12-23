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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.sei.ams.cloudlet.android.CloudletAsyncTask;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.cloudlet.client.R;
import edu.cmu.sei.ams.cloudlet.android.wifi.CloudletNetwork;
import edu.cmu.sei.ams.cloudlet.android.wifi.CloudletNetworkFinder;
import edu.cmu.sei.ams.cloudlet.android.wifi.ConnectToCloudletNetworkAsyncTask;

public class CloudletNetworkListActivity extends Activity
{
    private static final String LOG_TAG = "CloudletNetworkListAct";
    public static final int MENU_OPTION_DISCOVER_CLOUDLET_NETWORKS = 1;

    private ArrayAdapter<String> listAdapter;

    /**
     * Task to find cloudlet networks asynchronously.
     */
    public class FindCloudletNetworksAsyncTask extends CloudletAsyncTask<List<CloudletNetwork>>
    {
        private static final String TITLE = "Cloudlet";
        private static final String MESSAGE = "Searching for Cloudlet Networks...";

        public FindCloudletNetworksAsyncTask(Context context, CloudletCallback<List<CloudletNetwork>> callback)
        {
            super(context, callback, TITLE, MESSAGE);
        }

        @Override
        protected List<CloudletNetwork> doInBackground(Void... params)
        {
            try
            {
                Log.i("FindCloudletsAsyncTask", "Finding cloudlet networks");

                // Find the networks.
                CloudletNetworkFinder finder = new CloudletNetworkFinder(CloudletNetworkListActivity.this);
                return finder.findNetworks();
            }
            catch(Exception e)
            {
                Log.e("FindCloudletsAsyncTask", "Error finding cloudlet networks: ", e);
                this.mException = e;
                return new ArrayList<CloudletNetwork>();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloudlet_network_list);

        ListView lv = (ListView) findViewById(R.id.cloudlet_network_list);
        listAdapter = new ArrayAdapter<String>(this, R.layout.cloudlet_discovery_item);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String ssid = (String) adapter.getItemAtPosition(position);
                ConnectToCloudletNetworkAsyncTask task = new ConnectToCloudletNetworkAsyncTask(CloudletNetworkListActivity.this, ssid, new CloudletCallback<Boolean>()
                {
                    @Override
                    public void handle(Boolean result)
                    {
                        if(result)
                        {
                            Toast.makeText(CloudletNetworkListActivity.this, "Connected to network.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(CloudletNetworkListActivity.this, "Problem connecting to network.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                task.execute();
            }
        });

        // Initial search.
        runDiscoveryProcess();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_OPTION_DISCOVER_CLOUDLET_NETWORKS, 0, "Discover Cloudlet Networks");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        Log.d(LOG_TAG, "onMenuItemSelected");

        switch (item.getItemId())
        {
            case MENU_OPTION_DISCOVER_CLOUDLET_NETWORKS:
                runDiscoveryProcess();
                break;
        }

        return true;
    }

    /**
     * Looks for cloudlet networks.
     */
    private void runDiscoveryProcess()
    {
        FindCloudletNetworksAsyncTask task = new FindCloudletNetworksAsyncTask(this, new CloudletCallback<List<CloudletNetwork>>()
        {
            @Override
            public void handle(List<CloudletNetwork> cloudletNetworks)
            {
                fillData(cloudletNetworks);
            }
        });

        task.execute();
    }

    /***
     * Adds data of the given cloudlet networks to the array adapter for the visual list.
     * @param cloudletNetworks A list of cloudlet networks found.
     */
    @SuppressWarnings("deprecation")
    private void fillData(List<CloudletNetwork> cloudletNetworks)
    {
        listAdapter.clear();

        if (cloudletNetworks == null || cloudletNetworks.size() == 0) {
            Log.d(LOG_TAG, "No cloudlet networks nearby");

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            Log.d(LOG_TAG, "Displaying alert");
            alertDialog.setTitle("Cloudlet Network Discovery Failed");
            alertDialog.setMessage("No Cloudlet Networks in proximity of this device");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialog.show();
        }

        Log.d(LOG_TAG, "Cloudlets nearby");
        Log.d(LOG_TAG, "Cloudlet Info Length = " + cloudletNetworks.size());
        for (CloudletNetwork cloudletNetwork : cloudletNetworks)
        {
            String descriptor = cloudletNetwork.getSSID();
            Log.d(LOG_TAG, descriptor);
            listAdapter.add(descriptor);
        }

        Log.d(LOG_TAG, "Added data to list adapter");
    }
}
