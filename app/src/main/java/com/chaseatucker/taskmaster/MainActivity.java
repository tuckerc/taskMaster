package com.chaseatucker.taskmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

public class MainActivity extends AppCompatActivity {

    String TAG = "cat.MainActivity";
    String givenName = "";

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
        Button newTaskButton = findViewById(R.id.addTaskButton);
        newTaskButton.setOnClickListener(goToNewTaskCreator);

        // add on click listener to settings button
        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(goToSettings);

        // add on click listener to logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(logout);

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
                            } catch (Exception e) {
                                Log.i(TAG, "error getting userAttributes \n" + e);
                            }
                            Handler h = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message inputMessage) {
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
}