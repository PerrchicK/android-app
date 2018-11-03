package com.perrchick.someapplication;

import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class SomeMapActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = SomeMapActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private final String formatForGeocodeFromAddress = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private final String formatForReverseGeocoding = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s";
    private final String formatForAutocompletePlacesSearch = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&types=address&language=iw&key=%s";
    private final String apiKey = "AIzaSyDC5LC2DDP6Vi11nVw53q7uAyxyVhOfbxw"; // Different from the Maps API key

    private GoogleMap googleMap;
    private TextView lblZoom;
    private EditText txtAddress;
    private SeekBar zoomSlider;
    private Spinner actionsDropdownList;
    private int markersCounter = 0;

    private Location currentLocation = null;
    private LocationManager locationManager;
    private boolean didAlreadyRequestLocationPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_some_map);
        PerrFuncs.hideActionBarOfActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        didAlreadyRequestLocationPermission = false;

        if (isGoogleMapsInstalled()) {
            // Add the Google Maps fragment dynamically
            MapFragment mapFragment = MapFragment.newInstance();

            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.mapsPlaceHolder, mapFragment);
            transaction.commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    setGoogleMap(googleMap);
                }
            });
        } else {
            // Update UI if needed
            findViewById(R.id.imageTarget).setVisibility(View.INVISIBLE);

            // Notify the user he should install GoogleMaps (after installing Google Play Services)
            FrameLayout mapsPlaceHolder = (FrameLayout) findViewById(R.id.mapsPlaceHolder);
            TextView errorMessageTextView = new TextView(getApplicationContext());
            errorMessageTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            errorMessageTextView.setText(R.string.missing_google_maps_error_message);
            errorMessageTextView.setTextColor(Color.RED);
            mapsPlaceHolder.addView(errorMessageTextView);
        }

        this.actionsDropdownList = (Spinner) findViewById(R.id.spinner_maps_actions);
        final String[] actionValues = getResources().getStringArray(R.array.location_spinner_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actionValues);
        actionsDropdownList.setAdapter(adapter);

        txtAddress = (EditText) findViewById(R.id.txtAddress);
        lblZoom = (TextView) findViewById(R.id.lblZoomLevel);

        zoomSlider = (SeekBar) findViewById(R.id.seekBar);
        zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //txtAddress.setText(progress + "");
                if (progress > 0) {
                    if (googleMap != null) {
                        LatLng current = googleMap.getCameraPosition().target;
                        float zoom = 18f * (float) progress / 100f;
                        Log.d(TAG, "progress = " + progress + ", zoom = " + zoom);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.latitude, current.longitude), zoom));
                        lblZoom.setText(zoom + "");
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

        findViewById(R.id.btnGoMapAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMapActionPressed();
            }
        });
        findViewById(R.id.btnAdreessSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddressSearchPressed();
            }
        });

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
    }

    private void btnMapActionPressed() {
        // Guard
        if (googleMap == null)
            return;

        LatLng cameraTarget = googleMap.getCameraPosition().target;
        String geoLocationString = cameraTarget.latitude + ", " + cameraTarget.longitude;
        switch (actionsDropdownList.getSelectedItemPosition()) {
            case 0: // take camera to Afeka
                takeCameraToAfeka(new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        // Zoom in
                        zoomSlider.setProgress(95);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            case 1: { // Copy current target to clipboard
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("geocode", geoLocationString);
                clipboardManager.setPrimaryClip(clipData); //text/plain
                PerrFuncs.toast("copied...");
            }
            break;
            case 2: // Put marker
                googleMap.addMarker(new MarkerOptions()
                        .title("marker (" + (++markersCounter) + ")")
                        .draggable(true)
                        .position(cameraTarget)) // The 'MarkerOptions' constructor works with Fluent Pattern
                        .setSnippet(geoLocationString); // The 'addMarker' almost works with Fluent Pattern, it returns the instance of the added marker
                break;
            case 3: // Go to current location
                takeCameraToCurrentLocation();
                break;
            default:
                break;
        }
    }

    private void takeCameraToCurrentLocation() {
        if (currentLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        } else {
            if (!isPermissionForLocationServicesGranted()) {
                requestLocationPermissionsIfNeeded(true);
            }
        }
    }

    private void btnAddressSearchPressed() {
        String searchAddressUrl = String.format(formatForGeocodeFromAddress, this.txtAddress.getText().toString(), apiKey);
        PerrFuncs.makeGetRequest(searchAddressUrl, new PerrFuncs.CallbacksHandler<Response>() {
            @Override
            public void onCallback(Response callbackObject) {
                try {
                    String jsonData = callbackObject.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);

                    String responseStatus = jsonObject.getString("status");
                    PerrFuncs.toast("Google servers response status: " + responseStatus); // The status we get in the response from Google

                    if (responseStatus.equals("OK")) {
                        // All good
                        Log.v(TAG, jsonObject.toString());
                        JSONObject locationJson = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                        final double lat = Double.parseDouble(locationJson.get("lat").toString());
                        final double lng = Double.parseDouble(locationJson.get("lng").toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18f));
                            }
                        });
                    } else {
                        // Try to show the error message, if any, if not it will jump to the catch clause
                        PerrFuncs.toast(jsonObject.getString("error_message"));
                    }
                    // The image's data is here
                } catch (IOException e) {
                    e.printStackTrace(); // Will print the stack trace in the "locgcat"
                } catch (JSONException e) {
                    Log.e(TAG, "Error in parsing the JSON");
                    PerrFuncs.toast("Failed to parse the JSON");
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Sets and configures the map
     * @param googleMap    The map
     */
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Unmark to see the changes...

        boolean isAllowedToUseLocation = isPermissionForLocationServicesGranted();
        if (isAllowedToUseLocation) {
            try {
                // Allow to (try to) set
                googleMap.setMyLocationEnabled(true);
                takeCameraToAfeka(null);
            } catch (SecurityException exception) {
                PerrFuncs.toast("Error getting location");
            }
        } else {
            PerrFuncs.toast("Location is blocked in this app");
        }
    }

    private boolean isPermissionForLocationServicesGranted() {
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (!(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED));

    }

    @SuppressWarnings("MissingPermission")
    private void getCurrentLocation() {
        if (requestLocationPermissionsIfNeeded(false)) {
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            float metersToUpdate = 1;
            long intervalMilliseconds = 1000;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalMilliseconds, metersToUpdate, this);
        }

        Log.d(TAG, "getCurrentLocation: " + currentLocation);

        String searchAddressUrl = String.format(Locale.US, formatForReverseGeocoding, 32.1226496f, 34.8240027f, apiKey);
        PerrFuncs.makeGetRequest(searchAddressUrl, new PerrFuncs.CallbacksHandler<Response>() {
            @Override
            public void onCallback(Response callbackObject) {
                try {
                    String jsonData = callbackObject.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);

                    // Extract the status we get in the response from Google
                    String responseStatus = jsonObject.getString("status");

                    if (responseStatus.equals("OK")) {
                        String streetName = jsonObject.getJSONArray("results")
                                .getJSONObject(0)
                                .getJSONArray("address_components")
                                .getJSONObject(1)
                                .getString("long_name");
                        // All good
                        String currentLocationMessage = "The map is currently on street: '" + streetName + "'";
                        PerrFuncs.toast(currentLocationMessage);
                        Log.v(TAG, currentLocationMessage);

                    }
                } catch (JSONException jsonException) {
                    // Failed to parse
                    Log.e(TAG, "getCurrentLocation - Failed to parse: " + jsonException);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean requestLocationPermissionsIfNeeded(boolean byUserAction) {
        boolean isAccessGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
            String coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
            isAccessGranted = getApplicationContext().checkSelfPermission(fineLocationPermission) == PackageManager.PERMISSION_GRANTED &&
                    getApplicationContext().checkSelfPermission(coarseLocationPermission) == PackageManager.PERMISSION_GRANTED;
            if (!isAccessGranted) { // The user blocked the location services of THIS app / not yet approved

                if (!didAlreadyRequestLocationPermission || byUserAction) {
                    didAlreadyRequestLocationPermission = true;
                    String[] permissionsToAsk = new String[]{fineLocationPermission, coarseLocationPermission};
                    // IllegalArgumentException: Can only use lower 16 bits for requestCode
                    ActivityCompat.requestPermissions(this, permissionsToAsk, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        } else {
            // Because the user's permissions started only from Android M and on...
            isAccessGranted = true;
        }

        return isAccessGranted;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCurrentLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (LOCATION_PERMISSION_REQUEST_CODE == requestCode) {
            Log.d(TAG, "onRequestPermissionsResult: user granted location permissions " + Arrays.toString(grantResults));
        }
    }

    /**
     * Searches the street of the main entrance of Afeka College finds the coordinates and animates the camera to this point on the globe.
     *
     * @param callback
     */
    private void takeCameraToAfeka(GoogleMap.CancelableCallback callback) {
        takeMapToStreet("Bnei Efraim 218, Tel Aviv", callback);
    }

    private void takeMapToStreet(String address, final GoogleMap.CancelableCallback callback) {
        if (googleMap == null)
            return;

        String searchAddressUrl = String.format(Locale.US, formatForGeocodeFromAddress, address, apiKey);
        PerrFuncs.makeGetRequest(searchAddressUrl, new PerrFuncs.CallbacksHandler<Response>() {
            @Override
            public void onCallback(Response callbackObject) {
                if (callbackObject == null) return;

                try {
                    String jsonData = callbackObject.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);

                    String responseStatus = jsonObject.getString("status");
                    PerrFuncs.toast(responseStatus); // The status we get in the response from Google

                    if (responseStatus.equals("OK")) {
                        // All good
                        Log.v(TAG, jsonObject.toString());
                        JSONObject locationJson = jsonObject.getJSONArray("results")
                                .getJSONObject(0) // 1st result in the results array
                                .getJSONObject("geometry")
                                .getJSONObject("location");
                        final double lat = locationJson.getDouble("lat");
                        final double lng = locationJson.getDouble("lng");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)), callback);
                                } else {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                                }

                            }
                        });
                    } else {
                        // Try to show the error message, if any, if not it will jump to the catch clause
                        PerrFuncs.toast(jsonObject.getString("error_message"));
                    }
                    // The image's data is here
                } catch (IOException e) {
                    e.printStackTrace(); // Will print the stack trace in the "locgcat"
                } catch (JSONException e) {
                    Log.e(TAG, "Error in parsing the JSON");
                    PerrFuncs.toast("Failed to parse the JSON");
                    e.printStackTrace();
                }
            }
        });
    }
}