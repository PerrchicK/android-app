package com.perrchick.onlinesharedpreferences.parse;

import java.util.HashMap;

/**
 * Created by perrchick on 12/18/15.
 */
public class ParseSyncedObject {

    public static final String SAVED_OBJECT_KEY = "key";
    public static final String SAVED_OBJECT_VALUE = "value";
    public static final String SAVED_OBJECT_PACKAGE_NAME = "packageName";
    private static final String TAG = ParseSyncedObject.class.getSimpleName();
    private final HashMap<String, String> keysAndValues;

    public ParseSyncedObject() {
        keysAndValues = new HashMap<>();
    }

    public ParseSyncedObject(String packageName, String key, Object value) {
        this();
        setPackageName(packageName);
    }

    public void setPackageName(String packageName) {
        put(SAVED_OBJECT_PACKAGE_NAME, packageName);
    }

    public String getPackageName() {
        return getString(SAVED_OBJECT_PACKAGE_NAME);
    }


    public void setKey(String key) {
        put(SAVED_OBJECT_KEY, key);
    }

    public String getKey() {
        return getString(SAVED_OBJECT_KEY);
    }

    public void setValue(String value) {
        put(SAVED_OBJECT_VALUE, value);
    }

    public Object getValue() {

        return getString(SAVED_OBJECT_VALUE);

        /*
        Object value = null;

        try {
            value = getJSONObject(SAVED_OBJECT_VALUE).get("parsed");
        } catch (JSONException e) {
            Log.e(TAG, "Parsing object from key '" + getKey() + "' failed!");
            e.printStackTrace();
        }

        return value;
        */
    }

    private String getString(String key) {
        return this.keysAndValues.get(key);
    }

    @Override
    public String toString() {
        return "<" + getKey() + ", " + getValue() + ">";
    }

    public void put(String key, String value) {
        keysAndValues.put(key, value);
    }
}