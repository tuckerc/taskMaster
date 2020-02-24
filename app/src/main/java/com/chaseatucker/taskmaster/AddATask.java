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

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
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

import type.CreateTaskInput;

public class AddATask extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static String TAG = "cat.addTask";
    AWSAppSyncClient mAWSAppSyncClient;
    Spinner teamsSpinner;
    List<String> teamList;
    ArrayAdapter<String> adapter;
    HashMap<String, String> teamIDsMap;
    String selectedTeamID = "";

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
                    taskTeamId(selectedTeamID).
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
                            Log.i(TAG, "failed to add task: " + e);
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

        // set the values of the teams spinner
        teamsSpinner = findViewById(R.id.teams_spinner);

        teamList = new LinkedList<>();

        teamIDsMap = new HashMap<>();

        adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item, teamList);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        teamsSpinner.setAdapter(adapter);
        teamsSpinner.setOnItemSelectedListener(this);
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
                                    adapter.notifyDataSetChanged();
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
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // set currentTeamID based on ID of selected team name
        String teamName = parent.getItemAtPosition(position).toString();
        selectedTeamID = teamIDsMap.get(teamName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
