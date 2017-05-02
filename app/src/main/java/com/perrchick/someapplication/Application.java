package com.perrchick.someapplication;

/**
 * Created by perrchick on 12/16/15.
 */
public class Application extends android.app.Application {
    private static Application _applicationInstance;

    public static Application getApplicationInstance() {
        return _applicationInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _applicationInstance = this;
    }
}