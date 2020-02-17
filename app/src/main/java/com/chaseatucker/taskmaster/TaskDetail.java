package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        String taskName = getIntent().getStringExtra("title");
        TextView taskTitle = findViewById(R.id.taskDetailTitle);
        taskTitle.setText(taskName);

        String taskState = getIntent().getStringExtra("state");
        TextView taskStateTextView = findViewById(R.id.taskStateTextView);
        taskStateTextView.setText(taskState);

        String taskBody = getIntent().getStringExtra("body");
        TextView taskBodyTextView = findViewById(R.id.taskBodyTextView);
        taskBodyTextView.setText(taskBody);
    }
}