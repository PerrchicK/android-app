package com.perrchick.someapplication.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.perrchick.someapplication.R;

/**
 * Created by perrchick on 12/4/15.
 */
public class SensorsFragmentRed extends SensorsFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Which is the same as: this.fragmentView (it doesn't really matter)
        View theCreatedView = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        theCreatedView.setBackgroundColor(Color.argb(255, 150, 0, 0));
        return theCreatedView;
    }

}
