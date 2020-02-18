package com.chaseatucker.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaseatucker.taskmaster.TaskFragment.OnListFragmentInteractionListener;
import com.chaseatucker.taskmaster.model.Task;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.chaseatucker.taskmaster.model.Task} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<Task> mValues;
    private final OnListFragmentInteractionListener mListener;


    public MyTaskRecyclerViewAdapter(List<Task> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mStateView.setText(mValues.get(position).getState());
        holder.mBodyView.setText(mValues.get(position).getBody());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), TaskDetail.class);
                i.putExtra("title", holder.mTitleView.getText());
                i.putExtra("state", holder.mStateView.getText());
                i.putExtra("body", holder.mBodyView.getText());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mStateView;
        public final TextView mBodyView;
        public Task mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.fragmentTaskTitle);
            mStateView = view.findViewById(R.id.fragmentTaskState);
            mBodyView = view.findViewById(R.id.fragmentTaskBody);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mStateView.getText() + "'";
        }
    }
}
