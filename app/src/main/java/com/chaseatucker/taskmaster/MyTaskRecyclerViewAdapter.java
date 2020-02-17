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
    private final OnTaskListener mOnTaskListener;


    public MyTaskRecyclerViewAdapter(List<Task> items, OnListFragmentInteractionListener listener,
                                     OnTaskListener onTaskListener) {
        mValues = items;
        mListener = listener;
        mOnTaskListener = onTaskListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);
        return new ViewHolder(view, mOnTaskListener);
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
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mStateView;
        public final TextView mBodyView;
        public Task mItem;
        OnTaskListener onTaskListener;

        public ViewHolder(View view, OnTaskListener onTaskListener) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.fragmentTaskTitle);
            mStateView = view.findViewById(R.id.fragmentTaskState);
            mBodyView = view.findViewById(R.id.fragmentTaskBody);
            this.onTaskListener = onTaskListener;

            mView.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mStateView.getText() + "'";
        }


        @Override
        public void onClick(View v) {
            onTaskListener.onTaskClick(getAdapterPosition());
        }
    }

    // Got help from: https://www.youtube.com/watch?v=69C1ljfDvl0
    public interface OnTaskListener{
        void onTaskClick(int position);
    }
}
