package com.perrchick.onlinesharedpreferences;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by perrchick on 1/17/16.
 */
public class OnlineSharedPreferences {

    private static final String TAG = OnlineSharedPreferences.class.getSimpleName();
    private static final long TIME_OUT_MILLIS = 10000;
    private static boolean shouldInitializeFireBase = true;
    // To prevent overriding by similar keys, there's another foreign key that will make this combination unique
    private final String packageName;
    public static final String FIREBASE = "https://boiling-inferno-8318.firebaseio.com/";
    private final Firebase packageFirebaseRef;
    private final Context context;
    private HashMap<String, String> keysAndValues;
    private Object lock = new Object();

    /**
     * Gets a new instance of OnlineSharedPreferences, managed by Parse
     * @param context The application's context
     * @return OnlineSharedPreferences new instance
     */
    public static OnlineSharedPreferences getOnlineSharedPreferences(Context context) {
//        return new OnlineSharedPreferences(context, "6uvLKEmnnQtdRpdThttAnDneX1RxyGUjyHwpI462", "TaVVVo6EP2dufExRhznnVSHYl5YHwM9gPhvxwP00");
        return getOnlineSharedPreferences(context, FIREBASE);
    }

    /**
     *
     * Gets a new instance of OnlineSharedPreferences, managed by Parse
     * @param context The application's context
     * @param firebaseAppUrl The application ID on your Parse project, if any
     * @return OnlineSharedPreferences new instance
     */
    public static OnlineSharedPreferences getOnlineSharedPreferences(Context context, String firebaseAppUrl) {
        return new OnlineSharedPreferences(context, firebaseAppUrl);
    }

    private OnlineSharedPreferences(Context context, String firebaseAppUrl) {
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
                if (keysAndValues == null) {
                    keysAndValues = new HashMap<>();
                    for (DataSnapshot data:children) {//getValue
                        keysAndValues.put(data.getKey(), data.getValue().toString());
                    }

                    synchronized (lock) {
                        lock.notify();
                    }
                }
                Log.v(TAG, dataSnapshot.toString());
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

    // This object will contain all the <key,value> combinations
//    private final SyncedObjectWrapper keyValueObjectContainer;

    /*
    public OnlineSharedPreferences putObject(String key, Object value) {
        keyValueObjectContainer.putObject(key, value);

        return this;
    }
    */

    /**
     * Puts a string in the shared preferences, but it doesn't upload the value yet, until {@link #commitInBackground(CommitCallback)} is being called.
     * @param key      The key that identifies the value
     * @param value    The String value that should persist online
     */
    public OnlineSharedPreferences putString(String key, String value) {
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
        /*
        keyValueObjectContainer.remove(key, new RemoveCallback() {
            @Override
            public void done(Exception e) {
                if (removeCallback != null) {
                    removeCallback.done(e);
                }
                if (e == null) {
                    Log.v(TAG, "... Removed '" + key + "'");
                } else {
                    Log.e(TAG, "... Failed to remove '" + key + "'");
                    e.printStackTrace();
                }
            }
        });
        */
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
                synchronized (lock) {
                    if (keysAndValues == null) {
                        try {
                            lock.wait(TIME_OUT_MILLIS);
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

        /*
        ParseQuery<ParseSyncedObject> parseQuery = ParseQuery.getQuery(ParseSyncedObject.class);
        // Has two keys (package name + key)
        parseQuery.whereEqualTo(PACKAGE_NAME_KEY, keyValueObjectContainer.getPackageName());
        parseQuery.whereEqualTo(ParseSyncedObject.SAVED_OBJECT_KEY, key);
        Log.v(TAG, "Getting string for key '" + key + "'...");
        parseQuery.findInBackground(new FindCallback<ParseSyncedObject>() {
            @Override
            public void done(List<ParseSyncedObject> objects, ParseException e) {
                String value = null;
                if (objects.size() > 0) {
                    if (objects.get(0).getValue() instanceof String) {
                        value = (String) objects.get(0).getValue();
                    }
                }

                if (callback != null) {
                    // Should be unique
                    callback.done(value, e);
                }

                if (e == null) {
                    Log.v(TAG, "... Got object for key '" + key + "'");
                } else {
                    Log.e(TAG, "... Failed to get object for key '" + key + "'");
                    e.printStackTrace();
                }
            }
        });
        */
    }

    /*
    public void getObject(final String key, final GetObjectCallback callback) {
        // Guard
        if (callback == null){
            return;
        }

        ParseQuery<ParseSyncedObject> parseQuery = ParseQuery.getQuery(ParseSyncedObject.class);
        // Has two keys (package name + key)
        parseQuery.whereEqualTo(PACKAGE_NAME_KEY, keyValueObjectContainer.getPackageName());
        parseQuery.whereEqualTo(ParseSyncedObject.SAVED_OBJECT_KEY, key);
        Log.v(TAG, "Getting object for key '" + key + "'...");
        parseQuery.findInBackground(new FindCallback<ParseSyncedObject>() {
            @Override
            public void done(List<ParseSyncedObject> objects, ParseException e) {
                Object value = null;
                if (objects.size() > 0) {
                    value = objects.get(0).getValue();
                }
                if (callback != null) {
                    // Should be unique
                    callback.done(value, e);
                }

                if (e == null) {
                    Log.v(TAG, "... Got object for key '" + key + "'");
                } else {
                    Log.e(TAG, "... Failed to get object for key '" + key + "'");
                    e.printStackTrace();
                }
            }
        });
    }
    */

    /**
     * Gets all objects in the online shared preferences
     * @param callback
     */
    public void getAllObjects(final GetAllObjectsCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (keysAndValues == null) {
                        try {
                            lock.wait(TIME_OUT_MILLIS);
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
        /*
        final ParseQuery<ParseSyncedObject> parseQuery = ParseQuery.getQuery(ParseSyncedObject.class);
        parseQuery.whereEqualTo(PACKAGE_NAME_KEY, keyValueObjectContainer.getPackageName());
        Log.v(TAG, "Getting all objects...");
        parseQuery.findInBackground(new FindCallback<ParseSyncedObject>() {
            @Override
            public void done(List<ParseSyncedObject> objects, ParseException e) {
                HashMap<String, Object> savedObjects = new HashMap<String, Object>(objects.size());
                for (ParseSyncedObject syncedObject : objects) {
                    savedObjects.put(syncedObject.getKey(), syncedObject.getValue());
                }
                callback.done(savedObjects, e);

                if (e == null) {
                    Log.v(TAG, "... Got all (" + objects.size() + ") objects");
                } else {
                    Log.e(TAG, "... Failed to get all objects");
                    e.printStackTrace();
                }
            }
        });
        */
    }

    /**
     * Commits the changes to the cloud asynchronously without a callback
     */
    public void commitInBackground() {
        commitInBackground(null);
    }

    /**
     * Commits the changes to the cloud asynchronously
     * @param commitCallback    The callback object that will be called when the commit is done
     */
    public void commitInBackground(final CommitCallback commitCallback) {
        Log.v(TAG, "Committing in background...");
//        packageFirebaseRef.getApp()
        /*
        keyValueObjectContainer.saveInBackground(new CommitCallback() {
            @Override
            public void done(Exception e) {
                if (commitCallback != null) {
                    commitCallback.done(e);
                }

                if (e == null) {
                    Log.v(TAG, "... Committed in background");
                } else {
                    Log.e(TAG, "... Failed to commit in background");
                    e.printStackTrace();
                }
            }
        });
        */
    }

    // Adapter pattern (Object Adapter)
    private static class SyncedObject {
        String key;
        String value;

        protected SyncedObject() {
        }

        protected void putString(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /*
        protected void putObject(String key, Object value) {
            innerObject.put(ParseSyncedObject.SAVED_OBJECT_KEY, key);
            innerObject.put(ParseSyncedObject.SAVED_OBJECT_VALUE, value);
        }
        */

        protected void remove(String key, final RemoveCallback deleteCallback) {

            /*
            ParseQuery<ParseSyncedObject> parseQuery = ParseQuery.getQuery(ParseSyncedObject.class);
            parseQuery.whereEqualTo(OnlineSharedPreferences.PACKAGE_NAME_KEY, innerObject.getPackageName());
            parseQuery.whereEqualTo(ParseSyncedObject.SAVED_OBJECT_KEY, key);
            // Find the object to delete
            parseQuery.findInBackground(new FindCallback<ParseSyncedObject>() {
                @Override
                public void done(final List<ParseSyncedObject> objects, ParseException e) {
                    // Found?
                    if (objects.size() == 1) {
                        if (deleteCallback == null) {
                            objects.get(0).deleteInBackground();
                        } else {
                            objects.get(0).deleteInBackground(deleteCallback);
                        }
                    }
                }
            });
            */
        }

        /**
         * Saves the shared object
         * I'm sure there's an efficient way, I might solve it with Parse by configuring the object to have two unique keys from 'packageName" + 'key'.
         * But I wanted to have the challenge.
         *
         * Will check duplications, delete if any, and save.
         *
         * @param saveCallback The callback that holds the method to run in completion
         */
        protected void saveInBackground(final CommitCallback saveCallback) {
            /*
            ParseQuery<ParseSyncedObject> parseQuery = ParseQuery.getQuery(ParseSyncedObject.class);
            parseQuery.whereEqualTo(OnlineSharedPreferences.PACKAGE_NAME_KEY, innerObject.getPackageName());
            parseQuery.whereEqualTo(ParseSyncedObject.SAVED_OBJECT_KEY, this.innerObject.getKey());
            // (1) Find duplications
            parseQuery.findInBackground(new FindCallback<ParseSyncedObject>() {
                @Override
                public void done(final List<ParseSyncedObject> objects, ParseException e) {
                    // (2) Duplications found?
                    if (objects.size() > 0) {
                        // (3) Delete duplications (disallowed) before add new object
                        boolean finished = false;
                        ExecutorService es = Executors.newCachedThreadPool();
                        for (int i = 0; i < objects.size(); i++) {
                            final int index = i; // must be final because another thread "would like" to use that resource
                            es.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        objects.get(index).deleteInBackground().waitForCompletion();
                                    } catch (InterruptedException deletionException) {
                                        deletionException.printStackTrace();
                                    }
                                }
                            });
                        }
                        es.shutdown();
                        try {
                            finished = es.awaitTermination(1, TimeUnit.MINUTES);
                        } catch (InterruptedException waitingException) {
                            waitingException.printStackTrace();
                        }

                        if (finished) {
                            // (3.1) Save...
                            new ParseSyncedObject(innerObject.getPackageName(), innerObject.getKey(), innerObject.getValue()).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // (4) Notify for completion with callback
                                    if (saveCallback != null) {
                                        saveCallback.done(e);
                                    }
                                }
                            });
                        } else {
                            if (saveCallback != null) {
                                saveCallback.done(new ParseException(-1, "Couldn't delete (duplications) and save"));
                            }
                        }
                    } else { // Already unique, brand new
                        // (3) Save...
                        innerObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                // (4) Notify for completion with callback
                                if (saveCallback != null) {
                                    saveCallback.done(e);
                                }
                            }
                        });
                    }
                }
            });
            */
        }
    }
}