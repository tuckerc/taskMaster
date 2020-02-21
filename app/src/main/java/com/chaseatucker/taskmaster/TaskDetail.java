package com.chaseatucker.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetTaskQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

public class TaskDetail extends AppCompatActivity {

    private static final String TAG = "cat.taskDetail";
    AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

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

                                String taskState = response.data().getTask().state();
                                TextView taskStateTextView = findViewById(R.id.taskStateTextView);
                                taskStateTextView.setText(taskState);

                                String taskBody = response.data().getTask().body();
                                TextView taskBodyTextView = findViewById(R.id.taskBodyTextView);
                                taskBodyTextView.setText(taskBody);
                            }
                        };
                        h.obtainMessage().sendToTarget();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();



    }
}