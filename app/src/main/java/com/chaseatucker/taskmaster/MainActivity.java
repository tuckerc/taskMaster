package com.chaseatucker.taskmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String TAG = "cat.MainActivity";
    String givenName = "";

    // OnClickListener to go to add a task
    private View.OnClickListener goToNewTaskCreator = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AddATask.class);
            startActivity(i);
        }
    };

    // OnClickListener to go to all tasks
    private View.OnClickListener goToAllTasks = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), AllTasks.class);
            startActivity(i);
        }
    };

    // OnClickListener to go to all tasks
    private View.OnClickListener logout = new View.OnClickListener() {
        public void onClick(View v) {
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
        }
    };

    // OnClickListener to go to settings
    private View.OnClickListener goToSettings = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(getBaseContext(), Settings.class);
            startActivity(i);
        }
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

        // update user tasks label text
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = p.getString("username", "user") + "'s Tasks";
        TextView userLabel = findViewById(R.id.userTasksLabel);
        userLabel.setText(username);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i(TAG + " INIT", "onResult: " + userStateDetails.getUserState());
                        if(userStateDetails.getUserState().equals(UserState.SIGNED_OUT)) {
                            userSignIn();
                        }
                        if(userStateDetails.getUserState().equals(UserState.SIGNED_IN)) {
                            try {
//                                setTitle(AWSMobileClient.getInstance().getUserAttributes().get("given_name"));
                                Map<String, String> userAttributes = AWSMobileClient.getInstance().getUserAttributes();
                                givenName = userAttributes.get("given_name");

                                for(String key : AWSMobileClient.getInstance().getUserAttributes().keySet()) {
                                    Log.i(TAG, "userDetailsKey: " + key);
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "error getting userAttributes \n" + e);
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG + " INIT", "Initialization error.", e);
                    }
                }
        );



        // update action bar with [user name]'s Tasks
//        Handler h = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message inputMessage) {
//                try {
//                    Map<String, String> userAttributes = AWSMobileClient.getInstance().getUserAttributes();
//                    for(String key : userAttributes.keySet()) {
//                        Log.i(TAG, "userAttributes key: " + key);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        h.obtainMessage().sendToTarget();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update user tasks label text
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = p.getString("username", "user") + "'s Tasks";
        TextView userLabel = findViewById(R.id.userTasksLabel);
        userLabel.setText(username);


        setTitle(AWSMobileClient.getInstance().getUsername());
//        setTitle(givenName + "'s Tasks");
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