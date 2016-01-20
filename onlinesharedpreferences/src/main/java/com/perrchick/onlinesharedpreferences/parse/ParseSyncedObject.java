package com.perrchick.onlinesharedpreferences.parse;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONException;

/**
 * Created by perrchick on 12/18/15.
 */
@ParseClassName("SavedObject")
public class ParseSyncedObject extends ParseObject {

    public static final String SAVED_OBJECT_KEY = "key";
    public static final String SAVED_OBJECT_VALUE = "value";
    public static final String SAVED_OBJECT_PACKAGE_NAME = "packageName";
    private static final String TAG = ParseSyncedObject.class.getSimpleName();

    // Required
    public ParseSyncedObject() {
        super();
    }

    public ParseSyncedObject(String packageName, String key, Object value) {
        super();
        setPackageName(packageName);
        setKey(key);
        setValue(value);
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

    public void setValue(Object value) {
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

    @Override
    public String toString() {
        return "<" + getKey() + ", " + getValue() + ">";
    }
}