package com.chaseatucker.taskmaster;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaseatucker.taskmaster.task.Task;
import com.chaseatucker.taskmaster.view.TaskFragment;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Task} and makes a call to the
 * specified {@link TaskFragment.OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    private final List<Task> mTaskList;
    private final TaskFragment.OnFragmentInteractionListener mListener;

    public MyTaskRecyclerViewAdapter(List<Task> items, TaskFragment.OnFragmentInteractionListener listener) {
        mTaskList = items;
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
        holder.mItem = mTaskList.get(position);
        holder.taskBody.setText(mTaskList.get(position).id);
        holder.taskStatus.setText(mTaskList.get(position).content);

        holder.taskTitle.setOnClickListener(new View.OnClickListener() {
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
        return mTaskList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Task task;
        public final TextView taskTitle;
        public final TextView taskBody;
        public final TextView taskStatus;

        public ViewHolder(View view) {
            super(view);
            taskTitle = view.findViewById(R.id.fra);
            taskBody = (TextView) view.findViewById(R.id.item_number);
            taskStatus = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + taskStatus.getText() + "'";
        }
    }
}
