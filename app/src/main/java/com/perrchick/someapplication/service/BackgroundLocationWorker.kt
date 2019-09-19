package com.perrchick.someapplication.service;

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.concurrent.futures.ResolvableFuture
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.perrchick.someapplication.NotificationsActivity
import com.perrchick.someapplication.SomeApplication
import com.perrchick.someapplication.service.location.LocationHelper
import com.perrchick.someapplication.utilities.AppLogger
import com.perrchick.someapplication.utilities.loadLatLng
import com.perrchick.someapplication.utilities.persist

class BackgroundLocationWorker(appContext: Context, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {
    private var taskFuture: ResolvableFuture<Result>? = null
    val backgroundHandler: Handler by lazy {
        val handlerThread = HandlerThread("com.perrchick.somebgworker.location")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    class Constants {
        class Keys {
            companion object {
                const val LastLocationCoordinates = "LastLocationCoordinates"
                const val isEnabled = "IsEnabled"
            }
        }
        class FileNames {
            companion object {
                const val LastKnownLocation = "LastKnownLocation"
            }
        }
    }

    companion object {
        fun toggle() {
            SomeApplication
                    .shared()
                    .getSharedPreferences(TAG, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(Constants.Keys.isEnabled, !isEnabled())
                    .apply()
        }

        fun isEnabled(context: Context? = null): Boolean {
            val appContext = context ?: SomeApplication.shared()
            return appContext
                    .getSharedPreferences(TAG, Context.MODE_PRIVATE)
                    .getBoolean(Constants.Keys.isEnabled, false)
        }

        @JvmField
        var INTERVAL_IN_MINUTS: Long = 15
        @JvmField
        var NAME: String = BackgroundLocationWorker.TAG + "_name"
        @JvmField
        var TAG: String = BackgroundLocationWorker::class.simpleName.toString()
    }

    private val bgTask = object : Runnable {
        override fun run() {
            if (!isEnabled(applicationContext)) return

            AppLogger.log(TAG, "background worker started... last location was here: ${loadLatLng()}")

            LocationHelper.instance.fetchLocation(applicationContext) { coordinate ->
                coordinate?.let {
                    it.persist()
                    val notificationDelay = NotificationsActivity.showNotification("fetched location from background", it.toString())
                    AppLogger.log(TAG, "will notify in $notificationDelay milliseconds")
                    taskFuture?.set(Result.success())
                } ?: run {
                    AppLogger.error(TAG, "Failed to fetch location by service!")
                    taskFuture?.set(Result.retry())
                }
            }
        }
    }

    override fun startWork(): ListenableFuture<Result> {
        val future = ResolvableFuture.create<Result>()
        taskFuture = future
        backgroundHandler.post(bgTask)
        AppLogger.log(TAG, "Called `startWork`...")

        return future
    }
}
