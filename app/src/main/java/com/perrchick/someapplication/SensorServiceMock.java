package com.perrchick.someapplication;

import android.content.Intent;
import android.os.IBinder;

import java.util.Random;

/**
 * Created by perrchick on 12/4/15.
 */
public class SensorServiceMock extends SensorService {

    private static final long TIME_TO_SLEEP = 1000;
    private static final String _TAG = SensorServiceMock.class.getSimpleName();

    private boolean shouldRun = false;
    private Random random = new Random();

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

                    evaluateAndNotify(evaluate());
                }
            }
        }).start();
        return super.onBind(intent);
    }

    @Override
    public SensorService getSelf() {
        return this;
    }

    @Override
    public String getTag() {
        return _TAG;
    }

    @Override
    protected float[] evaluate() {
        return new float[]{random.nextFloat(),random.nextFloat(),random.nextFloat()};
    }
}