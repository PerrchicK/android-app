package com.perrchick.someapplication.utilities

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.perrchick.someapplication.SomeApplication
import com.perrchick.someapplication.service.BackgroundLocationWorker


fun LatLng.persist() {
    SomeApplication
            .shared()
            .getSharedPreferences(BackgroundLocationWorker.Constants.FileNames.LastKnownLocation, Context.MODE_PRIVATE)
            .edit()
            .putString(BackgroundLocationWorker.Constants.Keys.LastLocationCoordinates, toJson())
            .apply()
}

fun LatLng.toJson(): String {
    return Gson().toJson(mapOf("latitude" to this.latitude, "longitude" to longitude))
}

fun loadLatLng(): LatLng? {
    val locationCoordinatesJson = SomeApplication
            .shared()
            .getSharedPreferences(BackgroundLocationWorker.Constants.FileNames.LastKnownLocation, Context.MODE_PRIVATE)
            .getString(BackgroundLocationWorker.Constants.Keys.LastLocationCoordinates, "")

    return Gson().fromJson(locationCoordinatesJson, LatLng::class.java)
}