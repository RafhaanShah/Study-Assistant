package com.rafhaanshah.studyassistant;

import android.app.Application;

import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.io.File;

import io.realm.Realm;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(MainApplication.this);

        File directory = HelperUtils.getLectureDirectory(MainApplication.this);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Notifier.createNotificationChannel(MainApplication.this);
    }
}
