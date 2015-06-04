package edu.cmu.sei.cloudlet.client.ibc;

import android.content.Context;
import android.provider.Settings;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Assumes device can store information of only 1 cloudlet at one time.
 * Created by Sebastian on 2015-05-27.
 */
public class IBCRepoManager {

    /**
     * Returns an ID for the device.
     * @return a String representing a unique id for the device.
     */
    public static String getId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    /**
     * Stores the server certificate so it can be used for WPA2-Enterprise.
     * More exactly, creates a WPA2-Enterprise configuration with the given certificate.
     * @param fileContents
     */
    public static void storeServerCertificate(byte[] fileContents) {
        try {
            // Create a cert object from the data contents.
            CertificateFactory certificateGenerator = CertificateFactory.getInstance("X.509");
            Collection certs = certificateGenerator.generateCertificates(new ByteArrayInputStream(fileContents));
            X509Certificate serverCertificate = (X509Certificate) certs.iterator().next();

        } catch (CertificateException e) {
            e.printStackTrace();
        }

        // Add a network configuration for the given network associated to the certificate.
        /*WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = 'test';

        enterpriseConfig.setCertificate(serverCertificate);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        wifiConfig.enterpriseConfig = enterpriseConfig;
        networkId = wfm.addNetwork(wifiConfig);*/
    }

    /**
     * Stores the device private IBC key.
     * @param fileContents
     */
    public static void storeIBCPrivateKey(byte[] fileContents) {

    }

    /**
     * Stores the equivalent of the Master's public key, which currently are the IBE parameters.
     * @param fileContents
     */
    public static void storeIBCMasterKey(byte[] fileContents) {

    }
}
