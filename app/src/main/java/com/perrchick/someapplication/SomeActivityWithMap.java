package com.perrchick.someapplication;

import android.app.FragmentTransaction;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class SomeActivityWithMap extends AppCompatActivity {
    final String apiKey = "AIzaSyB700S2ZCSx_iq_nvSKkVSHhDylpk9HuFg";
    final String projectId = "regal-scholar-117015";
    final String projectNumber = "358469675076";
    private GoogleMap googleMap;
    private TextView lblZoom;
    private EditText txtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_some_activity_with_map);
        PerrFuncs.hideActionBarOfActivity(this);

        if (isGoogleMapsInstalled()) {
            // Add the Google Maps fragment dynamically
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            MapFragment mapFragment = MapFragment.newInstance();
            transaction.add(R.id.mapsPlaceHolder, mapFragment);
            transaction.commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    setGoogleMap(googleMap);
                }
            });
        } else {
            // Notify the user he should install GoogleMaps (after installing Google Play Services)
            FrameLayout mapsPlaceHolder = (FrameLayout) findViewById(R.id.mapsPlaceHolder);
            TextView errorMessageTextView = new TextView(getApplicationContext());
            errorMessageTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            errorMessageTextView.setText("No GoogleMaps installed on this device");
            errorMessageTextView.setTextColor(Color.RED);
            mapsPlaceHolder.addView(errorMessageTextView);
        }

        findViewById(R.id.btnGotoAfeka).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCameraToAfeka();
            }
        });

        txtAddress = (EditText) findViewById(R.id.txtAddress);
        lblZoom = (TextView) findViewById(R.id.lblZoomLevel);

        SeekBar slider = (SeekBar) findViewById(R.id.seekBar);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //txtAddress.setText(progress + "");
                if (progress > 0) {
                    if (getGoogleMap() != null) {
                        GoogleMap map = getGoogleMap();

                        LatLng current = map.getCameraPosition().target;
                        float zoom = 18f * (float)progress / 100f;
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.latitude, current.longitude), zoom));
                        lblZoom.setText(zoom  + "");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return info != null;
        }
        catch(PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Sets and configures the map
     * @param googleMap
     */
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;

        boolean isAllowedToUseLocation = true; //PerrFuncs.hasPermissionForLocationServices(getApplicationContext())
        if (isAllowedToUseLocation) {
            try {
                // Allow to (try to) set
                googleMap.setMyLocationEnabled(true);
                takeCameraToAfeka();
            } catch (SecurityException exception) {
                PerrFuncs.toast("Error getting location");
            }
        } else {
            PerrFuncs.toast("Location is blocked in this app");
        }
    }

    private void takeCameraToAfeka() {
        takeMapToStreet("Bnei Efraim 218, Tel Aviv", this.googleMap);
    }

    private void takeMapToStreet(String address, GoogleMap googleMap) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.1165, 34.8176), 18f));
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }
}