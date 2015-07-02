package edu.cmu.sei.cloudlet.client.ska.adb;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.sei.cloudlet.client.ibc.IBCAuthManager;

/**
 * Created by Sebastian on 2015-07-02.
 */
public class IBCOutDataHandler implements IOutDataHandler {

    private static final String DEVICE_ID = "device_id";

    @Override
    public String getData(Bundle data, Context context) {
        JSONObject jsonData = new JSONObject();

        for(String key : data.keySet()) {
            if(DEVICE_ID.equals(key)) {
                String deviceId = IBCAuthManager.getDeviceId(context);
                try {
                    jsonData.put(DEVICE_ID, deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonData.toString();
    }
}
