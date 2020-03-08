package com.chaseatucker.taskmaster;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amazonaws.amplify.generated.graphql.CreateFileMutation;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateFileInput;
import type.CreateTaskInput;

import static com.chaseatucker.taskmaster.FilePickerFragment.PICKFILE_REQUEST_CODE;

public class AddATask extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static String TAG = "cat.addTask";
    AWSAppSyncClient mAWSAppSyncClient;
    Spinner teamsSpinner;
    List<String> teamList;
    ArrayAdapter<String> adapter;
    HashMap<String, String> teamIDsMap;
    String selectedTeamID = "";
    Uri fileUri;
    String fileName;
    String addedTaskID;
    String taskNameStr;
    String taskBodyStr;
    String newestFileID;

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener newTaskCreateListener = new View.OnClickListener() {
        public void onClick(View v) {

            // grab new task title and body
            EditText taskName = findViewById(R.id.newTaskNamePT);
            taskNameStr = taskName.getText().toString();
            EditText taskBody = findViewById(R.id.newTaskBodyPT);
            taskBodyStr = taskBody.getText().toString();

            CreateTaskInput newTask = CreateTaskInput.builder().
                    title(taskNameStr).
                    body(taskBodyStr).
                    state("new").
                    taskTeamId(selectedTeamID).
                    build();

            // create the new task
            mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(newTask).build()).enqueue(
                    new GraphQLCall.Callback<CreateTaskMutation.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response) {
                            // store the added task id
                            addedTaskID = response.data().createTask().id();

                            // upload file to S3
                            if(fileUri != null) {
                                CreateFileInput createFileInput = CreateFileInput.builder().
                                        name(fileName).
                                        fileTaskId(addedTaskID).
                                        build();


                                // create the new file in DynamoDB
                                mAWSAppSyncClient.mutate(CreateFileMutation.builder().input(createFileInput).build()).enqueue(
                                        new GraphQLCall.Callback<CreateFileMutation.Data>() {
                                            @Override
                                            public void onResponse(@Nonnull Response<CreateFileMutation.Data> response) {
                                                Log.i(TAG, "created file id: " + response.data().createFile().id());
                                                Log.i(TAG, "created file task id: " + response.data().createFile().task().id());

                                                newestFileID = response.data().createFile().id();
                                                uploadWithTransferUtility(fileUri);
                                            }

                                            @Override
                                            public void onFailure(@Nonnull ApolloException e) {
                                                Log.i(TAG, "error creating file");
                                            }
                                        }
                                );
                            }

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

        Button btnChooseFile = this.findViewById(R.id.btn_choose_file);

        btnChooseFile.setOnClickListener(v -> {
            Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICKFILE_REQUEST_CODE);
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICKFILE_REQUEST_CODE && resultCode == -1 && null != data) {
            fileUri = data.getData();
            Log.i(TAG, "fileUri: " + fileUri);
            fileName = getFileName(fileUri);
            Log.i(TAG, "fileName: " + fileName);
            TextView tvItemPath = this.findViewById(R.id.tv_file_path);
            tvItemPath.setText(fileName);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

    public void uploadWithTransferUtility(Uri uri) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/" + fileName + newestFileID,
                        new File(filePath));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception e) {
                Log.e(TAG, "error uploading file: " + e);
            }

        });

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }
}