package de.openlt.andriod.main;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.bluetoothlegatt.DeviceScanActivity;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.SampleGattAttributes;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.openlt.andriod.Audio.SoundPoolPlayer;
import de.openlt.andriod.WebSocket.WebSocketService;

public class InGameActivity extends Activity {
    private final static String TAG = InGameActivity.class.getSimpleName();


    BluetoothLeService mBluetoothLeService;
    WebSocketService webSocketService;

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String MACADDRESSFIELDNAME= "TAGGERMAC";
    public static final float DeathTime = 5000;

    private String mDeviceAddress;
    private boolean mConnected = false;
    private TextView mConnectionState;
    private TextView mPlayerState;
    private TextView mTimeActivationLeft;
    private SoundPoolPlayer soundPlayer;
    private int connectionCounter;

    BluetoothGattCharacteristic latency;
    BluetoothGattCharacteristic trigger;
    BluetoothGattCharacteristic irRead;
    BluetoothGattCharacteristic irWrite;

    static public List<BluetoothGattCharacteristic> characteristicsLeftToActivate;

    final String csvDir = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.laserTag.de/Logging"); // Here csv file name is MyCsvFile.csv
    final String csvFileName = "latencyLog.csv";

    private Boolean activePlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        soundPlayer = new SoundPoolPlayer(this);
        characteristicsLeftToActivate = new LinkedList<BluetoothGattCharacteristic>();

        mConnectionState = (TextView) findViewById(R.id.InGame_Connected_Val);
        mPlayerState = (TextView) findViewById(R.id.InGame_Active_Val);
        mTimeActivationLeft = (TextView) findViewById(R.id.TimeActivationLeft);
        activePlayer = true;

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putString(MACADDRESSFIELDNAME, mDeviceAddress);
        editor.apply();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Intent webSockertIntent = new Intent(this, WebSocketService.class);
        startService(webSockertIntent);
        bindService(webSockertIntent, WebSockertServiceConnection, BIND_AUTO_CREATE);

        Button removeTaggerBtn =  (Button) findViewById(R.id.RemoveTaggerBtn);
        removeTaggerBtn.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View v) {
                                                   editor.remove(MACADDRESSFIELDNAME);
                                                   editor.apply();
                                                   mBluetoothLeService.disconnect();
                                                   Intent intent = new Intent(InGameActivity.this, DeviceScanActivity.class);
                                                   startActivity(intent);
                                               }
                                           });
//        Thread t = new Thread(){
//            public void run() {
//                getApplicationContext().bindService(
//                        new Intent(getApplicationContext(),
//                                BluetoothLeService.class),
//                        mServiceConnection, BIND_AUTO_CREATE);
//            }
//        };
//        t.start();
    }


    private final ServiceConnection WebSockertServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketService = ((WebSocketService.LocalBinder) service).getService();
            Log.i(TAG,"WebSocket onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,"WebSocket onServiceDisconnected");
            webSocketService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.i(TAG,"WebSocket onBindingDied");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.i(TAG,"WebSocket onNullBinding");
        }
    };

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
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.d(TAG, "ACTION_GATT_DISCONNECTED");
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                InitCharactaristics(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                TakeAction(intent);
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }


    byte[] GenerateShootData()
    {
        return BluetoothLeService.hexStringToByteArray(BluetoothLeService.SHOOTCOMMAND + "010201"); //TODO: PUT PLAYERID HERE
    }

    private void TakeAction(Intent intent) {
        String stringExtra = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        String command = intent.getStringExtra(BluetoothLeService.COMMAND);
        Log.i(TAG, "Command was " + command +" with " + stringExtra);

        switch (command) {
            case "SHOOT":
                if(activePlayer && stringExtra.equals("1")) {
                    irWrite.setValue(GenerateShootData());
                    mBluetoothLeService.writeCharacteristic(irWrite);
                    soundPlayer.playShortResource(R.raw.laser_gun_shot_2);
                }
                break;
            case "LATENCY":
                ((TextView)findViewById(R.id.InGame_Latenz_Val)).setText(stringExtra);
                WriteLog(intent, stringExtra);
                break;
            case "REVCIEVE":
                GotHit(stringExtra);
                break;
            default:
                Log.e(TAG, "unkown Command");
                break;
        }
    }

    void GotHit(String data){
        if(data.equals("241") || data.equals("191")){
            return;
        }
        if(activePlayer) {
            mPlayerState.setText(R.string.DeactiveLabel);
            activePlayer = false;
            soundPlayer.playShortResource(R.raw.shield_hit_1);
            final Timer t = new Timer();
            final int timeStep = 100;
            final ProgressBar bar = findViewById(R.id.progressBar);
            bar.setVisibility(View.VISIBLE);

            TimerTask task = new TimerTask() {
                float timer = 0;
                @Override
                public void run() {
                    if (timer >= DeathTime) {
                        activePlayer = true;
                        SetTimerToUI("");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayerState.setText(R.string.activeLabel);
                                bar.setProgress(0);
                                bar.setVisibility(View.INVISIBLE);
                            }});
                        t.cancel();
                    } else {
                        timer += timeStep;
                        SetTimerToUI("" + (DeathTime - timer) / 1000f);
                    }
                }

                void SetTimerToUI(final String time) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int val = (int) ((timer/ DeathTime) * 100);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                bar.setProgress(val, false);
                            }
                            mTimeActivationLeft.setText(time);
                        }
                    });
                }
            };
            t.scheduleAtFixedRate(task, 0, timeStep);
        }
    }




    private void InitCharactaristics(List<BluetoothGattService> supportedGattServices) {
        boolean Writing = false;
        for (BluetoothGattService blGattService: supportedGattServices) {
            String uuid = blGattService.getUuid().toString();
            if(uuid.equals(SampleGattAttributes.TaggerService)){
                List<BluetoothGattCharacteristic> characteristics = blGattService.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_TRIGGER_UUID)||
                        characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_LATENCY_UUID) ||
                        characteristic.getUuid().toString().equals(SampleGattAttributes.CHARACTERISTIC_IR_RECEIVE_UUID))
                    {

                        if(Writing)
                            characteristicsLeftToActivate.add(characteristic);
                        else
                        {
                            Writing = true;
                            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                        }
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
        Log.d(TAG, "onResume");
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        unbindService(mServiceConnection);
        unbindService(WebSockertServiceConnection);

        mBluetoothLeService = null;
        webSocketService = null;
        soundPlayer.release();
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
