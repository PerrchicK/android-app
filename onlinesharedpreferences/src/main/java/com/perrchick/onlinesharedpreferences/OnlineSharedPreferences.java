package com.perrchick.onlinesharedpreferences;

import android.content.Context;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.perrchick.onlinesharedpreferences.backendless.BackendlessSyncedObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by perrchick on 1/17/16.
 */
public class OnlineSharedPreferences {
    private static final String SAVED_OBJECT_KEY = "key";
    private static final String SAVED_OBJECT_VALUE = "value";
    private static final String SAVED_OBJECT_PACKAGE_NAME = "packageName";

    private static final String TAG = OnlineSharedPreferences.class.getSimpleName();
    private static boolean isInitialized = false;
    BackendlessSyncedObject syncedObject;
    // This object will contain all the <key,value> combinations
    private BackendlessSyncedObject innerObject = null;

    /**
     * Gets a new instance of OnlineSharedPreferences, managed by Backendless
     * @param context The application's context
     * @return OnlineSharedPreferences new instance
     */
    public static OnlineSharedPreferences getOnlineSharedPreferences(Context context) {
        return getOnlineSharedPreferences(context, "E492F84C-D939-8870-FFAA-9E4AAEDDD200", "DF91B6EE-01F5-3A2B-FF73-2AD90DB00800");
    }

    /**
     *
     * Gets a new instance of OnlineSharedPreferences, managed by Backendless
     * @param context The application's context
     * @param appId The application ID on your Backendless project, if any
     * @param clientKey The client key on your Backendless project, if any
     * @return OnlineSharedPreferences new instance
     */
    public static OnlineSharedPreferences getOnlineSharedPreferences(Context context, String appId, String clientKey) {
        return new OnlineSharedPreferences(context, appId, clientKey);
    }

    private OnlineSharedPreferences(Context context, String appId, String secretKey) {
        Log.v(TAG, "Initializing integration with Backendless");
        if (!isInitialized) {
            Backendless.initApp(context, appId, secretKey, "v1");

            isInitialized = true;
        }

        this.innerObject = new BackendlessSyncedObject(context.getPackageName());
    }

    public interface GetAllObjectsCallback {
        void done(HashMap<String, String> objects, BackendlessException e);
    }
    public interface GetObjectCallback {
        void done(Object value, BackendlessException e);
    }
    public interface GetStringCallback {
        void done(String value, BackendlessException e);
    }
    public interface CommitCallback {
        void done(BackendlessException e);
    }
    public interface RemoveCallback {
        void done(BackendlessException e);
    }

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
        innerObject.setKey(key);
        innerObject.setValue(value);

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
        findKey(key, new BackendlessCallback<BackendlessCollection<BackendlessSyncedObject>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessSyncedObject> objects) {
                // Found?
                if (objects != null && objects.getData().size() > 0) {
                    for (BackendlessSyncedObject syncedObject : objects.getData()) {
                        Backendless.Persistence.of(BackendlessSyncedObject.class).remove(syncedObject, new BackendlessCallback<Long>() {
                            @Override
                            public void handleResponse(Long aLong) {
                                Log.v(TAG, "... Removed '" + key + "'");
                                if (removeCallback != null) {
                                    removeCallback.done(null);
                                }
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.e(TAG, "... Failed to remove '" + key + "'");
                                if (removeCallback != null) {
                                    removeCallback.done(new BackendlessException(fault.getCode(), fault.getMessage()));
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "... Failed to remove '" + key + "'");
                if (removeCallback != null) {
                    removeCallback.done(new BackendlessException(fault.getCode(), fault.getMessage()));
                }
            }
        });

        Log.v(TAG, "Removing '" + key + "'...");
    }

    private void findKey(String key, AsyncCallback<BackendlessCollection<BackendlessSyncedObject>> callback) {
        // Has two keys (package name + key)
        final BackendlessDataQuery query = new BackendlessDataQuery(SAVED_OBJECT_PACKAGE_NAME + " = '" + innerObject.getPackageName() + "'" +
                " and " + SAVED_OBJECT_KEY + " = '" + key + "'");
        Log.v(TAG, "Getting string for key '" + key + "'...");
        Backendless.Persistence.of(BackendlessSyncedObject.class).find(query, callback);
     }

    private void findKey(String key, String value, AsyncCallback<BackendlessCollection<BackendlessSyncedObject>> callback) {
        // Has two keys (package name + key)
        final BackendlessDataQuery query = new BackendlessDataQuery(SAVED_OBJECT_PACKAGE_NAME + " = '" + innerObject.getPackageName() + "'" +
                " and " + SAVED_OBJECT_KEY + " = '" + key + "'" + " and " + SAVED_OBJECT_VALUE + " = '" + value + "'");
        Log.v(TAG, "Getting string for key '" + key + "'...");
        Backendless.Persistence.of(BackendlessSyncedObject.class).find(query, callback);
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

        Log.v(TAG, "Getting string for key '" + key + "'...");
        findKey(key, new BackendlessCallback<BackendlessCollection<BackendlessSyncedObject>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessSyncedObject> objects) {
                if (objects.getData().size() > 0) {
                    String value = null;
                    if (objects.getData().get(0).getValue() instanceof String) {
                        value = (String) objects.getData().get(0).getValue();
                    }
                    callback.done(value, null);
                    Log.v(TAG, "... Got object for key '" + key + "'");
                } else {
                    Log.e(TAG, "... Failed to get object for key '" + key + "'");
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "... Failed to get object for key '" + key + "'");
                callback.done(null ,new BackendlessException(fault.getCode(), fault.getMessage()));
            }
        });
    }

    /*
    public void getObject(final String key, final GetObjectCallback callback) {
        // Guard
        if (callback == null){
            return;
        }

        ParseQuery<BackendlessSyncedObject> parseQuery = ParseQuery.getQuery(BackendlessSyncedObject.class);
        // Has two keys (package name + key)
        parseQuery.whereEqualTo(PACKAGE_NAME_KEY, keyValueObjectContainer.getPackageName());
        parseQuery.whereEqualTo(BackendlessSyncedObject.SAVED_OBJECT_KEY, key);
        Log.v(TAG, "Getting object for key '" + key + "'...");
        parseQuery.findInBackground(new FindCallback<BackendlessSyncedObject>() {
            @Override
            public void done(List<BackendlessSyncedObject> objects, BackendlessException e) {
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
        // Guard
        if (callback == null){
            return;
        }

        Log.v(TAG, "Getting all objects...");
        final BackendlessDataQuery query = new BackendlessDataQuery(SAVED_OBJECT_PACKAGE_NAME + " = '" + innerObject.getPackageName() + "'");
        Backendless.Persistence.of(BackendlessSyncedObject.class).find(query, new BackendlessCallback<BackendlessCollection<BackendlessSyncedObject>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessSyncedObject> objects) {
                HashMap<String, String> savedObjects = new HashMap<>(objects.getData().size());
                for (BackendlessSyncedObject syncedObject : objects.getData()) {
                    savedObjects.put(syncedObject.getKey(), syncedObject.getValue().toString());
                }
                Log.v(TAG, "... Got all (" + objects.getData().size() + ") objects");
                callback.done(savedObjects, null);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "... Failed to get all objects");
                callback.done(null ,new BackendlessException(fault.getCode(), fault.getMessage()));
            }
        });
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

        /*
         * I hope there could be a more efficient way, I might solve it with Backendless by configuring the object to have two unique keys from 'packageName" + 'key'.
         * But I still wanted to have the challenge.
         *
         * Will check duplications, delete if any, and save.
         */

        // (1) Find duplications
        findKey(innerObject.getKey(), new BackendlessCallback<BackendlessCollection<BackendlessSyncedObject>>() {
            @Override
            public void handleResponse(BackendlessCollection<BackendlessSyncedObject> backendlessSyncedObjectBackendlessCollection) {
                final List<BackendlessSyncedObject> objects = backendlessSyncedObjectBackendlessCollection.getData();

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
                                // Should be synchronously
                                Backendless.Persistence.of(BackendlessSyncedObject.class).remove(objects.get(index));
                            }
                        });
                    }
                    es.shutdown();
                    try {
                        finished = es.awaitTermination(1, TimeUnit.MINUTES);
                    } catch (InterruptedException waitingException) {
                        waitingException.printStackTrace();
                    }
                        /* all tasks have finished or the time has been reached */

                    if (finished) {
                        // (3.1) Save...
                        BackendlessSyncedObject newObject = new BackendlessSyncedObject(innerObject.getPackageName());
                        newObject.setKey(innerObject.getKey());
                        newObject.setValue(innerObject.getValue().toString());
                        Backendless.Persistence.of(BackendlessSyncedObject.class).save(newObject, new BackendlessCallback<BackendlessSyncedObject>() {
                            @Override
                            public void handleResponse(BackendlessSyncedObject backendlessSyncedObject) {
                                // (4) Notify for completion with callback
                                if (commitCallback != null) {
                                    commitCallback.done(null);
                                }
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                // (4) Notify for completion with callback
                                if (commitCallback != null) {
                                    commitCallback.done(new BackendlessException(fault.getCode(), fault.getMessage()));
                                }
                            }
                        });
                    } else {
                        if (commitCallback != null) {
                            commitCallback.done(new BackendlessException("-1", "Couldn't delete (duplications) and save"));
                        }
                    }
                } else { // Already unique, brand new
                    // (3) Save original object...
                    Backendless.Persistence.of(BackendlessSyncedObject.class).save(innerObject, new BackendlessCallback<BackendlessSyncedObject>() {
                        @Override
                        public void handleResponse(BackendlessSyncedObject backendlessSyncedObject) {
                            // (4) Notify for completion with callback
                            Log.v(TAG, "... Committed in background");
                            if (commitCallback != null) {
                                commitCallback.done(null);
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // (4) Notify for completion with callback
                            Log.e(TAG, "... Failed to commit in background");
                            if (commitCallback != null) {
                                commitCallback.done(new BackendlessException(fault.getCode(), fault.getMessage()));
                            }
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // (4) Notify for completion with callback
                Log.e(TAG, "... Failed to commit in background");
                if (commitCallback != null) {
                    commitCallback.done(new BackendlessException(fault.getCode(), fault.getMessage()));
                }
            }
        });
    }
}