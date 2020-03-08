package com.chaseatucker.taskmaster;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.amplify.generated.graphql.CreateUserMutation;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import javax.annotation.Nonnull;

import type.CreateUserInput;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "cat.MainActivity";
    String givenName = "";
    AWSAppSyncClient mAWSAppSyncClient;
    String userID;
    String userTeamID;

    private static PinpointManager pinpointManager;

    // OnClickListener to go to add a team
    private View.OnClickListener goToNewTeamCreator = v -> {
        Intent i = new Intent(getBaseContext(), AddATeam.class);
        startActivity(i);
    };

    // OnClickListener to go to add a task
    private View.OnClickListener goToNewTaskCreator = v -> {
        Intent i = new Intent(getBaseContext(), AddATask.class);
        startActivity(i);
    };

    // OnClickListener to logout
    private View.OnClickListener logout = v -> {
        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
            @Override
            public void onResult(final Void result) {
                Log.d(TAG, "signed-out");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "sign-out error", e);
            }
        });
        AWSMobileClient.getInstance().signOut();
        userSignIn();
    };

    // OnClickListener to go to settings
    private View.OnClickListener goToSettings = v -> {
        Intent i = new Intent(getBaseContext(), Settings.class);
        startActivity(i);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // add on click listener to newTaskButton
        Button newTeamButton = findViewById(R.id.main_add_a_team_btn);
        newTeamButton.setOnClickListener(goToNewTeamCreator);

        // add on click listener to newTaskButton
        Button newTaskButton = findViewById(R.id.addTaskButton);
        newTaskButton.setOnClickListener(goToNewTaskCreator);

        // add on click listener to settings button
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(goToSettings);

        // add on click listener to logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(logout);

        // request permission to read external storage
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        // request permission to write to external storage
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        // request permission to write to external storage
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        // initialize mAWSAppSyncClient
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // Initialize Firebase
        FirebaseApp.initializeApp(getApplicationContext());

        // Initialize PinpointManager
        getPinpointManager(getApplicationContext());

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i(TAG, "INIT onResult: " + userStateDetails.getUserState());
                        UserState userState = userStateDetails.getUserState();
                        if(userState.equals(UserState.SIGNED_OUT)) {
                            userSignIn();
                        }
                        if(userState.equals(UserState.SIGNED_IN)) {
                            try {
                                givenName = AWSMobileClient.getInstance().getUserAttributes().get("given_name");
                                userID = AWSMobileClient.getInstance().getUserAttributes().get("sub");

                                mAWSAppSyncClient.query(ListUsersQuery.builder().build())
                                        .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                                        .enqueue(new GraphQLCall.Callback<ListUsersQuery.Data>() {
                                            @Override
                                            public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
                                                if(response.data().listUsers() != null) {
                                                    boolean userCreated = false;
                                                    for(ListUsersQuery.Item user : response.data().listUsers().items()) {
                                                        if(user.id().equals(userID)) {
                                                            userCreated = true;

                                                            userTeamID = user.team().id();

                                                            // store user team id in shared prefs
                                                            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                            SharedPreferences.Editor editor = p.edit();
                                                            editor.putString("userTeamID", userTeamID);
                                                            editor.apply();
                                                        }
                                                    }
                                                    if(!userCreated) {
                                                        CreateUserInput newUser = CreateUserInput.builder()
                                                                .id(userID)
                                                                .username(AWSMobileClient.getInstance().getUsername())
                                                                .build();

                                                        mAWSAppSyncClient.mutate(CreateUserMutation.builder()
                                                                .input(newUser).build()).enqueue(
                                                                new GraphQLCall.Callback<CreateUserMutation.Data>() {
                                                                    @Override
                                                                    public void onResponse(@Nonnull Response<CreateUserMutation.Data> response) {
                                                                        Log.i(TAG, "new user " + response.data().createUser().username()
                                                                        + " successfully created");
                                                                    }

                                                                    @Override
                                                                    public void onFailure(@Nonnull ApolloException e) {
                                                                        Log.e(TAG, "failure creating user " + e);
                                                                    }
                                                                }
                                                        );
                                                    }
                                                } else {
                                                    CreateUserInput newUser = CreateUserInput.builder()
                                                            .id(userID)
                                                            .username(AWSMobileClient.getInstance().getUsername())
                                                            .build();

                                                    mAWSAppSyncClient.mutate(CreateUserMutation.builder()
                                                            .input(newUser).build()).enqueue(
                                                            new GraphQLCall.Callback<CreateUserMutation.Data>() {
                                                                @Override
                                                                public void onResponse(@Nonnull Response<CreateUserMutation.Data> response) {
                                                                    Log.i(TAG, "new user " + response.data().createUser().username()
                                                                            + " successfully created");
                                                                }

                                                                @Override
                                                                public void onFailure(@Nonnull ApolloException e) {
                                                                    Log.e(TAG, "failure creating user " + e);
                                                                }
                                                            }
                                                    );
                                                }
                                            }

                                            @Override
                                            public void onFailure(@Nonnull ApolloException e) {
                                                Log.e(TAG, "failure listing users " + e);
                                            }
                                        });

                            } catch (Exception e) {
                                Log.i(TAG, "error getting userAttributes \n" + e);
                            }
                            Handler h = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message inputMessage) {
                                    // update the action bar
                                    setTitle(givenName + "'s Tasks");
                                }
                            };
                            h.obtainMessage().sendToTarget();

                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "INIT Initialization error.", e);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void userSignIn() {
        AWSMobileClient.getInstance().showSignIn(MainActivity.this,
                SignInUIOptions.builder()
                        .nextActivity(MainActivity.class)
                        .backgroundColor(R.color.colorAccent)
                        .logo(R.drawable.logo)
                        .canCancel(false)
                        .build(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d(TAG, "onResult: " + result.getUserState());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("INIT", userStateDetails.getUserState().toString());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", "Initialization error.", e);
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            final String token = task.getResult().getToken();
                            Log.d(TAG, "Registering push notifications token: " + token);
                            pinpointManager.getNotificationClient().registerDeviceToken(token);
                        }
                    });
        }
        return pinpointManager;
    }
}