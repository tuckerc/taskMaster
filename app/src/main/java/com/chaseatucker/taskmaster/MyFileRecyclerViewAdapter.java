package com.chaseatucker.taskmaster;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListFilesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.chaseatucker.taskmaster.FileFragment.OnListFragmentInteractionListener;

import java.io.File;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFileRecyclerViewAdapter extends RecyclerView.Adapter<MyFileRecyclerViewAdapter.ViewHolder> {

    String TAG = "cat.MyFileRecyclerViewAdapter";
    private final List<ListFilesQuery.Item> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyFileRecyclerViewAdapter(List<ListFilesQuery.Item> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mFileNameView.setText(mValues.get(position).name());

        holder.mFileOpenBtn.setOnClickListener(v -> {
            Log.i(TAG, "file clicked");
            // download the file
            Handler h = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    downloadWithTransferUtility(v, holder.mItem.name(), holder.mItem.id());
                }
            };
            h.obtainMessage().sendToTarget();

            holder.mView.getContext().startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mFileNameView;
        public final Button mFileOpenBtn;
        public ListFilesQuery.Item mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mFileNameView = view.findViewById(R.id.file_fragment_file_name_tv);
            mFileOpenBtn = view.findViewById(R.id.file_fragment_open_file_btn);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mFileNameView.getText() + "'";
        }
    }

    private void downloadWithTransferUtility(View view, String fileName, String fileID) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(view.getContext().getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        TransferObserver downloadObserver =
                transferUtility.download(
                        "public/" + fileName + fileID,
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Log.i(TAG, "file download complete");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {

                Log.i(TAG, "error downloading file: " + ex);
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == downloadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d(TAG, "Bytes Transferred: " + downloadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + downloadObserver.getBytesTotal());
    }
}
