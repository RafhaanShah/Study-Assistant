package com.rafhaanshah.studyassistant.schedule;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ScheduleItem extends RealmObject {

    static final String ScheduleItem_ID = "ID";
    static final String ScheduleItem_TITLE = "title";
    static final String ScheduleItem_TYPE = "type";
    static final String ScheduleItem_NOTES = "notes";
    static final String ScheduleItem_TIME = "time";
    static final String ScheduleItem_COMPLETED = "completed";

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
        HOMEWORK, COURSEWORK, TEST, EXAM
    }
}


