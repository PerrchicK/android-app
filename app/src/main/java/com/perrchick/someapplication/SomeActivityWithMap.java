package com.perrchick.someapplication;

import android.app.FragmentTransaction;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class SomeActivityWithMap extends AppCompatActivity {
    final String apiKey = "AIzaSyB700S2ZCSx_iq_nvSKkVSHhDylpk9HuFg";
    final String projectId = "regal-scholar-117015";
    final String projectNumber = "358469675076";
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_some_activity_with_map);

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
        }
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

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;

        boolean isAllowedToUseLocation = true; //PerrFuncs.hasPermissionForLocationServices(getApplicationContext())
        if (isAllowedToUseLocation) {
            try {
                googleMap.setMyLocationEnabled(true);

            } catch (SecurityException exception) {
                PerrFuncs.toast("Error getting location");
            }
        } else {
            PerrFuncs.toast("Location is blocked in this app");
        }
    }
}
