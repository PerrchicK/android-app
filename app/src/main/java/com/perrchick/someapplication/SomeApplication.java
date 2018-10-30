package com.perrchick.someapplication;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.perrchick.someapplication.utilities.AppLogger;
import com.perrchick.someapplication.utilities.Synchronizer;

import java.lang.ref.WeakReference;

/**
 * Created by perrchick on 12/16/15.
 */
public class SomeApplication extends android.app.Application {

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

    @Nullable
    public static Activity getTopActivity() { return ApplicationHolder.sharedInstance.application.topActivity != null ? ApplicationHolder.sharedInstance.application.topActivity.get() : null; }
    public static Context getContext() { return ApplicationHolder.sharedInstance.application.getApplicationContext(); }
    public static void setContext(Context context) {
        if (context == null) return;
        if (!(context.getApplicationContext() instanceof SomeApplication)) return;

        ApplicationHolder.sharedInstance.application = (SomeApplication) context.getApplicationContext();
    }

    public static boolean isInForeground() { return ApplicationHolder.sharedInstance.application.isApplicationInForeground; }
    public static final boolean isReleaseVersion;
    static {
        isReleaseVersion = !BuildConfig.DEBUG;
    }
    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, 0);
    }
    public static void runOnUiThread(Runnable runnable, long delayMillis) {
        if (delayMillis > 0) {
            ApplicationHolder.sharedInstance.application.mainThreadHandler.postDelayed(runnable, delayMillis);
        } else {
            ApplicationHolder.sharedInstance.application.mainThreadHandler.post(runnable);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mainThreadHandler = new Handler(Looper.getMainLooper());
        ApplicationHolder.sharedInstance.application = this;
        // From: https://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo
        registerActivityLifecycleCallbacks(new AppLifecycleTracker());

        Stetho.initializeWithDefaults(this);

        exampleForSyncedAsyncOperations();
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
                LocalBroadcastReceiver.notify(LocalBroadcastReceiver.APPLICATION_GOING_FOREGROUND);
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
                LocalBroadcastReceiver.notify(LocalBroadcastReceiver.APPLICATION_GOING_BACKGROUND);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    static public class LocalBroadcastReceiver extends BroadcastReceiver {

        public static final String APPLICATION_GOING_BACKGROUND = BuildConfig.APPLICATION_ID + " - yo";
        public static final String APPLICATION_GOING_FOREGROUND = BuildConfig.APPLICATION_ID + " - yoyo";

        public interface PrivateBroadcastListener {
            void onBroadcastReceived(@NonNull Intent intent, LocalBroadcastReceiver receiver);
        }

        private final String[] actions;
        @Nullable
        private PrivateBroadcastListener receiverListener;

        /**
         * The receiver will live as long as the context lives. Therefore we will pass the application context in most of the times.
         * @param actionsToListen
         */
        private LocalBroadcastReceiver(String[] actionsToListen) {
            actions = actionsToListen;
            IntentFilter intentFilter = new IntentFilter();
            if (actionsToListen.length == 0) return;

            for (int i = 0; i < actionsToListen.length; i++) {
                intentFilter.addAction(actionsToListen[i]);
            }
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(this, intentFilter);
        }

        public void setListener(PrivateBroadcastListener listener) {
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

        public static LocalBroadcastReceiver createNewReceiver(PrivateBroadcastListener listener, String... action) {
            LocalBroadcastReceiver scenesReceiver = new LocalBroadcastReceiver(action);
            scenesReceiver.setListener(listener);
            return scenesReceiver;
        }

        public static void notify(String action) {
            notify(action, null);
        }

        public static void notify(String action, Bundle extraValues) {
            Intent broadcastIntent = new Intent(getContext(), LocalBroadcastReceiver.class).setAction(action);
            if (extraValues != null && extraValues.size() > 0) {
                broadcastIntent.putExtras(extraValues);
            }

            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);
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

    private static Synchronizer.SyncedSynchronizer<Boolean> onApplicationEntersForeground;
    private void exampleForSyncedAsyncOperations() {

        onApplicationEntersForeground = Synchronizer.makeSyncedSynchronizer();
        onApplicationEntersForeground.addOperation(new Synchronizer.OperationHolder<Boolean>() {
            @Override
            public void onMyTurn(final Synchronizer.SyncedSynchronizer<Boolean> synchronizer) {
                Log.d(TAG, "1st operation: login");
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
                Log.d(TAG, "2nd operation: register for remote notification");
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
                Log.d(TAG, "3rd operation: upload token data to server");
                synchronizer.lastResult();
                // When done, the app should run: synchronizer.doNext();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPushTokenFinishedUpdateRemotely();
                    }
                }, 700);
            }
        });

        onApplicationEntersForeground.doNext(); // Login
    }

    void onLoggedIn() {
        onApplicationEntersForeground.doNext(true);
    }

    void onPushTokenReceived() {
        onApplicationEntersForeground.doNext(false);
    }

    void onPushTokenFinishedUpdateRemotely() {
        onApplicationEntersForeground.doNext(null); // Won't run anything 'onAllDone()'
    }

}