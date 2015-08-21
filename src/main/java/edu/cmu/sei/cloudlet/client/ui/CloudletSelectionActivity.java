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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import edu.cmu.sei.ams.cloudlet.*;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.ams.cloudlet.android.CredentialsManager;
import edu.cmu.sei.ams.cloudlet.android.FindCloudletByRankAsyncTask;
import edu.cmu.sei.ams.cloudlet.android.StartServiceAsyncTask;
import edu.cmu.sei.ams.cloudlet.rank.CpuBasedRanker;
import edu.cmu.sei.cloudlet.client.CloudletReadyApp;
import edu.cmu.sei.cloudlet.client.CurrentCloudlet;
import edu.cmu.sei.cloudlet.client.R;
import edu.cmu.sei.cloudlet.client.synth.models.OverlayInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * User: jdroot
 * Date: 4/18/14
 * Time: 9:49 AM
 */
public class CloudletSelectionActivity extends Activity
{
    private static final String CLOUDLET_OVERLAY_DIR = Environment.getExternalStorageDirectory() + "/cloudlet/overlays";
    private OverlayListAdapter adapter;
    private List<String> serviceObjects;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloudlet_services_list_no_header);

        ListView lv = (ListView) findViewById(R.id.listView);
        serviceObjects = new ArrayList<String>();

        adapter = new OverlayListAdapter(this, R.layout.cloudlet_service_item_no_header, serviceObjects);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                final String item = (String) adapterView.getItemAtPosition(i);
                Log.v("CLOUDLET", "SERVICE VM ID: " + item);
                new FindCloudletByRankAsyncTask(CloudletSelectionActivity.this, item, new CpuBasedRanker(), new CloudletCallback<Cloudlet>()
                {
                    @Override
                    public void handle(Cloudlet result)
                    {
                        if (result == null)
                            Toast.makeText(CloudletSelectionActivity.this, "Failed to find a nearby Cloudlet for the selected service", Toast.LENGTH_LONG).show();
                        else
                        {
                            try
                            {
                                Service service = result.getServiceById(item);
                                new StartServiceAsyncTask(service, CloudletSelectionActivity.this, new CloudletCallback<ServiceVM>()
                                {
                                    @Override
                                    public void handle(ServiceVM result)
                                    {
                                        if (result == null)
                                            Toast.makeText(CloudletSelectionActivity.this, "Failed to start the service", Toast.LENGTH_LONG).show();
                                        else //Service was started
                                        {
                                            CloudletReadyApp app = new CloudletReadyApp(result);
                                            app.start(CloudletSelectionActivity.this);
                                        }
                                    }
                                }).execute();
                            }
                            catch (Exception e)
                            {
                                Log.e("CLOUDLET", "Failed to start the service", e);
                                Toast.makeText(CloudletSelectionActivity.this, "Error occured while starting the service", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).execute();
                //new FindCloudletAsync(item).execute();
            }
        });

        new GetServicesListAsync(this).execute();
    }



    private enum State
    {
        LOOKING_FOR_CLOUDLET,
        GETTING_SERVICE,
        STARTING_SERVICE
    }

    private class FindCloudletAsync extends AsyncTask<Void, Void, Cloudlet>
    {
        private String service;
        private Context context;

        public FindCloudletAsync(String service, Context context)
        {
            this.service = service;
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(CloudletSelectionActivity.this);
            mProgressDialog.setTitle("Cloudlet");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Looking for Cloudlet");
            mProgressDialog.show();
        }

        @Override
        protected Cloudlet doInBackground(Void... voids)
        {
            CloudletFinder finder = new CloudletFinder();
            finder.enableEncryption(CredentialsManager.getDeviceId(this.context), CredentialsManager.loadDataFromFile("password"));
            return finder.findCloudletForService(service, new CpuBasedRanker());
        }

        @Override
        protected void onPostExecute(Cloudlet result)
        {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();


            if (result == null)
                Toast.makeText(CloudletSelectionActivity.this, "Failed to find a nearby Cloudlet for the selected service", Toast.LENGTH_LONG).show();
            else
                new StartServiceAsync(service, result).execute();
        }
    }


    private class StartServiceAsync extends AsyncTask<Void, Void, ServiceVM>
    {
        private String service;
        private Cloudlet cloudlet;

        private StartServiceAsync(String service, Cloudlet cloudlet)
        {
            this.service = service;
            this.cloudlet = cloudlet;
        }

        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(CloudletSelectionActivity.this);
            mProgressDialog.setTitle("Cloudlet: " + cloudlet.getName());
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Starting service");
            mProgressDialog.show();
        }


        @Override
        protected ServiceVM doInBackground(Void... voids)
        {
            try
            {
                Service s = cloudlet.getServiceById(service);
                if (s == null)
                    return null;
                return s.startService();
            }
            catch (CloudletException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ServiceVM result)
        {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();

            if (result == null)
                Toast.makeText(CloudletSelectionActivity.this, "Failed to start the service", Toast.LENGTH_LONG).show();
            else //Service was started
            {
                CloudletReadyApp app = new CloudletReadyApp(result);
                app.start(CloudletSelectionActivity.this);
            }
        }
    }

    private class OverlayListAdapter extends ArrayAdapter<String>
    {
        private int resourceId;
        public OverlayListAdapter(Context context, int textViewResourceId, List<String> objects)
        {
            super(context, textViewResourceId, objects);
            this.resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if( v == null )
            {
                LayoutInflater inflator = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                v = inflator.inflate(resourceId, null);
            }

            final String service = getItem(position);

            TextView idTextView = (TextView)v.findViewById(R.id.idTextView);

            idTextView.setText(service);

            return v;
        }
    }

    private class GetServicesListAsync extends AsyncTask<Void, Void, List<String>>
    {
        private Context context;

        public GetServicesListAsync(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(CloudletSelectionActivity.this);
            mProgressDialog.setTitle("Services");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Looking for nearby services");
            mProgressDialog.show();
        }

        @Override
        protected List<String> doInBackground(Void... voids)
        {
            CloudletFinder finder = new CloudletFinder();
            finder.enableEncryption(CredentialsManager.getDeviceId(this.context), CredentialsManager.loadDataFromFile("password"));
            return finder.findAllNearbyServices();
        }

        @Override
        protected void onPostExecute(List<String> services)
        {
            if (mProgressDialog != null)
                mProgressDialog.dismiss();

            serviceObjects.clear();
            serviceObjects.addAll(services);
            adapter.notifyDataSetChanged();
        }
    }
}
