package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaseatucker.taskmaster.model.Task;
import com.chaseatucker.taskmaster.room.AppDatabase;
import com.chaseatucker.taskmaster.room.TaskMasterDao;

public class AddATask extends AppCompatActivity {

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener newTaskCreateListener = new View.OnClickListener() {
        public void onClick(View v) {
            // grab new task title and body
            EditText taskName = findViewById(R.id.newTaskNamePT);
            String taskNameStr = taskName.getText().toString();
            EditText taskBody = findViewById(R.id.newTaskBodyPT);
            String taskBodyStr = taskBody.getText().toString();

            // create the new task
            Task newTask = new Task(taskNameStr, taskBodyStr);

            // use the Room db
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "pokemon")
                    .allowMainThreadQueries()
                    .build();
            TaskMasterDao dao = db.taskMasterDao();

            // add the new task to the db
            dao.addTask(newTask);

            // go back to main activity
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_atask);

        // add listener to new task submit button
        Button newTaskCreateButton = findViewById(R.id.newTaskSubmit);
        newTaskCreateButton.setOnClickListener(newTaskCreateListener);
    }
}
