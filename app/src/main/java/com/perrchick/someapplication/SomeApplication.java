package com.perrchick.someapplication;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.stetho.Stetho;
import com.perrchick.someapplication.data.FirebaseHelper;
import com.perrchick.someapplication.service.BackgroundLocationWorker;
import com.perrchick.someapplication.service.SomeJobService;
import com.perrchick.someapplication.utilities.AppLogger;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.perrchick.someapplication.utilities.Synchronizer;
import com.squareup.leakcanary.LeakCanary;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Created by perrchick on 12/16/15.
 */
public class SomeApplication extends android.app.Application implements Configuration.Provider {
    static {
        isReleaseVersion = !BuildConfig.DEBUG;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }

    /**
     * Holds the application context in a safe static reference.
     */
    private enum ApplicationHolder {
        /**
         * A singleton-like object.
         */
        sharedInstance;

        /**
         * Holds the application context in a safe static reference.
         * <br/><br/>
         * Read more at:
         * https://dzone.com/articles/java-singletons-using-enum
         * https://dzone.com/articles/singleton-bill-pugh-solution-or-enum
         */
        private SomeApplication application; // Saving context in enum shouldn't create memory leaks, because eventually the application memory holds a reference to itself and he GC supports that.
    }

    private static final String TAG = SomeApplication.class.getSimpleName();
    private WeakReference<Activity> topActivity;
    private boolean isApplicationInForeground;
    private Handler mainThreadHandler;
    private Handler appBackgroundHandler;

    @Nullable
    public static Activity getTopActivity() { return ApplicationHolder.sharedInstance.application.topActivity != null ? ApplicationHolder.sharedInstance.application.topActivity.get() : null; }
    public static Context getContext() { return ApplicationHolder.sharedInstance.application.getApplicationContext(); }
    public static SomeApplication shared() { return ApplicationHolder.sharedInstance.application; }
    public static void setContext(Context context) {
        if (context == null) return;
        if (!(context.getApplicationContext() instanceof SomeApplication)) return;
        // This method is called before its `onCreate` method: https://www.youtube.com/watch?v=L3Cw5clOnxs

        ApplicationHolder.sharedInstance.application = (SomeApplication) context.getApplicationContext();
    }

    public static boolean isInForeground() { return ApplicationHolder.sharedInstance.application.isApplicationInForeground; }
    public static final boolean isReleaseVersion;

    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, 0);
    }

    public static void runOnUiThread(Runnable runnable, long delayMillis) {
        if (delayMillis > 0) {
            ApplicationHolder.sharedInstance.application.mainThreadHandler.postDelayed(runnable, delayMillis);
        } else {
            //new Handler(Looper.getMainLooper()).post(runnable);
            ApplicationHolder.sharedInstance.application.mainThreadHandler.post(runnable);
        }
    }

    public static void runInBackgroundThread(Runnable runnable) {
        ApplicationHolder.sharedInstance.application.appBackgroundHandler.post(runnable);
    }

    public static void cancelBackgroundThread(Runnable runnable) {
        ApplicationHolder.sharedInstance.application.appBackgroundHandler.removeCallbacks(runnable);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerBackgroundJobService();

        mainThreadHandler = new Handler(Looper.getMainLooper());

        HandlerThread appBackgroundThread = new HandlerThread(SomeApplication.class.getSimpleName() + "_BackgroundThread");
        appBackgroundThread.start();
        appBackgroundHandler = new Handler(appBackgroundThread.getLooper());

        // From: https://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());

        Stetho.initializeWithDefaults(this);

        exampleForSyncedAsyncOperations();

        FirebaseHelper.initialize();

        LeakCanary.install(this); // Normal app init code...
    }

    private void registerBackgroundJobService() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest locationWork = new PeriodicWorkRequest
                .Builder(BackgroundLocationWorker.class, BackgroundLocationWorker.INTERVAL_IN_MINUTS, TimeUnit.MINUTES)
                .addTag(BackgroundLocationWorker.TAG)
                .setConstraints(constraints).build();

        // Schedule and override the existing workers if any. (to prevent duplicates)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(BackgroundLocationWorker.NAME, ExistingPeriodicWorkPolicy.REPLACE, locationWork);
    }

    // From Oreo and on the Android OS is embracing the iOS attitude about background tasks
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static boolean registerJobService() { // From: https://medium.com/google-developers/scheduling-jobs-like-a-pro-with-jobscheduler-286ef8510129
        JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) return false;
        PersistableBundle extras = new PersistableBundle();
        extras.putLong("start", PerrFuncs.getCurrentTimestamp());
        JobInfo jobInfo = new JobInfo.Builder(SomeJobService.JOB_ID,
                new ComponentName(getContext(), SomeJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                //.setPeriodic(SomeJobService.JOB_PERIOD_MILLISECONDS) // Recurring job
                //.setRequiresCharging(false)
                //.setRequiresDeviceIdle(false)
                .setExtras(extras)
                .setPersisted(true) // Persisted across a reboot, requires adding another permission to Manifest.xml: <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
                .build();
        jobScheduler.schedule(jobInfo);

        // Or FirebaseJobDispatcher:
//        Job myJob = firebaseJobDispatcher.newJobBuilder() // Import in gradle using: implementation 'com.firebase:firebase-jobdispatcher:0.8.5'
//                .setService(SmartService.class)
//                .setTag(SmartService.LOCATION_SMART_JOB)
//                .setRecurring(true)
//                .setLifetime(FOREVER)
//                .setTrigger(Trigger.executionWindow(0, 60 * 5))
//                .setReplaceCurrent(false)
//                .setConstraints(ON_ANY_NETWORK)
//                .build();
//        firebaseJobDispatcher.mustSchedule(myJob);

        // Or WorkManager: https://developer.android.com/topic/libraries/architecture/workmanager/

        // Thanks to Yonatan Levin, you can gain more knowledge from: https://medium.com/google-developer-experts/services-the-life-with-without-and-worker-6933111d62a6
        return true;
    }

    private class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks  {
        private final String TAG = AppLifecycleTracker.class.getSimpleName();
        private int numStarted = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

        @Override
        public void onActivityStarted(Activity activity) {
            topActivity = new WeakReference<>(activity);

            if (numStarted++ == 0) {
                isApplicationInForeground = false;
                Log.d(TAG, "onActivityStopped: app went to foreground");
                PrivateEventBus.notify(PrivateEventBus.Action.APPLICATION_GOING_FOREGROUND);
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
                PrivateEventBus.notify(PrivateEventBus.Action.APPLICATION_GOING_BACKGROUND);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    static public class PrivateEventBus {

        public class Action {
            public static final String APPLICATION_GOING_BACKGROUND = BuildConfig.APPLICATION_ID + " - yo";
            public static final String APPLICATION_GOING_FOREGROUND = BuildConfig.APPLICATION_ID + " - yoyo";
            public static final String FIREBASE_IS_READY = BuildConfig.APPLICATION_ID + "runs whenever firebase is connected and has an updated clock diff";
        }


        public interface BroadcastReceiverListener {
            void onBroadcastReceived(@NonNull Intent intent, PrivateEventBus.Receiver receiver);
        }


        static class Receiver extends BroadcastReceiver {
            private final String[] actions;
            @Nullable
            private BroadcastReceiverListener receiverListener;
            /**
             * The receiver will live as long as the context lives. Therefore we will pass the application context in most of the times.
             * @param actionsToListen
             */
            private Receiver(String[] actionsToListen) {
                actions = actionsToListen;
                IntentFilter intentFilter = new IntentFilter();
                if (actionsToListen.length == 0) return;

                for (String actionToListen : actionsToListen) {
                    intentFilter.addAction(actionToListen);
                }

                LocalBroadcastManager.getInstance(getContext()).registerReceiver(this, intentFilter);
            }

            public void setListener(BroadcastReceiverListener listener) {
                this.receiverListener = listener;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                receivedAction(intent);
            }

            private void receivedAction(Intent intent) {
                if (receiverListener == null) {
                    AppLogger.error(TAG, "onBroadcastReceived: Missing listener! Intent == " + intent);
                } else if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                    receiverListener.onBroadcastReceived(intent, this);
                }
            }

            public void quit() {
                try {
                    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
                } catch (Exception e) {
                    AppLogger.error(TAG, "removeReceiver: couldn't quitReceiving receiver: " + e.getMessage());
                }
                receiverListener = null;
            }

            public String[] getActions() {
                return actions;
            }
        }
        public static PrivateEventBus.Receiver createNewReceiver(BroadcastReceiverListener listener, String... actions) {
            PrivateEventBus.Receiver receiver = new Receiver(actions);
            receiver.setListener(listener);
            return receiver;
        }

        public static void notify(String action) {
            notify(action, null);
        }

        public static void notify(String action, Bundle extraValues) {
            Intent broadcastIntent = new Intent(getContext(), PrivateEventBus.class).setAction(action);
            if (extraValues != null && extraValues.size() > 0) {
                broadcastIntent.putExtras(extraValues);
            }

            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
        }

    }

    private static Synchronizer.SyncedSynchronizer<Boolean> onApplicationEntersForeground;
    private void exampleForSyncedAsyncOperations() {

        onApplicationEntersForeground = Synchronizer.makeSyncedSynchronizer();
        onApplicationEntersForeground.addOperation(new Synchronizer.OperationHolder<Boolean>() {
            @Override
            public void onMyTurn(final Synchronizer.SyncedSynchronizer<Boolean> synchronizer) {
                AppLogger.log(TAG, "1st operation: login");
                // When done, the app should run: synchronizer.doNext();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onLoggedIn();
                    }
                }, 400);
            }
        }).addOperation(new Synchronizer.OperationHolder<Boolean>() {
            @Override
            public void onMyTurn(final Synchronizer.SyncedSynchronizer<Boolean> synchronizer) {
                AppLogger.log(TAG, "2nd operation: register for remote notification");
                // When done, the app should run: synchronizer.doNext();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPushTokenReceived();
                    }
                }, 800);
            }
        }).addOperation(new Synchronizer.OperationHolder<Boolean>() {
            @Override
            public void onMyTurn(final Synchronizer.SyncedSynchronizer<Boolean> synchronizer) {
                AppLogger.log(TAG, "3rd operation: upload token data to server");
                AppLogger.log(TAG, "synchronizer.lastResult() == " + synchronizer.lastResult());
                // When done, the app should run: synchronizer.doNext();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPushTokenFinishedUpdateRemotely();
                    }
                }, 700);
            }
        });

        onApplicationEntersForeground.carryOn(); // Login
    }

    void onLoggedIn() {
        onApplicationEntersForeground.carryOn(true);
    }

    void onPushTokenReceived() {
        onApplicationEntersForeground.carryOn(false);
    }

    void onPushTokenFinishedUpdateRemotely() {
        onApplicationEntersForeground.carryOn(null); // Won't run anything 'onAllDone()'
    }

    private void doInBackground() {
    }
}