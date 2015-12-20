package com.perrchick.someapplication.data.parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by perrchick on 12/18/15.
 */
@ParseClassName("SavedObject")
public class ParseSavedObject extends ParseObject{

    public static final String SAVED_OBJECT_KEY = "key";
    public static final String SAVED_OBJECT_VALUE = "value";
    public static final String SAVED_OBJECT_PACKAGE_NAME = "packageName";

    // Required
    public ParseSavedObject() {
        super();
    }

    public ParseSavedObject(String packageName, String key, String value) {
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

    public void setValue(String value) {
        put(SAVED_OBJECT_VALUE, value);
    }

    public String getValue() {
        return getString(SAVED_OBJECT_VALUE);
    }

    @Override
    public String toString() {
        return "<" + getKey() + ", " + getValue() + ">";
    }
}