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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * Class used to store information about an instance of a Service VM running on a cloudlet.
 * 
 * @author secheverria
 * 
 */
public class ServiceVMInstance
{
    public static final String LOG_TAG = ServiceVMInstance.class.getName();

    // Keys to get information from a JSON object.
    public static final String IP_ADDRESS_KEY = "IP_ADDRESS";
    public static final String PORT_KEY = "PORT";
    public static final String INSTANCE_ID_KEY = "INSTANCE_ID";

    // The connection information of the ServiceVM, plus the ID of the instance.
    protected String ipAddress;
    protected int port;
    protected String instanceId;

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Constructor.
     * @param jsonObj A Json object with the data.
     */
    public ServiceVMInstance(final JSONObject jsonObj)
    {
        if (jsonObj != null)
        {
            try
            {
                this.ipAddress = jsonObj.getString(IP_ADDRESS_KEY);
                this.port = jsonObj.getInt(PORT_KEY);
                this.instanceId = jsonObj.getString(INSTANCE_ID_KEY);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String getIpAddress()
    {
        return ipAddress;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public int getPort()
    {
        return port;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String getInstanceId()
    {
        return instanceId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("ServiceVMInstanceInfo ->").append("[").append("IP: ")
                .append(ipAddress != null ? ipAddress : "null")
                .append(" , PORT: ").append(port).append("]")
                .append(" , INSTANCE_ID: ").append(instanceId).append("]");        
        return buf.toString();
    }
}
