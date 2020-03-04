package com.chaseatucker.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.DeleteTaskMutation;
import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListFilesQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.DeleteTaskInput;

public class TaskDetail extends AppCompatActivity {

    private static final String TAG = "cat.taskDetail";
    AWSAppSyncClient mAWSAppSyncClient;
    String taskID = "";
    int version;
    List<GetTaskQuery.Item> files = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        Button updateTaskButton = findViewById(R.id.update_task_button);
        updateTaskButton.setOnClickListener(updateTaskListener);

        Button deleteTaskButton = findViewById(R.id.delete_task_button);
        deleteTaskButton.setOnClickListener(deleteTaskListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAWSAppSyncClient.query(GetTaskQuery.builder().id(getIntent().getStringExtra("id")).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<GetTaskQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetTaskQuery.Data> response) {
                        Handler h = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message inputMessage) {
                                String taskName = response.data().getTask().title();
                                TextView taskTitle = findViewById(R.id.taskDetailTitle);
                                taskTitle.setText(taskName);

                                Log.i(TAG, "task version: " + response.data().getTask()._version());

                                String taskState = "Status: " + response.data().getTask().state();
                                TextView taskStateTextView = findViewById(R.id.taskStateTextView);
                                taskStateTextView.setText(taskState);

                                TextView taskTeamTextView = findViewById(R.id.taskTeamTextView);
                                String taskTeam = "Team: ";
                                if(response.data().getTask().team() != null) {
                                    taskTeam += response.data().getTask().team().name();
                                } else {
                                    taskTeam += "none";
                                }
                                taskTeamTextView.setText(taskTeam);


                                String taskBody = response.data().getTask().body();
                                TextView taskBodyTextView = findViewById(R.id.taskBodyTextView);
                                taskBodyTextView.setText(taskBody);

                                taskID = response.data().getTask().id();

                                version = response.data().getTask()._version();

                                if(response.data().getTask().files().items() != null) {
                                    files = response.data().getTask().files().items();
                                }
                            }
                        };
                        h.obtainMessage().sendToTarget();

                        mAWSAppSyncClient.query(ListFilesQuery.builder().build())
                                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                                .enqueue(new GraphQLCall.Callback<ListFilesQuery.Data>() {
                                    @Override
                                    public void onResponse(@Nonnull Response<ListFilesQuery.Data> response) {
                                        Handler h = new Handler(Looper.getMainLooper()){
                                            @Override
                                            public void handleMessage(Message inputMessage) {
                                                if(response.data().listFiles().items() != null) {
                                                    for(ListFilesQuery.Item item : response.data().listFiles().items()) {
                                                        if(item.task() != null) {
                                                            if(item.task().id().equals(taskID)) {
                                                                Log.i(TAG, "file name: " + item.name());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        };
                                        h.obtainMessage().sendToTarget();
                                    }

                                    @Override
                                    public void onFailure(@Nonnull ApolloException e) {
                                        Log.i(TAG, "failed to get files");
                                    }
                                });

                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "task load failed: " + e);
                    }
                });
    }

    // OnClickListener to go to update a task
    private View.OnClickListener updateTaskListener = v -> {
        Intent i = new Intent(getApplicationContext(), UpdateTask.class);
        i.putExtra("id", taskID);
        startActivity(i);
    };

    // OnClickListener to delete a task
    private View.OnClickListener deleteTaskListener = v -> {
        DeleteTaskInput task = DeleteTaskInput.builder()
                .id(taskID)
                ._version(version)
                .build();

        Log.i(TAG, "trying to delete task: " + taskID);

        mAWSAppSyncClient.mutate(DeleteTaskMutation.builder().input(task).build()).enqueue(
                new GraphQLCall.Callback<DeleteTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<DeleteTaskMutation.Data> response) {
                        Log.i(TAG, "should have deleted task: " + taskID);
                        finish();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "failed to delete task: " + e);
                    }
                }
        );
    };
}