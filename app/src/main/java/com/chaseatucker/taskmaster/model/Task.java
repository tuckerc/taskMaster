package com.chaseatucker.taskmaster.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey
    private long id;
    @ColumnInfo
    private String title;
    @ColumnInfo
    private String body;
    @ColumnInfo
    private String state;

    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.state = "new";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return title + " (" + state + "): \n" + body;
    }
}
