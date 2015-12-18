package com.perrchick.someapplication;

import com.parse.Parse;
import com.perrchick.someapplication.data.parse.ParseSavedObject;

/**
 * Created by perrchick on 12/16/15.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // [Optional] Power your app with Local Datastore. For more info, go to
        // https://parse.com/docs/android/guide#local-datastore
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "6uvLKEmnnQtdRpdThttAnDneX1RxyGUjyHwpI462", "TaVVVo6EP2dufExRhznnVSHYl5YHwM9gPhvxwP00");
        ParseSavedObject.registerSubclass(ParseSavedObject.class);
    }
}