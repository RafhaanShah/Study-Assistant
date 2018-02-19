package com.rafhaanshah.studyassistant.schedule;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ScheduleEvent extends RealmObject {

    static final String ScheduleEvent_ID = "ID";
    static final String ScheduleEvent_TITLE = "title";
    static final String ScheduleEvent_TYPE = "type";
    static final String ScheduleEvent_NOTES = "notes";
    static final String ScheduleEvent_TIME = "time";
    static final String ScheduleEvent_COMPLETED = "completed";

    @PrimaryKey
    private int ID;
    @Required
    private String title, notes, type;
    @Required
    private Long time, reminderTime;
    private boolean completed, reminder;

    int getID() {
        return ID;
    }

    void setID(int ID) {
        this.ID = ID;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    ScheduleItemType getType() {
        return ScheduleItemType.valueOf(type);
    }

    void setType(ScheduleItemType type) {
        this.type = type.name();
    }

    String getNotes() {
        return notes;
    }

    void setNotes(String notes) {
        this.notes = notes;
    }

    Long getTime() {
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

    Long getReminderTime() {
        return reminderTime;
    }

    void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
    }

    boolean isReminder() {
        return reminder;
    }

    void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public enum ScheduleItemType {
        HOMEWORK, TEST, COURSEWORK, EXAM
    }
}


