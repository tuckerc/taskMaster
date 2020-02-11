package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener goToNewTaskCreator = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AddATask.class);
            startActivity(i);
        }
    };

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener goToAllTasks = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AllTasks.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add listener to addTask button
        Button addTask = findViewById(R.id.addATask);
        addTask.setOnClickListener(goToNewTaskCreator);

        // add listener to addTask button
        Button allTasks = findViewById(R.id.);
        addTask.setOnClickListener(goToNewTaskCreator);
    }
}
