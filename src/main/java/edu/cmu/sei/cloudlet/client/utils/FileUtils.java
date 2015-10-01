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
package edu.cmu.sei.cloudlet.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

/**
 * Utility class for dealing with files. 
 * @author ssimanta
 *
 */
public class FileUtils {


	public static final String LOG_TAG = FileUtils.class.getName();
	
	public static String parseDataFileToString(final String fileName) {
		try {
			final File file = new File(fileName);
			InputStream stream = new FileInputStream(file);

			int size = stream.available();
			byte[] bytes = new byte[size];
			stream.read(bytes);
			stream.close();

			return new String(bytes);

		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException in reading data file  " + fileName + " \n" + e.getMessage());
		}
		return null;
	}
	
	public static boolean writeStringtoDataFile(final String contents,
			final String fileName) {
		// First create the folder structure if necessary.
		File fileToWrite = new File(fileName);
		fileToWrite.mkdirs();		
		
		FileWriter fileWriter = null;
		boolean success = false; 
		try {
			fileWriter = new FileWriter(fileName);

			if (fileWriter != null) {
				fileWriter.write(contents);
				fileWriter.flush();
				fileWriter.close();
				success = true; 
			}

			
		} catch (FileNotFoundException e1) {
			Log.e(LOG_TAG, "File not found " + fileName + " \n" + e1.getMessage());
			e1.printStackTrace();


		} catch (IOException ioe) {
			Log.e(LOG_TAG, "IOException while writing to file -> " + fileName + " \n" + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		return success; 
	}

}
