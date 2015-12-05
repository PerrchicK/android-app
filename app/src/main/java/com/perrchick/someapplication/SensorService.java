package com.perrchick.someapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by perrchick on 12/5/15.
 */
public class SensorService extends Service implements SensorEventListener {
    public static final String SENSOR_SERVICE_BROADCAST_ACTION = "SENSOR_SERVICE_BROADCAST_ACTION";
    public static final String SENSOR_SERVICE_VALUES_KEY = "SENSOR_SERVICE_VALUES_KEY";

    private static final String _TAG = SensorService.class.getSimpleName();
    private final IBinder sensorServiceBinder = new SensorServiceBinder();
    protected float values;

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);;
        Sensor gyroUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensorManager.registerListener(this, gyroUncalibrated, SensorManager.SENSOR_DELAY_NORMAL);

        return sensorServiceBinder;
    }

    protected float[] evaluate() {
        return new float[]{0.1f, 0.1f, 0.1f};
    }

    protected void evaluateAndNotify() {
        evaluateAndNotify(evaluate());
    }

    protected void evaluateAndNotify(float[] values) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SENSOR_SERVICE_BROADCAST_ACTION);
        broadcastIntent.putExtra(SENSOR_SERVICE_VALUES_KEY, values);
        Log.v(getTag(), "Notifying new values: " + Arrays.toString(broadcastIntent.getFloatArrayExtra(SENSOR_SERVICE_VALUES_KEY)));
        sendBroadcast(broadcastIntent);
    }

    public float getValues() {
        return values;
    }

    public String getTag() {
        return _TAG;
    }

    public SensorService getSelf() {
        return this;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        evaluateAndNotify(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class SensorServiceBinder extends Binder {
        SensorService getService() {
            return SensorService.this.getSelf();
        }
    }
}