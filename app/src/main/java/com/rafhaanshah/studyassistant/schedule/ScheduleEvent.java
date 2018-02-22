package com.rafhaanshah.studyassistant.schedule;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ScheduleEvent extends RealmObject {

    public static final String ScheduleEvent_ID = "ID";
    public static final String ScheduleEvent_TITLE = "TITLE";
    public static final String ScheduleEvent_TYPE = "TYPE";
    public static final String ScheduleEvent_NOTES = "NOTES";
    public static final String ScheduleEvent_TIME = "EVENT_TIME";
    public static final String ScheduleEvent_COMPLETED = "COMPLETED";
    public static final String ScheduleEvent_REMINDER = "REMINDER";
    public static final String ScheduleEvent_REMINDER_TIME = "REMINDER_TIME";

    @PrimaryKey
    private int ID;
    @Required
    private String TITLE, NOTES, TYPE;
    @Required
    private Long EVENT_TIME, REMINDER_TIME;
    private boolean COMPLETED, REMINDER;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return TITLE;
    }

    public void setTitle(String title) {
        this.TITLE = title;
    }

    public ScheduleEventType getType() {
        return ScheduleEventType.valueOf(TYPE);
    }

    public void setType(ScheduleEventType type) {
        this.TYPE = type.name();
    }

    public String getNotes() {
        return NOTES;
    }

    public void setNotes(String notes) {
        this.NOTES = notes;
    }

    public Long getEventTime() {
        return EVENT_TIME;
    }

    public void setEventTime(Long eventTime) {
        this.EVENT_TIME = eventTime;
    }

    public boolean isCompleted() {
        return COMPLETED;
    }

    public void setCompleted(boolean completed) {
        this.COMPLETED = completed;
    }

    public Long getReminderTime() {
        return REMINDER_TIME;
    }

    public void setReminderTime(Long reminderTime) {
        this.REMINDER_TIME = reminderTime;
    }

    public boolean isReminderSet() {
        return REMINDER;
    }

    public void setReminder(boolean reminder) {
        this.REMINDER = reminder;
    }

    public enum ScheduleEventType {
        HOMEWORK, TEST, COURSEWORK, EXAM
    }
}


