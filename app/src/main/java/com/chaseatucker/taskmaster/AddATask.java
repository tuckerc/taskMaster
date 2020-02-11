package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddATask extends AppCompatActivity {

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener newTaskCreateListener = new View.OnClickListener() {
        public void onClick(View v) {
            // make success label visible
            TextView successLabel = findViewById(R.id.newTaskSubmitSuccess);
            successLabel.setVisibility(View.VISIBLE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_atask);

        // hide success label on create
        TextView successLabel = findViewById(R.id.newTaskSubmitSuccess);
        successLabel.setVisibility(View.GONE);

        // add listener to new task submit button
        Button newTaskCreateButton = findViewById(R.id.newTaskSubmit);
        newTaskCreateButton.setOnClickListener(newTaskCreateListener);
    }
}
