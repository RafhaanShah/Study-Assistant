package com.rafhaanshah.studyassistant;

import android.app.Application;
import android.os.StrictMode;

import java.io.File;

import io.realm.Realm;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File directory = new File(getFilesDir().getAbsolutePath() + File.separator + "lectures");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
