package de.openlt.andriod.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.bluetoothlegatt.DeviceScanActivity;
import com.example.android.bluetoothlegatt.R;

import static de.openlt.andriod.main.InGameActivity.EXTRAS_DEVICE_ADDRESS;
import static de.openlt.andriod.main.InGameActivity.MACADDRESSFIELDNAME;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        final Button button = findViewById(R.id.LosBtn);

        final EditText userNameField = (EditText) findViewById(R.id.userName);

        if (settings.contains("username")){
            userNameField.setText(settings.getString("username","n/a"));
        }


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String userName = userNameField.getText().toString();

                editor.putString("username", userName);
                editor.apply();
                // Code here executes on main thread after user presses button
                String mac = settings.getString(MACADDRESSFIELDNAME,"");
                if(mac!= null && !mac.equals(""))
                {
                    Intent intent = new Intent(StartActivity.this, InGameActivity.class);
                    intent.putExtra(EXTRAS_DEVICE_ADDRESS, mac);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(StartActivity.this, DeviceScanActivity.class);
                    startActivity(intent);
                }
                //setContentView(R.layout.listitem_device);
            }
        });
    }



}
