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
package edu.cmu.sei.cloudlet.client.push.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.sei.cloudlet.client.R;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InstalledApplcationListActivity extends Activity{

	private ListView listView;
	private List<ApplicationInfo> appList;
	private HashMap<ApplicationInfo, String> appNameMap;
	private HashMap<ApplicationInfo, Drawable> appIconMap;
	private ProgressBar progressBar;
	private MyInstalledAppAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.installed_apps_list);

		listView = (ListView)findViewById(R.id.listView);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);

		appNameMap = new HashMap<ApplicationInfo, String>();
		appIconMap = new HashMap<ApplicationInfo, Drawable>();
		appList = new ArrayList<ApplicationInfo>();
		
		listView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				File file = new File( appList.get(arg2).packageName );
				Toast.makeText(InstalledApplcationListActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			}
		});
		
		new LoadAppsAsyncTask().execute();

	}
	
	class LoadAppsAsyncTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			progressBar.setVisibility(View.GONE);
			adapter = new MyInstalledAppAdapter(InstalledApplcationListActivity.this, R.layout.installed_app_item, appList);
			listView.setAdapter(adapter);
		}
		

		@Override
		protected Void doInBackground(Void... params) {
			List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
			for (ApplicationInfo packageInfo : packages) {
				if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
				{
					appList.add(packageInfo);
					appNameMap.put(packageInfo, packageInfo.loadLabel(getPackageManager()).toString());
					appIconMap.put(packageInfo, packageInfo.loadIcon(getPackageManager()));
				}
			}
			return null;
		}
		
	}

	class MyInstalledAppAdapter extends ArrayAdapter<ApplicationInfo>{
		public MyInstalledAppAdapter(Context context, int textViewResourceId,List<ApplicationInfo> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if( v == null ){
				LayoutInflater inflator = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				v = inflator.inflate(R.layout.installed_app_item, null);
			}

			ApplicationInfo appInfo = appList.get(position);
			ImageView imageView = (ImageView)v.findViewById(R.id.app_icon);
			Drawable icon = appIconMap.get(appInfo);
			if(icon!=null)
				imageView.setImageDrawable(icon);
			else
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
			TextView textview = (TextView)v.findViewById(R.id.app_name);
			String label = appNameMap.get(appInfo);
			if(label!=null)
				textview.setText(label);
			else
				textview.setText("null");
			return v;
		}

	}

}
