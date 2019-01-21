package com.example.android.bluetoothlegatt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        final Button button = findViewById(R.id.LosBtn);

        EditText userNameField = (EditText) findViewById(R.id.userName);

        if (settings.contains("username")){
            userNameField.setText(settings.getString("username","n/a"));
        }
        String userName = userNameField.getText().toString();

        editor.putString("username", userName);
        editor.apply();

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button





                Intent intent = new Intent(StartActivity.this, DeviceScanActivity.class);
                startActivity(intent);

                //setContentView(R.layout.listitem_device);
            }
        });
    }



}
