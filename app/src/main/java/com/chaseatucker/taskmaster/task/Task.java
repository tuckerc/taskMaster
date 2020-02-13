package com.chaseatucker.taskmaster.task;

/**
 * Helper class for providing sample body for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Task {

    public final String title;
    public final String body;
    public final String status;

    public Task(String title, String body, String status) {
        this.title = title;
        this.body = body;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }
}
