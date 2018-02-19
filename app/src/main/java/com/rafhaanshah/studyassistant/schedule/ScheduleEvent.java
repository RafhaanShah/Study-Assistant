package com.rafhaanshah.studyassistant.schedule;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ScheduleEvent extends RealmObject {

    public static final String ScheduleEvent_ID = "ID";
    public static final String ScheduleEvent_TITLE = "title";
    public static final String ScheduleEvent_TYPE = "type";
    public static final String ScheduleEvent_NOTES = "notes";
    public static final String ScheduleEvent_TIME = "time";
    public static final String ScheduleEvent_COMPLETED = "completed";
    public static final String ScheduleEvent_REMINDER = "reminder";
    public static final String ScheduleEvent_REMINDER_TIME = "reminderTime";

    @PrimaryKey
    private int ID;
    @Required
    private String title, notes, type;
    @Required
    private Long time, reminderTime;
    private boolean completed, reminder;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ScheduleItemType getType() {
        return ScheduleItemType.valueOf(type);
    }

    public void setType(ScheduleItemType type) {
        this.type = type.name();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public enum ScheduleItemType {
        HOMEWORK, TEST, COURSEWORK, EXAM
    }
}


