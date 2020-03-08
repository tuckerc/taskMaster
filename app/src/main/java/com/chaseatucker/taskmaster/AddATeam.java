package com.chaseatucker.taskmaster;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateTeamMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.CreateTeamInput;

public class AddATeam extends AppCompatActivity {

    private static String TAG = "cat.addTask";
    AWSAppSyncClient mAWSAppSyncClient;
    String teamName;

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener newTeamCreateListener = new View.OnClickListener() {
        public void onClick(View v) {

            // grab new task title and body
            EditText teamNameET = findViewById(R.id.add_a_team_team_name_et);
            teamName = teamNameET.getText().toString();

            CreateTeamInput newTeam = CreateTeamInput.builder()
                    .name(teamName)
                    .build();

            // create a new team
            mAWSAppSyncClient.mutate(CreateTeamMutation.builder().input(newTeam).build()).enqueue(
                    new GraphQLCall.Callback<CreateTeamMutation.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
                            Log.i(TAG, "team " + response.data().createTeam().name() + " successfully created");
                            finish();
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            Log.e(TAG, "failed to create team: " + e);
                        }
                    }
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_team);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        findViewById(R.id.add_a_team_btn).setOnClickListener(newTeamCreateListener);
    }
}
