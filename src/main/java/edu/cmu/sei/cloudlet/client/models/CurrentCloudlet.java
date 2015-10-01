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

import edu.cmu.sei.ams.cloudlet.Cloudlet;

/**
 * 
 * This is a in-memory class used to store the cloudlet information.
 * It has public static members so the information about the currently selected
 * cloudlet can be shared easily between activities.
 * 
 * @author ssimanta, secheverria
 * 
 */
public class CurrentCloudlet
{
    public static Cloudlet cloudlet;

    // The IP and port. Static so that it can be easily shared between activities.
    public static String name = "";
    public static String ipAddress = "";
    public static int port = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Checks if the current cloudlet info is valid.
     * @return true if it is valid, false if not.
     */
    public static boolean isValid()
    {
        return !name.equals("") && !ipAddress.equals("") && !(port == 0);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the cloudlet info as a string.
     */    
    public static String getAsString()
    {
        return "[Name: " + name + ", IP: " + ipAddress + " , PORT: " + port + "]";
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns an HTTP URI to connect to the cloudlet.
     * @return
     */
    public static String getHttpURI()
    {
        return "http://" + ipAddress + ":" + port + "/";
    }
}
