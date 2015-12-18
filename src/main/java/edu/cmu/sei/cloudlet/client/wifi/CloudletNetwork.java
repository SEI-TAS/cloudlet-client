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
package edu.cmu.sei.cloudlet.client.wifi;

/**
 * Represents a cloudlet network.
 */
public class CloudletNetwork
{
    // Prefix to identify a valid network.
    public static final String PREFIX = "cloudlet-";

    // The cloudlet network name.
    private String name;

    /**
     * Checks if a given network SSID follows the pattern for cloudlet networks.
     * @param ssid the SSID to check
     * @return true if it is valid, false otherwise.
     */
    public static boolean isValidNetwork(String ssid)
    {
        return ssid.startsWith(PREFIX);
    }

    /**
     * Constructor
     * @param ssid network SSID.
     */
    public CloudletNetwork(String ssid)
    {
        // Remove the prefix.
        name = null;
        String[] parts = ssid.split("-");
        if(parts.length > 1)
        {
            name = parts[1];
        }
    }

    // Getter
    public String getName()
    {
        return name;
    }
}