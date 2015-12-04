package com.perrchick.someapplication;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

/**
 * Created by perrchick on 12/4/15.
 */
public class SensorService extends Service {

    public static final String SENSOR_SERVICE_BROADCAST_ACTION = "SENSOR_SERVICE_BROADCAST_ACTION";
    public static final String SENSOR_SERVICE_VALUES_KEY = "SENSOR_SERVICE_VALUES_KEY";

    private static final long TIME_TO_SLEEP = 1000;
    private static final String TAG = SensorService.class.getSimpleName();
    private final IBinder sensorServiceBinder = new SensorServiceBinder();
    private boolean shouldRun = false;
    private Random random = new Random();
    private float values;

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        shouldRun = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldRun) {
                    try {
                        Thread.sleep(TIME_TO_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    evaluateAndNotify();
                }
            }
        }).start();
        return sensorServiceBinder;
    }

    private float evaluate() {
        return values = random.nextFloat();
    }

    private void evaluateAndNotify() {
        float newValue = evaluate();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SENSOR_SERVICE_BROADCAST_ACTION);
        broadcastIntent.putExtra(SENSOR_SERVICE_VALUES_KEY, newValue);
        sendBroadcast(broadcastIntent);
    }

    public float getValues() {
        return values;
    }

    class SensorServiceBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }
}