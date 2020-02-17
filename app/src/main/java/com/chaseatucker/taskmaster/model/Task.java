package com.chaseatucker.taskmaster.model;

public class Task {

    private String title;
    private String body;
    private String state;

    public Task(String title) {
        this.title = title;
        this.body = "";
        this.state = "new";
    }

    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.state = "new";
    }

    public Task(String title, String body, String state) {
        this.title = title;
        this.body = body;
        this.state = state;
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
