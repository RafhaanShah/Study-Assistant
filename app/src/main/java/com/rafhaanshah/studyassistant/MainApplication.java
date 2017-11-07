package com.rafhaanshah.studyassistant;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Raf on 07/11/2017.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);


    }
}
