package com.perrchick.someapplication.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.perrchick.someapplication.uiexercises.SensorsFragment;

/**
 * Created by perrchick on 12/4/15.
 */
public class SensorsFragmentBlue extends SensorsFragment {
    public static final String TAG = SensorsFragmentBlue.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Which is the same as: this.fragmentView (it doesn't really matter)
        View theCreatedView = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        theCreatedView.setBackgroundColor(Color.argb(255, 0, 0, 200));
        return theCreatedView;
    }
}