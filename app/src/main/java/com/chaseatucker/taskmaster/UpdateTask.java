package com.chaseatucker.taskmaster;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.UpdateTaskInput;

public class UpdateTask extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static String TAG = "cat.updateTask";
    AWSAppSyncClient mAWSAppSyncClient;
    Spinner teamsSpinner;
    List<String> teamList;
    ArrayAdapter<String> teamAdapter;
    HashMap<String, String> teamIDsMap;
    String selectedTeamID = "";
    String taskID = "";
    Spinner taskStateSpinner;
    ArrayAdapter<CharSequence> taskStateAdapter;
    String taskState = "";

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener updateTaskListener = new View.OnClickListener() {
        public void onClick(View v) {
            // grab new task title and body
            EditText taskName = findViewById(R.id.updateTaskNamePT);
            String taskNameStr = taskName.getText().toString();
            EditText taskBody = findViewById(R.id.updateTaskBodyPT);
            String taskBodyStr = taskBody.getText().toString();

            UpdateTaskInput task = UpdateTaskInput.builder().
                    id(taskID).
                    title(taskNameStr).
                    body(taskBodyStr).
                    state(taskState).
                    taskTeamId(selectedTeamID).
                    build();

            Log.i(TAG, "updated task state: " + task.state());
            Log.i(TAG, "updated task team: " + task.taskTeamId());

            mAWSAppSyncClient.mutate(UpdateTaskMutation.builder().input(task).build()).enqueue(
                    new GraphQLCall.Callback<UpdateTaskMutation.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<UpdateTaskMutation.Data> response) {
                            // go back to previous activity
                            Log.i(TAG,"updated task state from response: " + response.data().updateTask().state());
                            Log.i(TAG, "updated team name from response: " + response.data().updateTask().team().name());
                            finish();
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            Log.i(TAG, "failed to update task: " + e);
                        }
                    }
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_task);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // add listener to new task submit button
        Button updateTaskCreateButton = findViewById(R.id.updateTaskSubmit);
        updateTaskCreateButton.setOnClickListener(updateTaskListener);

        // set the values of the teams spinner
        teamsSpinner = findViewById(R.id.update_teams_spinner);

        teamList = new LinkedList<>();

        teamIDsMap = new HashMap<>();

        teamAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, teamList);

        // Specify the layout to use when the list of choices appears
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        teamsSpinner.setAdapter(teamAdapter);
        teamsSpinner.setOnItemSelectedListener(this);

        taskStateSpinner = findViewById(R.id.task_state_spinner);
        taskStateAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_states_array, android.R.layout.simple_spinner_item);
        taskStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskStateSpinner.setAdapter(taskStateAdapter);
        taskStateSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        teamList.clear();
        teamIDsMap.clear();

        mAWSAppSyncClient.query(ListTeamsQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListTeamsQuery.Data> response) {
                        Handler h = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message inputMessage) {
                                for(ListTeamsQuery.Item item : response.data().listTeams().items()) {
                                    teamList.add(item.name());
                                    teamIDsMap.put(item.name(), item.id());
                                    teamAdapter.notifyDataSetChanged();
                                }
                            }
                        };
                        h.obtainMessage().sendToTarget();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "the query to get team list failed");
                    }
                });

        mAWSAppSyncClient.query(GetTaskQuery.builder().id(getIntent().getStringExtra("id")).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<GetTaskQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetTaskQuery.Data> response) {
                        Handler h = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message inputMessage) {
                                String taskName = response.data().getTask().title();
                                TextView taskTitle = findViewById(R.id.updateTaskNamePT);
                                taskTitle.setText(taskName);

                                String taskState = "Current Status: " + response.data().getTask().state();
                                TextView taskStateTextView = findViewById(R.id.task_update_current_state);
                                taskStateTextView.setText(taskState);

                                String taskBody = response.data().getTask().body();
                                TextView taskBodyTextView = findViewById(R.id.updateTaskBodyPT);
                                taskBodyTextView.setText(taskBody);

                                TextView updateCurrentTeamName = findViewById(R.id.updateCurrentTeamEditText);
                                String currentTeam = "Current Team: ";
                                if(response.data().getTask().team() != null) {
                                    currentTeam += response.data().getTask().team().name();
                                } else {
                                    currentTeam += "none";
                                }
                                updateCurrentTeamName.setText(currentTeam);

                                taskID = response.data().getTask().id();

                            }
                        };
                        h.obtainMessage().sendToTarget();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "task load failed: " + e);
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getAdapter().equals(teamAdapter)) {
            Log.i(TAG, "adapter: " + parent.getAdapter().toString());
            // set currentTeamID based on ID of selected team name
            String teamName = parent.getItemAtPosition(position).toString();
            selectedTeamID = teamIDsMap.get(teamName);
        }

        if(parent.getAdapter().equals(taskStateAdapter)) {
            Log.i(TAG, "adapter: " + parent.getAdapter().toString());
            taskState = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
