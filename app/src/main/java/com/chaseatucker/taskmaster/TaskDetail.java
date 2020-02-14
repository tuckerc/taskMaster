package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        String taskName = getIntent().getStringExtra("taskName");
        TextView taskTitle = findViewById(R.id.taskDetailTitle);
        taskTitle.setText(taskName);
    }
}