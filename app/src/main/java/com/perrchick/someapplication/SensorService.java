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
import java.util.List;
import java.util.Random;

/**
 * Created by perrchick on 12/5/15.
 */
public class SensorService extends Service implements SensorEventListener {
    public static final String SENSOR_SERVICE_BROADCAST_ACTION = "SENSOR_SERVICE_BROADCAST_ACTION";
    public static final String SENSOR_SERVICE_VALUES_KEY = "SENSOR_SERVICE_VALUES_KEY";

    private static final String _TAG = SensorService.class.getSimpleName();
    protected final IBinder sensorServiceBinder = new SensorServiceBinder();
    protected float values;
    private SensorManager sensorManager;

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList= sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.v(getTag(), "Available sensors: " + sensorList);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // null in Genymotion free edition of course
        if (sensor == null && sensorList.size() > 0) {
            sensor = sensorList.get(0); // for Genymotion sensors (Genymotion Accelerometer in my case)
        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // A.D: "You must always implement this method, but if you don't want to allow binding, then you should return null."
        return sensorServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

        return super.onUnbind(intent);
    }

    protected float[] evaluate() {
        return new float[]{0.1f, 0.1f, 0.1f};
    }

    protected void notifyEvaluation(float[] values) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SENSOR_SERVICE_BROADCAST_ACTION);
        broadcastIntent.putExtra(SENSOR_SERVICE_VALUES_KEY, values);
        //Log.v(getTag(), "Notifying new values: " + Arrays.toString(broadcastIntent.getFloatArrayExtra(SENSOR_SERVICE_VALUES_KEY)));
        sendBroadcast(broadcastIntent);
    }

    public float getValues() {
        return values;
    }

    /**
     * Used to specify the tag of the actual running class (sometimes I chose to work with a mock)
     * @return A String of the acting class's tag
     */
    public String getTag() {
        return _TAG;
    }

    public SensorService getSelf() {
        return this;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = new float[event.values.length];
        for (int i =0; i < event.values.length; i++) {
            values[i] = event.values[i];// * 1000000.0f;
        }

        notifyEvaluation(values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class SensorServiceBinder extends Binder {
        SensorService getService() {
            return SensorService.this.getSelf();
        }

        void notifyService(String msg) {
            // A.D: "you must provide an interface that clients use to communicate with the service, by returning an IBinder."
            Log.v(getTag(), SensorService.class.getSimpleName() +
                    " has got a message from its binding activity. Message: " + msg);
        }
    }
}