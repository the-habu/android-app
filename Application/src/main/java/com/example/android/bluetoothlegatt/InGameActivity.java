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
import android.os.Environment;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;

public class InGameActivity extends Activity {
    private final static String TAG = InGameActivity.class.getSimpleName();


    BluetoothLeService mBluetoothLeService;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceAddress;
    private boolean mConnected = false;


    BluetoothGattCharacteristic latency;
    BluetoothGattCharacteristic trigger;
    BluetoothGattCharacteristic irRead;
    BluetoothGattCharacteristic irWrite;

    final String csvDir = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.laserTag.de/Logging"); // Here csv file name is MyCsvFile.csv
    final String csvFileName = "latencyLog.csv";


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
                InitCharactaristics(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                TakeAction(intent);
            }
        }
    };

    private void TakeAction(Intent intent) {
        String stringExtra = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        String command = intent.getStringExtra(BluetoothLeService.COMMAND);
        Log.i(TAG, "Command was " + command +" with " + stringExtra);

        switch (command) {
            case "SHOOT":
                irWrite.setValue(BluetoothLeService.SHOOTCOMMAND);
                mBluetoothLeService.writeCharacteristic(irWrite);
                break;
            case "LATENCY":
                ((TextView)findViewById(R.id.textViewLatency)).setText(stringExtra);
                WriteLog(intent, stringExtra);
                break;
            default:
                TextView view = findViewById(R.id.textViewLatency);
                view.setText(stringExtra);
                break;
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

                    if(characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_IR_SEND_UUID)) {
                        irWrite = characteristic;
                        irWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    }
                }

            }
        }
    }

    private void WriteLog(Intent intent, String val) {
        //https://stackoverflow.com/questions/11341931/how-to-create-a-csv-on-android
        System.out.println("WriteLog");
        CSVWriter writer = null;
        try {
            File file = new File(csvDir);
            file.mkdirs();

            File csvfile = new File(csvDir+ File.separator + csvFileName);
            csvfile.createNewFile();
            writer = new CSVWriter(new FileWriter(csvfile,true));
            Date d = new Date();
            List<String[]> data = new ArrayList<String[]>();
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            System.out.println("WriteLog at" + csvDir +" with "+ val);
            data.add(new String[]{ timeStamp, val});
            writer.writeAll(data); // data is adding to csv
            writer.close();
            //callRead();
        } catch (IOException e) {
            e.printStackTrace();
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
