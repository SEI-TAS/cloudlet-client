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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import edu.cmu.sei.ams.cloudlet.App;
import edu.cmu.sei.ams.cloudlet.AppFilter;
import edu.cmu.sei.ams.cloudlet.android.AppFinderAsyncTask;
import edu.cmu.sei.ams.cloudlet.android.CloudletCallback;
import edu.cmu.sei.ams.cloudlet.android.DownloadAppAsyncTask;
import edu.cmu.sei.cloudlet.client.R;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jdroot
 * Date: 9/15/14
 * Time: 2:39 PM
 */
public class AppListActivity extends Activity
{
    private static XLogger log = XLoggerFactory.getXLogger(AppListActivity.class);

    private final List<App> apps = new ArrayList<App>();
    private AppRowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_cloudlet_apps_list);

        final TextView searchBox = (TextView) findViewById(R.id.app_search_textbox);
        final Button searchButton = (Button)findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AppFilter filter = new AppFilter();
                filter.osName = "Android";
                filter.osVersion = Build.VERSION.RELEASE;
                String searchText = searchBox.getText().toString();
                if (searchText.length() > 0)
                {
                    String[] tags = searchText.split(" ");
                    for (String tag : tags){
                        filter.tags.add(tag);
                    }
                }
                new AppFinderAsyncTask(AppListActivity.this, filter, new CloudletCallback<List<App>>()
                {
                    @Override
                    public void handle(List<App> apps)
                    {
                        AppListActivity.this.apps.clear();
                        AppListActivity.this.apps.addAll(apps);
                        adapter.notifyDataSetChanged();
                    }
                }).execute();
            }
        });
        adapter = new AppRowAdapter(this, R.layout.new_cloudlet_app_item, apps);
        final ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    private boolean isInstalled(App app)
    {
        String appPackage = app.getPackageName();

        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
        {
            boolean notSystemApp = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
            if(notSystemApp)
            {
                if(packageInfo.packageName.equals(appPackage))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static File getStorageDirectory()
    {
        final File directory = new File(Environment.getExternalStorageDirectory() +"/downloaded_apks/");
        if (!directory.exists())
            directory.mkdirs();
        return directory;
    }

    private boolean startApp(App app)
    {
        if (isInstalled(app))
        {
            Intent i = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            startActivity(i);
            return true;
        }
        return false;
    }

    private class AppRowAdapter extends ArrayAdapter<App>
    {

        public AppRowAdapter(Context context, int textViewResourceId,
                             List<App> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if( v == null )
            {
                LayoutInflater inflator = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                v = inflator.inflate(R.layout.new_cloudlet_app_item, null);
            }

            final App app = getItem(position);

            final TextView name = (TextView)v.findViewById(R.id.nameTextView);
            final TextView desc = (TextView)v.findViewById(R.id.descriptionTextView);
            final TextView minVersion = (TextView)v.findViewById(R.id.minRequiredVersionTextView);
            final TextView tags = (TextView)v.findViewById(R.id.tagsTextView);
            final TextView md5 = (TextView)v.findViewById(R.id.sha1hashTextView);

            name.setText(app.getName());
            desc.setText(app.getDescription());
            minVersion.setText(app.getMinAndroidVersion());
            String tagsText = "";

            for (String tag: app.getTags())
            {
                if (tagsText.length() != 0)
                    tagsText += ", ";
                tagsText += tag;
            }
            tags.setText(tagsText);
            md5.setText(app.getMD5Sum());

            final Button downloadButton = (Button)v.findViewById(R.id.download_app_button);
            downloadButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (!startApp(app))
                    {
                        new DownloadAppAsyncTask(AppListActivity.this, app, getStorageDirectory(), new CloudletCallback<File>()
                        {
                            @Override
                            public void handle(File file)
                            {
                                if(file != null)
                                {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                    startActivityForResult(intent, 5000);
                                }
                                else
                                {
                                    log.error("Error installing APK file: file was not found.");
                                }
                            }
                        }).execute();
                    }
                }
            });

            return v;
        }
    }
}
