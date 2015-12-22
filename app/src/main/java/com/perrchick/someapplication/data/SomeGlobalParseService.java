package com.perrchick.someapplication.data;

import android.content.Context;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.perrchick.someapplication.data.parse.ParseSavedObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by perrchick on 12/15/15.
 */
public class SomeGlobalParseService {

    public interface GetAllObjectsCallback {
        void done(HashMap<String, String> objects, ParseException e);
    }
    public interface GetObjectCallback {
        void done(String value, ParseException e);
    }
    public interface CommitCallback {
        void done(ParseException e);
    }
    public interface RemoveCallback {
        void done(ParseException e);
    }

    public static ParseSharedPreferences getParseSharedPreferences(Context context) {
        return new ParseSharedPreferences(context);
    }

    public static class ParseSharedPreferences {
        private final ParseObjectWrapper editedObject;
        public static final String PACKAGE_NAME_KEY = "packageName";

        public ParseSharedPreferences(Context context) {
            this.editedObject = new ParseObjectWrapper(context.getPackageName());
        }

        public ParseSharedPreferences putObject(String key, String value) {
            editedObject.putObject(key, value);

            return this;
        }

        public void remove(String key, final RemoveCallback removeCallback) {
            editedObject.remove(key, new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (removeCallback != null) {
                        removeCallback.done(e);
                    }
                }
            });
        }

        public void getObject(String key, final GetObjectCallback callback) {
            ParseQuery<ParseSavedObject> parseQuery = ParseQuery.getQuery(ParseSavedObject.class);
            // Has two keys (package name + key)
            parseQuery.whereEqualTo(PACKAGE_NAME_KEY, editedObject.getPackageName());
            parseQuery.whereEqualTo(ParseSavedObject.SAVED_OBJECT_KEY, key);
            parseQuery.findInBackground(new FindCallback<ParseSavedObject>() {
                @Override
                public void done(List<ParseSavedObject> objects, ParseException e) {
                    String value = null;
                    if (objects.size() > 0) {
                        value = objects.get(0).getValue();
                    }
                    if (callback != null) {
                        // Should be unique
                        callback.done(value, e);
                    }
                }
            });
        }

        public void getAllObjects(final GetAllObjectsCallback callback) {
            final ParseQuery<ParseSavedObject> parseQuery = ParseQuery.getQuery(ParseSavedObject.class);
            parseQuery.whereEqualTo(PACKAGE_NAME_KEY, editedObject.getPackageName());
            parseQuery.findInBackground(new FindCallback<ParseSavedObject>() {
                @Override
                public void done(List<ParseSavedObject> objects, ParseException e) {
                    HashMap<String, String> savedObjects = new HashMap<String, String>(objects.size());
                    for (ParseSavedObject savedObject : objects) {
                        savedObjects.put(savedObject.getKey(), savedObject.getValue());
                    }
                    callback.done(savedObjects, e);
                }
            });
        }

        public void commitInBackground() {
            editedObject.saveInBackground(null);
        }

        public void commitInBackground(final CommitCallback saveCallback) {
            editedObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (saveCallback != null) {
                        saveCallback.done(e);
                    }
                }
            });
        }

    }

    // Adapter pattern (Object Adapter)
    private static class ParseObjectWrapper {
        private final ParseSavedObject innerObject;

        protected ParseObjectWrapper(String packageName) {
            innerObject = new ParseSavedObject();
            innerObject.put(ParseSharedPreferences.PACKAGE_NAME_KEY, packageName);
        }

        protected void putObject(String key, Object value) {
            innerObject.put(ParseSavedObject.SAVED_OBJECT_KEY, key);
            innerObject.put(ParseSavedObject.SAVED_OBJECT_VALUE, value);
        }

        protected void remove(String key, final DeleteCallback deleteCallback) {
            ParseQuery<ParseSavedObject> parseQuery = ParseQuery.getQuery(ParseSavedObject.class);
            parseQuery.whereEqualTo(ParseSharedPreferences.PACKAGE_NAME_KEY, innerObject.getPackageName());
            parseQuery.whereEqualTo(ParseSavedObject.SAVED_OBJECT_KEY, key);
            // Find the object to delete
            parseQuery.findInBackground(new FindCallback<ParseSavedObject>() {
                @Override
                public void done(final List<ParseSavedObject> objects, ParseException e) {
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
        protected void saveInBackground(final SaveCallback saveCallback) {
            ParseQuery<ParseSavedObject> parseQuery = ParseQuery.getQuery(ParseSavedObject.class);
            parseQuery.whereEqualTo(ParseSharedPreferences.PACKAGE_NAME_KEY, innerObject.getPackageName());
            parseQuery.whereEqualTo(ParseSavedObject.SAVED_OBJECT_KEY, this.innerObject.getKey());
            // (1) Find duplications
            parseQuery.findInBackground(new FindCallback<ParseSavedObject>() {
                @Override
                public void done(final List<ParseSavedObject> objects, ParseException e) {
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
                        /* all tasks have finished or the time has been reached */

                        if (finished) {
                            // (3.1) Save...
                            new ParseSavedObject(innerObject.getPackageName(), innerObject.getKey(), innerObject.getValue()).saveInBackground(new SaveCallback() {
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
        }

        public String getPackageName() {
            return innerObject.getPackageName();
        }
    }
}