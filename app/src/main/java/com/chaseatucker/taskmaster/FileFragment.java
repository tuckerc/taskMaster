package com.chaseatucker.taskmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.amplify.generated.graphql.ListFilesQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateFileSubscription;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.ModelFileFilterInput;
import type.ModelIDInput;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends Fragment {

    String TAG = "cat.FileFragment";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private AWSAppSyncClient mAWSAppSyncClient;
    MyFileRecyclerViewAdapter adapter;
    List<ListFilesQuery.Item> fileList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FileFragment newInstance(int columnCount) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileList = new LinkedList<>();

        adapter = new MyFileRecyclerViewAdapter(fileList, null);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
        }

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(view.getContext().getApplicationContext())
                .awsConfiguration(new AWSConfiguration(view.getContext().getApplicationContext()))
                .build();

        OnCreateFileSubscription fileSubscription = OnCreateFileSubscription.builder().build();
        AppSyncSubscriptionCall<OnCreateFileSubscription.Data> subscriptionWatcher = mAWSAppSyncClient.subscribe(fileSubscription);
        subscriptionWatcher.execute(new AppSyncSubscriptionCall.Callback<OnCreateFileSubscription.Data>() {
            @Override
            public void onResponse(@Nonnull Response<OnCreateFileSubscription.Data> response) {
                Log.i(TAG, ".subscription: " + response.data().toString());
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, ".subscription error: " + e.toString());
            }

            @Override
            public void onCompleted() {
                Log.i(TAG, ".subscription: Subscription completed");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        fileList.clear();

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(recyclerView.getContext().getApplicationContext());
        String currentTaskID = p.getString("currentTaskID", "");
        Log.i(TAG, "currentTaskID: " + currentTaskID);
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
                                        if(item.task().id().equals(currentTaskID)) {
                                            fileList.add(item);
                                        }
                                    }
                                } else {
                                    Log.i(TAG, "ListFilesQuery empty");
                                }

                                for(ListFilesQuery.Item item : fileList) {
                                    Log.i(TAG, "file: " + item.name());
                                }

                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        };
                        h.obtainMessage().sendToTarget();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "failure in ListFilesQuery: " + e);
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ListFilesQuery.Item item);
    }
}
