package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Settings extends AppCompatActivity {

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener updateUsernameListener = new View.OnClickListener() {
        public void onClick(View v) {
            // grab the username field
            EditText usernameField = findViewById(R.id.usernameInput);
            // get the text from username field
            String username = usernameField.getText().toString();
            // save the username to SharedPreferences
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = p.edit();
            editor.putString("username", username);
            editor.apply();
            // go back to main activity
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveUsernameButton = findViewById(R.id.usernameSaveButton);
        saveUsernameButton.setOnClickListener(updateUsernameListener);
    }
}