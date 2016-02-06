package com.perrchick.onlinesharedpreferences.backendless;

import java.util.HashMap;

/**
 * Created by perrchick on 2/5/16.
 */
public class BackendlessSyncedObject {
    private String packageName;
    private String key;
    private String value;

    public BackendlessSyncedObject() {
    }

    public BackendlessSyncedObject(String packageName) {
        this();
        setPackageName(packageName);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }


    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {

        return this.value;

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
