package com.perrchick.someapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.perrchick.someapplication.data.SomePojo;

import java.util.List;

/**
 * Created by perrchick on 12/5/15.
 */
public class SensorService extends Service implements SensorEventListener {

    interface SensorServiceListener {
        void onSensorValuesChanged(SensorService sensorService, float[] values);
    }
    public static final String SENSOR_SERVICE_BROADCAST_ACTION = "SENSOR_SERVICE_BROADCAST_ACTION";
    public static final String SENSOR_SERVICE_VALUES_KEY = "SENSOR_SERVICE_VALUES_KEY";
    public static final String PARCEL_OBJECT_KEY = "some_parceled_object";

    private static final String TAG = SensorService.class.getSimpleName();
    protected SensorServiceBinder sensorServiceBinder = new SensorServiceBinder();// An IBinder implementation subclass
    protected float values;
    private SensorManager sensorManager;
    boolean isListening = false;
    HandlerThread sensorThread;
    private Handler sensorHandler;
    private SensorServiceListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorThread = new HandlerThread(SensorService.class.getSimpleName());
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        sensorServiceBinder.sensorService = this;

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

    protected void notifyEvaluation(float[] values) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SENSOR_SERVICE_BROADCAST_ACTION);

        SomePojo somePojo = new SomePojo();
        somePojo.setName("parcelable POJO");
        somePojo.setLatitude(32.1151989);
        somePojo.setLongitude(34.8196429);

        //broadcastIntent.putExtra(PARCEL_OBJECT_KEY, somePojo);
        broadcastIntent.putExtra(SENSOR_SERVICE_VALUES_KEY, values);
        //Log.v(getTag(), "Notifying new values: " + Arrays.toString(broadcastIntent.getFloatArrayExtra(SENSOR_SERVICE_VALUES_KEY)));
        sendBroadcast(broadcastIntent);

        if (listener != null) {
            listener.onSensorValuesChanged(this, values);
        }
    }

    public void setListener(SensorServiceListener listener) {
        this.listener = listener;
    }

    public float getValues() {
        return values;
    }

    /**
     * Used to specify the tag of the actual running class (sometimes I chose to work with a mock)
     * @return A String of the acting class's tag
     */
    public String getTag() {
        return TAG;
    }

    public SensorService getSelf() {
        return this;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        final float[] values = new float[event.values.length];
        for (int i = 0; i < event.values.length; i++) {
            values[i] = event.values[i];// * 1000000.0f;
        }

        notifyEvaluation(values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class SensorServiceBinder extends Binder {
        static final String START_LISTENING = "Start";
        private SensorService sensorService;

        SensorService getService() {
            return sensorService;
        }

        void notifyService(String msg) {
            // A.D: "you must provide an interface that clients use to communicate with the service, by returning an IBinder."
            Log.v(getTag(), SensorService.class.getSimpleName() + " has got a message from its binding activity. Message: " + msg);

            if (msg == SensorServiceBinder.START_LISTENING && !isListening) { // Why can we use this instead of equals?
                List<Sensor> sensorList= sensorManager.getSensorList(Sensor.TYPE_ALL);
                Log.v(getTag(), "Available sensors: " + sensorList);
                Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // Sensor.TYPE_GYROSCOPE will be null in Genymotion free edition
                if (sensor == null && sensorList.size() > 0) {
                    // Backup
                    sensor = sensorList.get(0); // for Genymotion sensors (Genymotion Accelerometer in my case)
                }

                if (sensor == null) return;

                sensorManager.registerListener(getService(), sensor, SensorManager.SENSOR_DELAY_UI, sensorHandler);
                isListening = true;
            }
        }
    }
}