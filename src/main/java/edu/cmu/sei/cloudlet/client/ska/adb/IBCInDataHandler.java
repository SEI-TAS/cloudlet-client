package edu.cmu.sei.cloudlet.client.ska.adb;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;

/**
 * Created by Sebastian on 2015-07-02.
 */
public class IBCInDataHandler implements IInDataHandler {
    private final String TAG = "IBCInDataHandler";

    private static final String SSID_ID = "ssid";

    @Override
    public void handleData(Bundle data, Context context) {
        for(String key : data.keySet()) {
            if(SSID_ID.equals(key)) {
                String networkId = data.getString(key);
                Log.v(TAG, "Received ssid: " + networkId);
                IBCAuthManager.setupWifiProfile(networkId, context);
            }
        }
    }
}
