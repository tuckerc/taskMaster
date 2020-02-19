package com.chaseatucker.taskmaster.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.chaseatucker.taskmaster.model.Task;

import java.util.List;

@Dao
public interface TaskMasterDao {
    @Query("select * from task")
    List<Task> getAllTasks();

    @Insert
    void addTask(Task t);
}
