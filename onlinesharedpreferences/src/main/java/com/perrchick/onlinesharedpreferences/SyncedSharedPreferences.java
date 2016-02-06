package com.perrchick.onlinesharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Created by perrchick on 1/17/16.
 */
public class SyncedSharedPreferences {

    public interface SyncedSharedPreferencesListener {
        enum SyncedSharedPreferencesChangeType {
            NoChange,
            Added,
            Modified,
            Removed
        }
        void onSyncedSharedPreferencesChanged(SyncedSharedPreferencesChangeType changeType,String key, String value);
    }

    private static final String TAG = SyncedSharedPreferences.class.getSimpleName();
    private static final long TIME_OUT_MILLIS = 10000;
    private static boolean shouldInitializeFireBase = true;

    private final SyncedSharedPreferencesListener listener;
    // To prevent overriding by similar keys, there's another foreign key that will make this combination unique
    private final String packageName;
    public static final String FIREBASE_APP_URL = "https://boiling-inferno-8318.firebaseio.com/";
    private final Firebase packageFirebaseRef;
    private final Context context;
    SharedPreferences localKeysAndValues;
    private Object keysAndValuesLocker = new Object();

    /**
     * Gets a new instance of SyncedSharedPreferences, managed by Firebase
     * @param context The application's context
     * @return SyncedSharedPreferences new instance
     */
    public static SyncedSharedPreferences getSyncedSharedPreferences(Context context) {
        return getSyncedSharedPreferences(context, null);
    }

    /**
     * Gets a new instance of SyncedSharedPreferences, managed by Firebase
     * @param context The application's context
     * @param listener The listener for added keys or changed values, may be null
     * @return SyncedSharedPreferences new instance
     */
    public static SyncedSharedPreferences getSyncedSharedPreferences(Context context, SyncedSharedPreferencesListener listener) {
        return getSyncedSharedPreferences(context, null, listener);
    }

    /**
     *
     * Gets a new instance of SyncedSharedPreferences, managed by Firebase
     * @param context The application's context
     * @param firebaseAppUrl The application ID on your Firebase project, if null the default project will be in use
     * @param listener The listener for added keys or changed values, may be null
     * @return SyncedSharedPreferences new instance
     */
    public static SyncedSharedPreferences getSyncedSharedPreferences(Context context, String firebaseAppUrl, SyncedSharedPreferencesListener listener) {
        if (firebaseAppUrl == null) {
            // Use default
            firebaseAppUrl = FIREBASE_APP_URL;
        }
        return new SyncedSharedPreferences(context, firebaseAppUrl, listener);
    }

    private SyncedSharedPreferences(final Context context, String firebaseAppUrl, SyncedSharedPreferencesListener syncedSharedPreferencesListener) {
        this.listener = syncedSharedPreferencesListener;
        this.context = context;
        packageName = context.getPackageName().replace(".", "-");

        Log.v(TAG, "Initializing integration with Firebase");

        if (shouldInitializeFireBase) {
            Firebase.setAndroidContext(context);
            shouldInitializeFireBase = false;
        }

        packageFirebaseRef = new Firebase(firebaseAppUrl).child(packageName);
        packageFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                synchronized (keysAndValuesLocker) {
                    if (localKeysAndValues == null) {
                        localKeysAndValues = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                    }
                }

                String deltaKey = "";
                String deltaValue = "";
                SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType changeType;
                Set<String> oldKeys = localKeysAndValues.getAll().keySet();

                // Find the delta between the server's snapshot and the local hash map
                for (DataSnapshot data:children) {
                    changeType = SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.NoChange;

                    // New key? or modified value?
                    if ((localKeysAndValues.getString(data.getKey(), null) == null)) {
                        changeType = SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.Added;
                    } else if ((localKeysAndValues.getString(data.getKey(), "").compareTo(data.getValue().toString()) != 0)) {
                        changeType = SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.Modified;
                    }

                    if (SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.Added.compareTo(changeType) == 0 ||
                            SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.Modified.compareTo(changeType) == 0) {
                        deltaKey = data.getKey();
                        deltaValue = data.getValue().toString();
                        localKeysAndValues.edit().putString(deltaKey, deltaValue).apply();

                        Log.v(TAG, "some value changed: " + "<" + deltaKey + "," + deltaValue + ">");
                        if (listener != null) {
                            listener.onSyncedSharedPreferencesChanged(changeType, deltaKey, deltaValue);
                        }
                    }

                    oldKeys.remove(data.getKey());
                }

                if (oldKeys.size() == 1) {
                    changeType = SyncedSharedPreferencesListener.SyncedSharedPreferencesChangeType.Removed;
                    deltaKey = oldKeys.toArray()[0].toString();
                    deltaValue = localKeysAndValues.getString(deltaKey, "");
                    localKeysAndValues.edit().remove(deltaKey).commit();

                    Log.v(TAG, "some value changed: " + "<" + deltaKey + "," + deltaValue + ">");
                    if (listener != null) {
                        listener.onSyncedSharedPreferencesChanged(changeType, deltaKey, deltaValue);
                    }
                }

                synchronized (keysAndValuesLocker) {
                    // Tell anyone who waited and used this lock object
                    keysAndValuesLocker.notify();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.toString());
            }
        });

        localKeysAndValues = null;
    }

    public interface GetAllObjectsCallback {
        void done(Map<String, ?> objects, Exception e);
    }
    public interface GetStringCallback {
        void done(String value, Exception e);
    }

    /**
     * Puts a string in the shared preferences and synchronizes with firebase cloud.
     * @param key      The key that identifies the value
     * @param value    The String value that should persist online
     */
    public SyncedSharedPreferences putString(String key, String value) {
        packageFirebaseRef.child(key).setValue(value);
        return this;
    }

    /**
     * Asynchronously removes the value and the key from the online shared preferences, without invoking a callback
     * @param key               The key that identifies the value
     */
    public void remove(final String key) {
        Log.v(TAG, "Removing '" + key + "'...");
        packageFirebaseRef.child(key).removeValue();
    }

    /**
     * Gets a string from the cloud
     * @param key         The key that identifies the value
     * @param callback    The callback that should be called after the value is fetched
     */
    public void getString(final String key, final GetStringCallback callback) {
        // Guard
        if (callback == null){
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (keysAndValuesLocker) {
                    if (localKeysAndValues == null) {
                        try {
                            keysAndValuesLocker.wait(TIME_OUT_MILLIS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            callback.done(localKeysAndValues == null ? null : localKeysAndValues.getString(key, ""), localKeysAndValues == null ? new TimeoutException("Firebase connection took too long") : null);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }).start();
    }

    /**
     * Gets all objects in the online shared preferences
     * @param callback
     */
    public void getAllObjects(final GetAllObjectsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (keysAndValuesLocker) {
                    if (localKeysAndValues == null) {
                        try {
                            keysAndValuesLocker.wait(TIME_OUT_MILLIS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            callback.done(localKeysAndValues == null ? null : localKeysAndValues.getAll(), localKeysAndValues == null ? new TimeoutException("Firebase connection took too long") : null);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }).start();
    }
}