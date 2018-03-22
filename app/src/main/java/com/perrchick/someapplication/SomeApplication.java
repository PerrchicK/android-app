package com.perrchick.someapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by perrchick on 12/16/15.
 */
public class SomeApplication extends android.app.Application {

    private enum ContextHolder {
        applicationContext;
        private Context context; // Saving context in enum should be moere secure
    }

    private static WeakReference<Activity> _topActivity;
    private static boolean isApplicationInForeground;

    @Nullable
    public static Activity getTopActivity() { return _topActivity != null ? _topActivity.get() : null; }
    public static Context getContext() { return ContextHolder.applicationContext.context; }
    public static boolean isInForeground() { return isApplicationInForeground; }

    @Override
    public void onCreate() {
        super.onCreate();

        ContextHolder.applicationContext.context = getApplicationContext();
        // From: https://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());
    }

    private static class AppLifecycleTracker implements SomeApplication.ActivityLifecycleCallbacks  {
        private static final String TAG = AppLifecycleTracker.class.getSimpleName();
        private int numStarted = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            _topActivity = new WeakReference<>(activity);

            if (numStarted++ == 0) {
                isApplicationInForeground = false;
                Log.d(TAG, "onActivityStopped: app went to foreground");
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--numStarted == 0) {
                isApplicationInForeground = false;
                Log.d(TAG, "onActivityStopped: app went to background");
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}