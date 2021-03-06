package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import type.UpdateUserInput;

public class Settings extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static String TAG = "cat.updateTask";
    AWSAppSyncClient mAWSAppSyncClient;
    Spinner teamsSpinner;
    List<String> teamList;
    ArrayAdapter<String> adapter;
    HashMap<String, String> teamIDsMap;
    String selectedTeamID = "";

    // OnClickListener for Save User Team Button
    private View.OnClickListener updateUserTeamListener = v -> {

        Handler h = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage) {
                Thread thread = new Thread(() -> {
                    try  {
                        try {
                            String userID = AWSMobileClient.getInstance().getUserAttributes().get("sub");

                            UpdateUserInput user = UpdateUserInput.builder()
                                    .id(userID)
                                    .userTeamId(selectedTeamID)
                                    .build();

                            mAWSAppSyncClient.mutate(UpdateUserMutation.builder().input(user).build())
                                    .enqueue(new GraphQLCall.Callback<UpdateUserMutation.Data>() {
                                        @Override
                                        public void onResponse(@Nonnull Response<UpdateUserMutation.Data> response) {
                                            Log.i(TAG, AWSMobileClient.getInstance().getUsername() +
                                                    "'s team successfully updated to team id " +
                                                    response.data().updateUser().team().id());

                                            // store user team id in shared prefs
                                            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = p.edit();
                                            editor.putString("userTeamID", selectedTeamID);
                                            editor.apply();
                                        }

                                        @Override
                                        public void onFailure(@Nonnull ApolloException e) {
                                            Log.e(TAG, "Failed to update " +
                                                    AWSMobileClient.getInstance().getUsername() +
                                                    "'s team id to " + selectedTeamID, e);
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        };
        h.obtainMessage().sendToTarget();

        Context context = getApplicationContext();
        CharSequence text = "Team Updated!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveUserTeamButton = findViewById(R.id.updateUserTeamButton);
        saveUserTeamButton.setOnClickListener(updateUserTeamListener);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // set the values of the teams spinner
        teamsSpinner = findViewById(R.id.user_teams_spinner);

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
        Log.i(TAG, "selected team " + selectedTeamID);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}