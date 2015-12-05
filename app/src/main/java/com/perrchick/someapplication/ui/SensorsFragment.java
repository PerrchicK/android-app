package com.perrchick.someapplication.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.SensorService;
import com.perrchick.someapplication.SensorServiceMock;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by perrchick on 12/4/15.
 *
 * This fragment tells you the sensor's state in a numerically form
 */
public class SensorsFragment extends Fragment {

    private SensorsFragmentListener _fragmentListener;
    private BroadcastReceiver broadcastReceiver;
    private TextView txtInfo;
    protected View fragmentView;

    public interface SensorsFragmentListener {
        void valuesUpdated(float[] someData);
    }

    IntentFilter intentFilter;

    /* Beginning of Fragment's Lifecycle */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity){
            activity = (Activity) getContext();

            try {
                _fragmentListener = (SensorsFragmentListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement " + SensorsFragmentListener.class.getSimpleName());
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
        txtInfo = (TextView) fragmentView.findViewById(R.id.txtInfo);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        intentFilter = new IntentFilter();
        //intentFilter.addAction(SensorService.SENSOR_SERVICE_BROADCAST_ACTION);
        intentFilter.addAction(SensorService.SENSOR_SERVICE_BROADCAST_ACTION);
        //bindService(new Intent(this, SensorServiceMock.class), mConnection, Context.BIND_AUTO_CREATE);
        //intentFilter.addAction(PHONE_BROADCAST_ACTION);

        // Q: Should I bind it to the main activity or to the app?
        // A: It doesn't matter as long as you remenber to shut the service down / destroy the Application
        // (for more info: http://stackoverflow.com/questions/3154899/binding-a-service-to-an-android-app-activity-vs-binding-it-to-an-android-app-app)
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SensorService.SENSOR_SERVICE_BROADCAST_ACTION)) {
                    senseDetected(intent.getFloatArrayExtra(SensorService.SENSOR_SERVICE_VALUES_KEY));
                }
            }
        };

        this.fragmentView.getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // As a fragment is no longer being used, it goes through a reverse series of callbacks

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        this.fragmentView.getContext().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        this.txtInfo.setText("" + Arrays.toString(sensorAngles));
    }
}