package edu.cmu.sei.cloudlet.client.ska.adb;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import edu.cmu.sei.cloudlet.client.ibc.IBCRepoManager;

/**
 * Created by Sebastian on 2015-06-04.
 */
public class FileHandler {
    private static final String TAG = "StoreCertificateService";

    public static final String FILES_PATH = "/sdcard/cloudlet/adb/";

    public static void writeToFile(String filePath, String data) {
        try {
            // Create the folders.
            File folders = new File(FileHandler.FILES_PATH);
            folders.mkdirs();

            // Write the id to the file.
            Log.v(TAG, "Writing to file.");
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.print(data);
            writer.close();
            Log.v(TAG, "Finished writing to file.");
        }
        catch(IOException e) {
            Log.e(TAG, "Error reading file: " + e.toString());
        }
    }

    public static byte[] readFromFile(String filePath) {
        try {
            // Create the folders if required.
            File folders = new File(FileHandler.FILES_PATH);
            folders.mkdirs();

            // Load the data.
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            byte[] buffer = new byte[(int) file.length()];
            file.read(buffer);

            return buffer;
        }
        catch(IOException e) {
            Log.e(TAG, "Error writing file: " + e.toString());
            return null;
        }
    }
}
