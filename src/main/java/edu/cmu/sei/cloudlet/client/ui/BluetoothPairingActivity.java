package edu.cmu.sei.cloudlet.client.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import edu.cmu.sei.cloudlet.client.R;
import edu.cmu.sei.cloudlet.client.ska.SKAPairingService;

public class BluetoothPairingActivity extends Activity {

    // Code to identify when the activity to enable Bluetooth returns.
    private final int RET_ENABLE_BLUETOOTH = 1;
    private final int RET_ENABLE_DISCOVERABLE = 2;

    private BluetoothAdapter mBluetoothAdapter = null;

    private Switch mDiscoverableSwitch = null;

    private SKAPairingService mPairingService = null;

    private final BroadcastReceiver mModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                if (mode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    mDiscoverableSwitch.setChecked(false);
                    mPairingService.stop();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDiscoverableSwitch = (Switch) findViewById(R.id.discoverySwitch);

        mPairingService = new SKAPairingService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerModeChangeReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterModeChangeReceiver();
    }

    private void registerModeChangeReceiver() {
        Log.v("", "Registering broadcastereceiver.");
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mModeReceiver, intent);
    }

    private void unregisterModeChangeReceiver() {
        try {
            unregisterReceiver(mModeReceiver);
        } catch (IllegalArgumentException e) {
            Log.v("", "Unregistering non-registered broadcastereceiver.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pairing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBluetoothOnSwitch(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on) {
            Intent startBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(startBTIntent, RET_ENABLE_BLUETOOTH);
        } else {
            mBluetoothAdapter.disable();
        }
    }

    public void onDiscoverableOnSwitch(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        int duration = 0;
        if (on) {
            duration = 300;
        } else {
            if(mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                // Only way to turn off discoverablity, turning it on for 1 second.
                duration = 1;
                mPairingService.stop();
            } else {
                // If it is not in discoverable mode, ignore this request.
                return;
            }
        }

        // This turns on or off discoverability (using the hack above for off).
        Intent makeDiscoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        makeDiscoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        startActivityForResult(makeDiscoverableIntent, RET_ENABLE_DISCOVERABLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("", "Code: " + requestCode);
        switch (requestCode) {
            case RET_ENABLE_DISCOVERABLE:
                Log.v("", "Res: " + resultCode);
                if (resultCode != Activity.RESULT_CANCELED) {
                    boolean turn_off_hack = resultCode == 1;
                    if(!turn_off_hack) {
                        mPairingService.start();
                    }
                } else {
                    Log.d("", "Discoverable mode not enabled");
                }
        }
    }
}
