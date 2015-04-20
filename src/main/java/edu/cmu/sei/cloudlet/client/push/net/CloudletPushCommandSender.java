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
package edu.cmu.sei.cloudlet.client.push.net;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.sei.cloudlet.client.net.CloudletCommandException;
import edu.cmu.sei.cloudlet.client.net.ServiceVmCommandSender;

/**
 * Class that handles the protocol to communicate Cloudlet Push commands with a CloudletServer.
 * The protocol supported is that of the Baseline VM Synthesis Prototype.
 * @author secheverria
 *
 */
public class CloudletPushCommandSender extends ServiceVmCommandSender
{
    // Used to identify logging statements.
    private static final String LOG_TAG = CloudletPushCommandSender.class.getName();

    // Commands sent to CloudletServer.
    private static final String HTTP_GET_APPS_LIST = "api/app/getList";
    private static final String HTTP_GET_APP = "api/app/getApp";
    
    /////////////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Constructor.
     */    
    public CloudletPushCommandSender(String cloudletIPAddress, int cloudletPort)
    {
        super(cloudletIPAddress, cloudletPort);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Sends the command to get a list of existing apps in the server.
     * @returns A structure with a list of available apps.
     */
    public String executeGetAppsList() throws CloudletCommandException
    {
        // Add the parameters to the command.
        String commandWithParams = HTTP_GET_APPS_LIST;
        
        // Execute the command.
        String response = sendCommand(commandWithParams);
        
        // Just return the string response.
        return response;        
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Sends the command to get a particular app.
     * @returns A structure with a list of available apps.
     */
    public String executeGetApp(String appName, String apkFileName) throws CloudletCommandException
    {
        // Add the parameters to the command.
        String commandWithParams = HTTP_GET_APP + "?appName=" + appName;
        
        // Execute the command.
        String response = sendCommand(commandWithParams, null, apkFileName);
        
        return response;
    }        

}
