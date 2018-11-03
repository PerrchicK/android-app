package com.perrchick.someapplication.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.utilities.AppLogger;

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
        if (theCreatedView != null) {
            Context context = container.getContext();
            ResourcesCompat.getDrawable(getResources(), R.drawable.ic_language_swift, context.getTheme());
            theCreatedView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
        } else {
            AppLogger.error(TAG, "Failed to create fragment");
        }

        return theCreatedView;
    }
}