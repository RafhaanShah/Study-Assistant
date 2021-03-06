package com.rafhaanshah.studyassistant;

import android.app.Application;

import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.io.File;

import io.realm.Realm;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Uses LolliPin library from https://github.com/omadahealth/LolliPin
        LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, LockScreenActivity.class);
        lockManager.getAppLock().setTimeout(1000);
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);
        lockManager.getAppLock().setLogoId(R.drawable.ic_lock_black_24dp);

        // Uses Realm library from https://realm.io/
        Realm.init(MainApplication.this);

        Notifier.createNotificationChannel(MainApplication.this);

        File directory = HelperUtils.getLectureDirectory(MainApplication.this);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
