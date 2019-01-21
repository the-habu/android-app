package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;

public class InGameActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();


    BluetoothLeService mBluetoothLeService;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceAddress;
    private boolean mConnected = false;


    BluetoothGattCharacteristic latency;
    BluetoothGattCharacteristic trigger;
    BluetoothGattCharacteristic irRead;
    BluetoothGattCharacteristic irWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Intent socketIntent = new Intent();
        ServiceConnection socketServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(socketIntent, socketServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };



    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                InitCharactaristics(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                TakeAction(intent);
            }
        }
    };

    private void TakeAction(Intent intent) {
        String stringExtra = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        String command = intent.getStringExtra(BluetoothLeService.COMMAND);

        if(command.equals("SHOOT")) {
            irWrite.setValue(BluetoothLeService.SHOOTCOMMAND);
        }
        else {
            TextView view = findViewById(R.id.textViewLatency);
            view.setText(stringExtra);
        }
    }

    private void InitCharactaristics(List<BluetoothGattService> supportedGattServices) {
        for (BluetoothGattService blGattService: supportedGattServices) {
            String uuid = blGattService.getUuid().toString();
            if(uuid.equals(SampleGattAttributes.TaggerService)){
                List<BluetoothGattCharacteristic> characteristics = blGattService.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_TRIGGER_UUID)||
                        characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_LATENCY_UUID) ||
                        characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_IR_RECEIVE_UUID))
                    {
                        mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                    }

                    if(characteristic.getUuid() == BluetoothLeService.UUID_CHARACTERISTIC_IR_SEND_UUID) {
                        irWrite = characteristic;

                        characteristic.setWriteType(WRITE_TYPE_DEFAULT);
                        //irWrite.setWriteType(PERMISSION_WRITE);
                    }
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
