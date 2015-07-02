package edu.cmu.sei.cloudlet.client.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

/**
 * Created by Sebastian on 2015-06-04.
 */
public class FileHandler {
    private static final String TAG = "FileHandler";

    public static final String APP_BASE_FOLDER = "/sdcard/cloudlet/";

    public static void writeToFile(String filePath, byte[] data) {
        try {
            // Create the folders.
            Log.v(TAG, "Creating folder for file " + filePath + ".");
            File folders = new File((new File(filePath)).getParent());
            folders.mkdirs();

            // Write the id to the file.
            Log.v(TAG, "Writing to file " + filePath + ".");
            FileOutputStream writer = new FileOutputStream(filePath);
            writer.write(data);
            writer.close();
            Log.v(TAG, "Finished writing to file " + filePath + ".");
        }
        catch(IOException e) {
            Log.e(TAG, "Error writing to file: " + e.toString());
        }
    }

    public static void writeToFile(String filePath, String data) {
        try {
            // Create the folders.
            File folders = new File((new File(filePath)).getParent());
            folders.mkdirs();

            // Write the id to the file.
            Log.v(TAG, "Writing to file " + filePath + ".");
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.print(data);
            writer.close();
            Log.v(TAG, "Finished writing to file " + filePath + ".");
        }
        catch(IOException e) {
            Log.e(TAG, "Error writing to file: " + e.toString());
        }
    }

    public static byte[] readFromFile(String filePath) {
        try {
            // Create the folders if required.
            File folders = new File((new File(filePath)).getParent());
            folders.mkdirs();

            // Load the data.
            Log.v(TAG, "Reading file.");
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            byte[] buffer = new byte[(int) file.length()];
            file.read(buffer);
            Log.v(TAG, "Finished reading file.");

            return buffer;
        }
        catch(IOException e) {
            Log.e(TAG, "Error reading file: " + e.toString());
            return null;
        }
    }
}
