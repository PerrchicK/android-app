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

/**
 * Created by perrchick on 12/4/15.
 *
 * This fragment tells you the sensor's state in a numerically form
 */
public class SensorsFragment extends Fragment {

    private SensorsFragmentListener fragmentListener;
    private TextView txtInfo;
    protected View fragmentView;

    public interface SensorsFragmentListener {
        void valuesUpdated(float someData);
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
                fragmentListener = (SensorsFragmentListener) activity;
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
        intentFilter = new IntentFilter();
        intentFilter.addAction(SensorService.SENSOR_SERVICE_BROADCAST_ACTION);
        //intentFilter.addAction(PHONE_BROADCAST_ACTION);
        this.fragmentView.getContext().registerReceiver(broadcastReceiver, intentFilter);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SensorService.SENSOR_SERVICE_BROADCAST_ACTION)) {
                senseDetected(intent.getFloatExtra(SensorService.SENSOR_SERVICE_VALUES_KEY, 0));
            }
        }
    };

    public void senseDetected(float sensorAngle) {
        this.txtInfo.setText("" + sensorAngle);
    }
}