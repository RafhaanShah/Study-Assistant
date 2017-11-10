package com.rafhaanshah.studyassistant.schedule;

import io.realm.RealmObject;

public class ScheduleItem extends RealmObject {
    private int ID;
    private String title, type, notes;
    private Long time;
    private boolean completed;

    int getID() {
        return ID;
    }

    void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    String getNotes() {
        return notes;
    }

    void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getTime() {
        return time;
    }

    void setTime(Long time) {
        this.time = time;
    }

    boolean isCompleted() {
        return completed;
    }

    void setCompleted(boolean completed) {
        this.completed = completed;
    }
}


