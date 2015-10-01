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
package edu.cmu.sei.cloudlet.client.models;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.sei.cloudlet.client.utils.FileUtils;

import android.util.Log;

/**
 * Class that stores the metadata information about a Server VM which a cloudlet-ready app will connect to.
 * @author secheverria
 *
 */
public class ServiceVMMetadata
{
    public static final String LOG_TAG = ServiceVMMetadata.class.getName();

    public static final String SERVICE_VM_DATA_KEY = "serviceVMData";
    public static final String SERVICE_ID_KEY = "serviceId";
    public static final String SERVICE_PORT_KEY = "servicePort";
    public static final String SERVICE_REF_IMAGE_ID_KEY = "refImageId";

    protected String serviceId;
    protected int servicePort;
    protected String refImageId = "None";

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public ServiceVMMetadata(final JSONObject jsonObj)
    {
        loadFromJson(jsonObj);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public ServiceVMMetadata(final String filePath)
    {
        loadFromFile(filePath);
    }       

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String getServiceId()
    {
        return serviceId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public int getPort()
    {
        return servicePort;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String getRefImageId()
    {
        return refImageId;
    }    

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String toString()
    {
        return writeToJson().toString();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////    
    private void loadFromJson(final JSONObject rootJSONObject)
    {
        if (rootJSONObject != null)
        {
            try
            {
                JSONObject serviceVmJSONObject = rootJSONObject.getJSONObject(SERVICE_VM_DATA_KEY);
                
                if(serviceVmJSONObject != null)
                {
                    this.serviceId = serviceVmJSONObject.getString(SERVICE_ID_KEY);
                    this.servicePort = serviceVmJSONObject.getInt(SERVICE_PORT_KEY);
                    this.refImageId = serviceVmJSONObject.getString(SERVICE_REF_IMAGE_ID_KEY);
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }        
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public JSONObject writeToJson()
    {
        // Create a new JSON object and put the data there
        JSONObject rootJSONObj = new JSONObject();
        try
        {
            JSONObject serviceVmJSONObject = new JSONObject();
            rootJSONObj.put(SERVICE_VM_DATA_KEY, serviceVmJSONObject);
            
            serviceVmJSONObject.put(SERVICE_ID_KEY, serviceId);
            serviceVmJSONObject.put(SERVICE_PORT_KEY, servicePort);
            serviceVmJSONObject.put(SERVICE_REF_IMAGE_ID_KEY, refImageId);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return rootJSONObj;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public boolean writeToFile(String fileName)
    {
        String jsonString = writeToJson().toString();
        return FileUtils
                .writeStringtoDataFile(jsonString, fileName);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public boolean loadFromFile(String fileName)
    {
        if (fileName == null)
        {
            Log.e(LOG_TAG, "Input file name to loadFromFile is null. ");
            return false;
        }

        // Check if the file exists.
        File file = new File(fileName);
        if (!file.exists())
        {
            Log.e(LOG_TAG, "Input file [" + fileName + "] to doesn't exist.");
            return false;
        }

        // Load the text.
        String fileContents = FileUtils.parseDataFileToString(fileName);
        if (fileContents == null || fileContents.trim().length() == 0)
        {
            Log.e(LOG_TAG, "Input file [" + fileName + "] is empty.");
            return false;
        }

        // Turn into a JSON structure, and load from there.
        try
        {
            JSONObject rootJSONObject = new JSONObject(fileContents);
            loadFromJson(rootJSONObject);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // if you got here all is well.
        return true;
    }
}
