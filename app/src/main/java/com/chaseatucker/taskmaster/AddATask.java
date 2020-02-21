package com.chaseatucker.taskmaster;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddATask extends AppCompatActivity {

    private static String TAG = "cat.addTask";
    AWSAppSyncClient mAWSAppSyncClient;

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener newTaskCreateListener = new View.OnClickListener() {
        public void onClick(View v) {
            // grab new task title and body
            EditText taskName = findViewById(R.id.newTaskNamePT);
            String taskNameStr = taskName.getText().toString();
            EditText taskBody = findViewById(R.id.newTaskBodyPT);
            String taskBodyStr = taskBody.getText().toString();

            CreateTaskInput newTask = CreateTaskInput.builder().
                    title(taskNameStr).
                    body(taskBodyStr).
                    state("new").
                    build();

            mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(newTask).build()).enqueue(
                    new GraphQLCall.Callback<CreateTaskMutation.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
                            // go back to previous activity
                            finish();
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            Log.w(TAG, "failure");
                        }
                    }
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_atask);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // add listener to new task submit button
        Button newTaskCreateButton = findViewById(R.id.newTaskSubmit);
        newTaskCreateButton.setOnClickListener(newTaskCreateListener);
    }
}
