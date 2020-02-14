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

    // OnClickListener to go to clean dishes task
    private View.OnClickListener goToCleanDishes = new View.OnClickListener() {
        public void onClick(View v) {
            Button cleanDishesButton = findViewById(R.id.cleanDishesTaskButton);
            String buttonText = cleanDishesButton.getText().toString();
            Intent i = new Intent(getBaseContext(), TaskDetail.class);
            i.putExtra("taskName", buttonText);
            startActivity(i);
        }
    };

    // OnClickListener to go to make dinner task
    private View.OnClickListener goToMakeDinner = new View.OnClickListener() {
        public void onClick(View v) {
            Button makeDinnerButton = findViewById(R.id.makeDinnerTaskButton);
            String buttonText = makeDinnerButton.getText().toString();
            Intent i = new Intent(getBaseContext(), TaskDetail.class);
            i.putExtra("taskName", buttonText);
            startActivity(i);
        }
    };

    // OnClickListener to go to feed dogs task
    private View.OnClickListener goToFeedDogs = new View.OnClickListener() {
        public void onClick(View v) {
            Button feedDogsButton = findViewById(R.id.feedDogsTaskButton);
            String buttonText = feedDogsButton.getText().toString();
            Intent i = new Intent(getBaseContext(), TaskDetail.class);
            i.putExtra("taskName", buttonText);
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

        // add on click listener to clean dishes button
        Button cleanDishesButton = findViewById(R.id.cleanDishesTaskButton);
        cleanDishesButton.setOnClickListener(goToCleanDishes);

        // add on click listener to make dinner button
        Button makeDinnerButton = findViewById(R.id.makeDinnerTaskButton);
        makeDinnerButton.setOnClickListener(goToMakeDinner);

        // add on click listener to feed dogs button
        Button feedDogsButton = findViewById(R.id.feedDogsTaskButton);
        feedDogsButton.setOnClickListener(goToFeedDogs);

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