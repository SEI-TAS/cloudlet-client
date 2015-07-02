package edu.cmu.sei.cloudlet.client.ibc;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.net.wifi.WifiEnterpriseConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.cmu.sei.cloudlet.client.utils.FileHandler;

/**
 * Assumes device can store information of only 1 cloudlet at one time.
 * Created by Sebastian on 2015-05-27.
 */
public class IBCAuthManager {
    private static final String TAG = "IBCAuthManager";

    private static final String IBC_FOLDER_PATH = FileHandler.APP_BASE_FOLDER + "ibc/";

    // TODO: see how to make this generic, even though it still can be used later when needed.
    public static final String SERVER_CERTIFICATE_PATH = IBC_FOLDER_PATH + "server_certificate.cer";
    public static final String DEVICE_CERTIFICATE_PATH = IBC_FOLDER_PATH + "device_certificate.cer";
    public static final String DEVICE_PRIVATE_KEY_PATH = IBC_FOLDER_PATH + "device_key.prv";
    public static final String SERVER_PUBLIC_KEY_PATH = IBC_FOLDER_PATH + "server_key.pub";

    public static final String SERVER_PUBLIC_KEY_ID = "server_public_key";
    public static final String DEVICE_PRIVATE_KEY_ID = "device_private_key";
    public static final String SERVER_CERTIFICATE_ID = "server_certificate";
    public static final String DEVICE_CERTIFICATE_ID = "device_certificate";

    private static HashMap<String, String> mFiles = new HashMap<String, String>();
    static {
        mFiles.put(SERVER_PUBLIC_KEY_ID, SERVER_PUBLIC_KEY_PATH);
        mFiles.put(DEVICE_PRIVATE_KEY_ID, DEVICE_PRIVATE_KEY_PATH);
        mFiles.put(SERVER_CERTIFICATE_ID, SERVER_CERTIFICATE_PATH);
        mFiles.put(DEVICE_CERTIFICATE_ID, DEVICE_CERTIFICATE_PATH);
    }

    /**
     * Returns the ID for the device.
     * @return a String representing a unique id for the device.
     */
    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    /**
     * Stores an IBC related file.
     * @param fileContents
     */
    public static void storeFile(byte[] fileContents, String fileId) {
        Log.v(TAG, "File contents for file " + fileId + ": " + new String(fileContents));
        FileHandler.writeToFile(mFiles.get(fileId), fileContents);
    }

    /**
     * Stores the server certificate so it can be used for WPA2-Enterprise.
     * @param fileContents
     */
    public static void storeServerCertificate(byte[] fileContents) {
        Log.v(TAG, "Certificate: " + new String(fileContents));
        FileHandler.writeToFile(SERVER_CERTIFICATE_PATH, fileContents);
    }

    /**
     * Stores the server certificate so it can be used for WPA2-Enterprise.
     * @param fileContents
     */
    public static void storeDeviceCertificate(byte[] fileContents) {
        Log.v(TAG, "Certificate: " + new String(fileContents));
        FileHandler.writeToFile(DEVICE_CERTIFICATE_PATH, fileContents);
    }

    /**
     * Stores the device private IBC key.
     * @param fileContents
     */
    public static void storeDevicePrivateKey(byte[] fileContents) {
        Log.v(TAG, "Device's Private Key: " + new String(fileContents));
        FileHandler.writeToFile(DEVICE_PRIVATE_KEY_PATH, fileContents);
    }

    /**
     * Stores the server's public key.
     * @param fileContents
     */
    public static void storeServerPublicKey(byte[] fileContents) {
        Log.v(TAG, "Server's Public Key: " + new String(fileContents));
        FileHandler.writeToFile(SERVER_PUBLIC_KEY_PATH, fileContents);
    }

    /**
     * Generates the password to be used when authenticating.
     * @return
     */
    private static String generateAuthPassword() {
        String password = "";
        // TODO: actually call the IBE lib here.
        // password = sIBELib.sign(deviceId, deviceCertificate).
        return password;
    }

    /**
     * Creates a WPA2-Enterprise configuration with the current files.
     * @param context
     */
    public static void setupWifiProfile(String ssid, Context context) {
        // Create a cert object from the certificate file.
        X509Certificate serverCertificate = null;
        try {
            CertificateFactory certificateGenerator = CertificateFactory.getInstance("X.509");
            Collection certs = certificateGenerator.generateCertificates(new FileInputStream(SERVER_CERTIFICATE_PATH));

            // We know our file will have only 1 certificate, so we get the first one.
            serverCertificate = (X509Certificate) certs.iterator().next();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            Log.e(TAG, "No valid certificates found");
            return;
        }

        // Create basic network configuration.
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = ssid;

        // Configure EAP-TTLS and PAP specific parameters.
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
        enterpriseConfig.setCaCertificate(serverCertificate);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
        enterpriseConfig.setPassword(generateAuthPassword());
        wifiConfig.enterpriseConfig = enterpriseConfig;

        // Store the profile.
        // TODO: test if this fails when there is already a profile with the same SSID.
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiManager.addNetwork(wifiConfig);
    }
}
