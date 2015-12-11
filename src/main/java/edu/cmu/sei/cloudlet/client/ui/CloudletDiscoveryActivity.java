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

import java.util.List;

import edu.cmu.sei.ams.cloudlet.Cloudlet;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.ams.cloudlet.android.FindCloudletsAsyncTask;
import edu.cmu.sei.cloudlet.client.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CloudletDiscoveryActivity extends Activity
{
	public static final int MENU_OPTION_DISCOVER_CLOUDLETS = 1;
	private static final String LOG_TAG = "CloudletDiscoveryAct";
	ArrayAdapter<String> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        Log.d(LOG_TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloudlet_discovery);

		TextView tv = (TextView) findViewById(R.id.message);
		tv.setText("List of Available Cloudlets");

		ListView lv = (ListView) findViewById(R.id.cloudlet_list);
		adapter = new ArrayAdapter<String>(this, R.layout.cloudlet_discovery_item);
		lv.setAdapter(adapter);
		
		// Run initial discovery.
		runDiscoveryProcess();
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
		menu.add(0, MENU_OPTION_DISCOVER_CLOUDLETS, 0, "Discover Cloudlets");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
		Log.d(LOG_TAG, "onMenuItemSelected");

		switch (item.getItemId())
        {
            case MENU_OPTION_DISCOVER_CLOUDLETS:
                runDiscoveryProcess();
                break;
		}

		return true;
	}

    /**
     * Starts an async task to find cloudlets and add them to the visual list when done.
     * Also updates the connection info in the connection fragment (current network and IP).
     */
	private void runDiscoveryProcess()
	{
	    ConnectionInfoFragment connInfoFragment = (ConnectionInfoFragment) getFragmentManager().findFragmentById(R.id.connInfoPanel);
	    connInfoFragment.updateConnectionInfo();

        FindCloudletsAsyncTask task = new FindCloudletsAsyncTask(this, new CloudletCallback<List<Cloudlet>>()
        {
            @Override
            public void handle(List<Cloudlet> cloudlets)
            {
                fillData(cloudlets);
            }
        });

        task.execute();
	}

    /***
     * Adds data of the given cloudlets to the array adapter for the visual list.
     * @param cloudlets A list of cloudlets found.
     */
	@SuppressWarnings("deprecation")
    private void fillData(List<Cloudlet> cloudlets)
    {
        adapter.clear();

		if (cloudlets == null || cloudlets.size() == 0) {
            Log.d(LOG_TAG, "No cloudlets nearby");

			AlertDialog alertDialog = new AlertDialog.Builder(this).create();  
			Log.d(LOG_TAG, "Displaying alert");
			alertDialog.setTitle("Cloudlet Discovery Failed");  
			alertDialog.setMessage("No Cloudlets in proximity of this device");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					return;
				}   
			}); 
			alertDialog.show();
		}
		else
		{
			Log.d(LOG_TAG, "Cloudlets nearby");
			Log.d(LOG_TAG, "Cloudlet Info Length = " + cloudlets.size());
            for (Cloudlet cloudlet : cloudlets)
			{
				String encryptionState = cloudlet.isEncryptionEnabled() ? "enabled" : "disabled";
                String descriptor = cloudlet.getName() + ":" +
                                    cloudlet.getAddress() + ":" +
                                    cloudlet.getPort() + " (encryption " + encryptionState + ")";
				Log.d(LOG_TAG, descriptor);
				adapter.add(descriptor);
			}
			
			Log.d(LOG_TAG,"Added data to list adapter");
		}
	}
}
