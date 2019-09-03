package com.perrchick.someapplication.service.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.perrchick.someapplication.SomeApplication
import com.perrchick.someapplication.utilities.AppLogger

/**
 * Using FusedLocationProvider to provide current location: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
 */
class LocationHelper {

    companion object {
        val instance: LocationHelper = LocationHelper()
        private val TAG = LocationHelper::class.java.simpleName
    }

    private var currentLocation: Location? = null

    init {
        // init some stuff if needed here
    }

    @SuppressLint("MissingPermission") // Already covered in `isPermissionGranted()`
    fun fetchLocation(context: Context? = null, callback: ((coordinate: LatLng?) -> Unit)) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SomeApplication.shared())

        val _context: Context?
        if (context == null) {
            val topActivity: Activity? = SomeApplication.getTopActivity()
            _context = topActivity ?: SomeApplication.shared()
        } else {
            _context = context
        }

        if (_context == null) {
            callback(null)
        } else {
            //TODO, Follow this: https://medium.com/google-developer-experts/exploring-android-q-location-permissions-64d312b0e2e1
            if (isForegroundPermissionGranted(context)) {
                val task: Task<Location>? = fusedLocationProviderClient.lastLocation
                task?.addOnCompleteListener { t ->
                    t.result?.let { location ->
                        currentLocation = location
                        callback(LatLng(location.latitude, location.longitude))
                    } ?: run {
                        callback(null)
                    }
                }
            } else {
                AppLogger.error(TAG, "Failed to fetch location - the user blocked permissions")
                callback(null)
            }
        }
    }

    private fun isBackgroundPermissionGranted(context: Context? = null): Boolean {
        val applicationContext = context ?: SomeApplication.shared()

        // From Android API 29+ we will need to consider background location permission as well...
        // More info here: https://developer.android.com/preview/privacy/device-location
//        val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

        return ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isForegroundPermissionGranted(context: Context? = null): Boolean {
        val applicationContext = context ?: SomeApplication.shared()

        //val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

        return ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun isSensorEnabled(context: Context? = null): Boolean {
        val applicationContext = context ?: SomeApplication.shared()
        val locationManager: LocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return isGpsEnabled || isNetworkEnabled
    }

    // "reverse_geocode"
    fun reverseGeocode(coordinate: LatLng, callback: ((addressComponents: List<String>?) -> Unit)? = null) {
        val geocoder = Geocoder(SomeApplication.shared())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 10)
        } catch (error: Exception) {
            AppLogger.error(TAG, error)
        }

        val parsedAddresses = addresses?.map { address -> arrayListOf(address.getAddressLine(0), address.locality, address.countryName) }

        callback?.invoke(parsedAddresses?.first() ?: ArrayList<String>())
    }

    fun geocode(addressString: String, callback: ((coordinate: LatLng?) -> Unit)? = null) {
        val geocoder = Geocoder(SomeApplication.shared())
        val addresses = geocoder.getFromLocationName(addressString, 1)
        addresses.firstOrNull()?.let {
            val latlng = LatLng(it.latitude, it.longitude)
            callback?.invoke(latlng)
        } ?: kotlin.run {
            callback?.invoke(null)
        }
    }

}
