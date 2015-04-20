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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import edu.cmu.sei.ams.cloudlet.Cloudlet;
import edu.cmu.sei.ams.cloudlet.CloudletFinder;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.ams.cloudlet.android.FindCloudletsAsyncTask;
import edu.cmu.sei.cloudlet.client.R;
import edu.cmu.sei.cloudlet.client.CurrentCloudlet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CloudletDiscoveryActivity extends Activity implements ServiceListener 
{
	public static final int MENU_OPTION_DISCOVER_CLOUDLETS = 1;
	protected ProgressDialog mProgressDialog = null;
	public static final String ASYNC_TASK_STATUS_SUCCESS = "success";
	public static final String ASYNC_TASK_STATUS_FAILURE = "failure";
	private static final String LOG_TAG = "AvailableCloudletsActivity";
	private static final String CLOUDLET_SERVER_DNS = "_cloudlet._tcp.local.";
	List<Cloudlet> cloudlets = null;
	String[] cloudletListData = null;
	ArrayAdapter<String> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

        ClassLoader classLoader = CloudletDiscoveryActivity.class.getClassLoader();
        URL resource = classLoader.getResource("org/apache/http/message/BasicLineFormatter.class");
        Log.v("CLOUDLET", "Class loaded by: " + resource);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloudlet_discovery);	
		Log.d(LOG_TAG, "onCreate");
		TextView tv = (TextView) findViewById(R.id.message);
		tv.setText("List of Available Cloudlets");
		ListView lv = (ListView) findViewById(R.id.cloudlet_list);
		adapter = new ArrayAdapter<String>(this, R.layout.cloudlet_discovery_item);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Invoke OverlayDetailsActivity
				invokeOverlayDetailsActivity(position);
			}
		});
		
		// Run initial discovery.
		runDiscoveryProcess();
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_OPTION_DISCOVER_CLOUDLETS, 0, "Discover Cloudlets");
		return super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		Log.d(LOG_TAG, "onMenuItemSelected");

		switch (item.getItemId()) {

		case MENU_OPTION_DISCOVER_CLOUDLETS:
			runDiscoveryProcess();
			break;
		}
		return true;

	}
	
	private void runDiscoveryProcess()
	{
	    ConnectionInfoFragment connInfoFragment = (ConnectionInfoFragment) getFragmentManager().findFragmentById(R.id.connInfoPanel);
	    connInfoFragment.updateWifiInfo();

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

	private List<Cloudlet> doZeroConfSetup()
	{
        return CloudletFinder.findCloudlets();
	}

	private void invokeOverlayDetailsActivity (int selectedCloudlet) 
	{
		// Get IP address and port of the selected cloudlet
		// It starts at 1 because the character at position 0 is "/"
        Log.v(LOG_TAG, "cloudlets object: " + cloudlets);
        Log.v(LOG_TAG, "cloudlets object: " + cloudlets.size());
	    String selectedCloudletName = cloudlets.get(selectedCloudlet).getName();

		String selectedCloudletIPAddress = cloudlets.get(selectedCloudlet).getAddress().getHostAddress();
		int selectedCloudletPort = cloudlets.get(selectedCloudlet).getPort();

		Log.d(LOG_TAG,"Selected Cloudlet IP Address: " + selectedCloudletIPAddress);
		Log.d(LOG_TAG,"Selected Cloudlet IP Port: " + selectedCloudletPort);
		
		// Store the cloudlet information in a shared static class.
		CurrentCloudlet.name = selectedCloudletName;
        CurrentCloudlet.ipAddress = selectedCloudletIPAddress;
        CurrentCloudlet.port = selectedCloudletPort;
        CurrentCloudlet.cloudlet = cloudlets.get(selectedCloudlet);

		// Invoke the selection Activity.
		Intent i = new Intent(this, ProcessSelectionActivity.class);
		startActivity(i);
	}

	@Override
	public void serviceAdded(ServiceEvent event) {
		Log.d(LOG_TAG,
				"Service added   : " + event.getName() + "." + event.getType());
	}

	@Override
	public void serviceRemoved(ServiceEvent event) {
		Log.d(LOG_TAG,
				"Service removed : " + event.getName() + "." + event.getType());
	}

	@Override
	public void serviceResolved(ServiceEvent event) {
		Log.d(LOG_TAG,
				"Service resolved: " + event.getName() + "." + event.getType()
				+ "\n" + event.getInfo());
	}

	@SuppressWarnings("deprecation")
    private void fillData(List<Cloudlet> cloudlets) {

		if (cloudlets == null || cloudlets.size() == 0) {
			Log.d(LOG_TAG, "No cloudlets nearby");
			adapter.clear();
			
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();  
			Log.d(LOG_TAG, "Displaying alert");
			alertDialog.setTitle("Cloudlet Discovery Failed");  
			alertDialog.setMessage("No Cloudlets in Proximity of this Device");  
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					return;
				}   
			}); 
			alertDialog.show();
		}
		else
		{
            this.cloudlets = cloudlets;
			Log.d(LOG_TAG, "Cloudlets nearby");
			Log.d(LOG_TAG, "Cloudlet Info Length = " + cloudlets.size());
			adapter.clear();
            for (Cloudlet cloudlet : cloudlets)
			{
				Log.d(LOG_TAG,"Name = "+ cloudlet.getName());
				Log.d(LOG_TAG,"IP = "+ cloudlet.getAddress());
				Log.d(LOG_TAG,"Port = " + cloudlet.getPort());
				adapter.add(cloudlet.getName() + ":" +
						cloudlet.getAddress() + ":" +
						cloudlet.getPort());
			}
			
			//adapter.notifyDataSetChanged();
			Log.d(LOG_TAG,"Added data to list adapter");
		}

	}
	
    /**
     * Get IP address from first non-localhost interface
     * @return  address or null if nothing was found.
     */
    @SuppressLint("DefaultLocale")
    public static InetAddress getLocalIPAddress() 
    {
        try 
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) 
			{
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) 
				{
                    if (!addr.isLoopbackAddress()) 
					{
                        String sAddr = addr.getHostAddress().toUpperCase();
                        Log.d(LOG_TAG, "Local IP address found: " + sAddr);
                        return addr;
                    }
                }
            }
        } 
        catch (Exception ex) 
        { 
        	Log.w(LOG_TAG, "Exception looking for local IP address:" + ex.toString());
        } 
		
        Log.w(LOG_TAG, "No valid local IP address found!");
        return null;
    }	
}
