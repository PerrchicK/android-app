package com.perrchick.onlinesharedpreferences;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by perrchick on 1/17/16.
 */
public class SyncedSharedPreferences {

    public interface SyncedSharedPreferencesListener {
        void onSyncedSharedPreferencesChanged(String key, String value);
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
    private HashMap<String, String> keysAndValues;
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
    public static SyncedSharedPreferences getSyncedSharedPreferences(Context context, @Nullable String firebaseAppUrl, @Nullable SyncedSharedPreferencesListener listener) {
        if (firebaseAppUrl == null) {
            // Use default
            firebaseAppUrl = FIREBASE_APP_URL;
        }
        return new SyncedSharedPreferences(context, firebaseAppUrl, listener);
    }

    private SyncedSharedPreferences(Context context, String firebaseAppUrl, SyncedSharedPreferencesListener syncedSharedPreferencesListener) {
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
                synchronized (SyncedSharedPreferences.this) {
                    if (keysAndValues == null) {
                        keysAndValues = new HashMap<>();
                    }
                }

                String deltaKey = "";
                String deltaValue = "";

                // Find the delta between the server's snapshot and the local hash map
                for (DataSnapshot data:children) {
                    // New key? or modified value?
                    if ( (keysAndValues.get(data.getKey()) == null) || (keysAndValues.get(data.getKey())) != data.getValue()) {
                        deltaKey = data.getKey();
                        deltaValue = data.getValue().toString();
                        keysAndValues.put(deltaKey, deltaValue);
                    }
                }

                synchronized (keysAndValuesLocker) {
                    // Tell anyone who waited and used this lock object
                    keysAndValuesLocker.notify();
                }

                Log.v(TAG, "some value changed: " + "<" + deltaKey + "," + deltaValue + ">");
                if (listener != null) {
                    listener.onSyncedSharedPreferencesChanged(deltaKey, deltaValue);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.toString());
            }
        });
//        firebaseRef.keepSynced(true);

//        this.keyValueObjectContainer = new SyncedObjectWrapper(packageName);
        keysAndValues = null;
    }

    public interface GetAllObjectsCallback {
        void done(HashMap<String, String> objects, Exception e);
    }
    private interface GetObjectCallback {
        void done(Object value, Exception e);
    }
    public interface GetStringCallback {
        void done(String value, Exception e);
    }
    public interface CommitCallback {
        void done(Exception e);
    }
    public interface RemoveCallback {
        void done(Exception e);
    }

    /**
     * Puts a string in the shared preferences and synchronizes with firebase cloud.
     * @param key      The key that identifies the value
     * @param value    The String value that should persist online
     */
    public SyncedSharedPreferences putString(String key, String value) {
        packageFirebaseRef.child(key).setValue(value);
//        packageFirebaseRef.child(SAVED_OBJECT_VALUE).setValue(value);
//        keyValueObjectContainer.putString(key, value);

        return this;
    }

    /**
     * Asynchronously removes the value and the key from the online shared preferences, without invoking a callback
     * @param key               The key that identifies the value
     */
    public void remove(final String key) {
        this.remove(key, null);
    }

    /**
     * Asynchronously removes the value and the key from the online shared preferences
     * @param key               The key that identifies the value
     * @param removeCallback    The callback object that will be called after the remove is done
     */
    public void remove(final String key, final RemoveCallback removeCallback) {
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
                    if (keysAndValues == null) {
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
                            callback.done(keysAndValues == null ? null : keysAndValues.get(key), keysAndValues == null ? new TimeoutException("Firebase connection took too long") : null);
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
                    if (keysAndValues == null) {
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
                            callback.done(keysAndValues == null ? null : new HashMap<>(keysAndValues), keysAndValues == null ? new TimeoutException("Firebase connection took too long") : null);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }).start();
    }
}