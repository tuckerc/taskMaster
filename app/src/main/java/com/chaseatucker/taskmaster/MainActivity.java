package com.chaseatucker.taskmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // OnClickListener to go to add a task
    private View.OnClickListener goToNewTaskCreator = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AddATask.class);
            startActivity(i);
        }
    };

    // OnClickListener to go to all tasks
    private View.OnClickListener goToAllTasks = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AllTasks.class);
            startActivity(i);
        }
    };

    // OnClickListener to go to settings
    private View.OnClickListener goToSettings = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), Settings.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add on click listener to newTaskButton
        Button newTaskButton = findViewById(R.id.addTaskButton);
        newTaskButton.setOnClickListener(goToNewTaskCreator);

        // add on click listener to all tasks button
        Button allTasksButton = findViewById(R.id.allTasksButton);
        allTasksButton.setOnClickListener(goToAllTasks);

        // add on click listener to settings button
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(goToSettings);

        // update user tasks label text
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = p.getString("username", "user") + "'s Tasks";
        TextView userLabel = findViewById(R.id.userTasksLabel);
        userLabel.setText(username);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update user tasks label text
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = p.getString("username", "user") + "'s Tasks";
        TextView userLabel = findViewById(R.id.userTasksLabel);
        userLabel.setText(username);
    }
}