package com.rafhaanshah.studyassistant;

import android.app.Application;
import android.content.Context;

import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.utils.HelperUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import io.realm.Realm;

public class MainApplication extends Application {

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, LockScreenActivity.class);
        lockManager.getAppLock().setTimeout(3600000);
        //lockManager.getAppLock().setTimeout(1000);
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);
        lockManager.getAppLock().setLogoId(R.drawable.ic_lock_black_24dp);

        Realm.init(MainApplication.this);

        Notifier.createNotificationChannel(MainApplication.this);

        File directory = HelperUtils.getLectureDirectory(MainApplication.this);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
