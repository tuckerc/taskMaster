package com.chaseatucker.taskmaster.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.chaseatucker.taskmaster.model.Task;

@Database(entities = {Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskMasterDao taskMasterDao();
}
