package com.perrchick.someapplication.uiexercises;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.SensorService;

import java.util.Arrays;

/**
 * Created by perrchick on 12/4/15.
 *
 * This fragment tells you the sensor's state in a numerically form
 */
// http://stackoverflow.com/questions/17553374/android-app-fragments-vs-android-support-v4-app-using-viewpager
public class SensorsFragment extends Fragment {

    private SensorsFragmentListener _fragmentListener;
    private BroadcastReceiver broadcastReceiver;
    private TextView txtInfo;
    private TextSwitcher txtCounter;
    protected View fragmentView;
    static private int counter = 0;
    private boolean shouldCount = true;

    public interface SensorsFragmentListener {
        void valuesUpdated(float[] someData);
    }

    /* Beginning of Fragment's Lifecycle */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) getContext();
            if (activity instanceof SensorsFragmentListener) {
                _fragmentListener = (SensorsFragmentListener) activity;
            } else {
                Log.e(getTag(), activity.toString() + " doesn't implement " + SensorsFragmentListener.class.getSimpleName() + "! Listener calls won't be available");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.fragmentView = inflater.inflate(R.layout.fragment_sensors, container, false);
        txtInfo = (TextView) fragmentView.findViewById(R.id.lblInfo);
        txtCounter = (TextSwitcher) fragmentView.findViewById(R.id.lblCounter);
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        txtCounter.setInAnimation(in);
        txtCounter.setOutAnimation(out);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        //intentFilter.addAction(SensorService.SENSOR_SERVICE_BROADCAST_ACTION);
        //bindService(new Intent(this, SensorServiceMock.class), mConnection, Context.BIND_AUTO_CREATE);
        //intentFilter.addAction(PHONE_BROADCAST_ACTION);

        // Q: Should I bind it to the main activity or to the app?
        // A: It doesn't matter as long as you remember to shut the service down / destroy the Application
        // (for more info: http://stackoverflow.com/questions/3154899/binding-a-service-to-an-android-app-activity-vs-binding-it-to-an-android-app-app)
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SensorService.SENSOR_SERVICE_BROADCAST_ACTION)) {
                    senseDetected(intent.getFloatArrayExtra(SensorService.SENSOR_SERVICE_VALUES_KEY));
                }
            }
        };

        getContext().registerReceiver(broadcastReceiver, new IntentFilter(SensorService.SENSOR_SERVICE_BROADCAST_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();

        txtCounter.setCurrentText(String.valueOf(counter));

        shouldCount = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldCount) {
                    // Count seconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (getView() != null) {
                        getView().post(new Runnable() {
                            @Override
                            public void run() {
                                txtCounter.setText(String.valueOf(++counter));
                            }
                        });
                    }
                }
            }
        }).start();
    }

    // As a fragment is no longer being used, it goes through a reverse series of callbacks

    @Override
    public void onPause() {
        shouldCount = false;

        txtCounter.setCurrentText("---");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    /* Ending of Fragment's Lifecycle */

    public void senseDetected(float[] sensorAngles) {
        if (this._fragmentListener != null) {
            this._fragmentListener.valuesUpdated(sensorAngles);
        }

        int[] values = new int[sensorAngles.length];
        for (int i =0; i < sensorAngles.length; i++) {
            values[i] = Math.round(sensorAngles[i]);
        }

        this.txtInfo.setText("Accelerometer sensors state: " + Arrays.toString(values));
    }
}