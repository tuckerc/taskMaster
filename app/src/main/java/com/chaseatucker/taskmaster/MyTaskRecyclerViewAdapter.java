package com.chaseatucker.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.chaseatucker.taskmaster.TaskFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link type.CreateTaskInput} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<ListTasksQuery.Item> mValues;
    private final OnListFragmentInteractionListener mListener;


    public MyTaskRecyclerViewAdapter(List<ListTasksQuery.Item> items, OnListFragmentInteractionListener listener) {
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
        holder.mTitleView.setText(mValues.get(position).title());

        holder.mView.setOnClickListener(v -> {
            // store the current task in shared prefs
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(v.getContext().getApplicationContext());
            SharedPreferences.Editor editor = p.edit();
            editor.putString("currentTaskID", mValues.get(position).id());
            editor.apply();

            Intent i = new Intent(v.getContext(), TaskDetail.class);
            i.putExtra("id", mValues.get(position).id());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public ListTasksQuery.Item mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.fragmentTaskTitle);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
