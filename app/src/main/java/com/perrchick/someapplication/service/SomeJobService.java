package com.perrchick.someapplication.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.perrchick.someapplication.SomeApplication;
import com.perrchick.someapplication.utilities.AppLogger;

/**
 * Created by perrchick on 18/10/2018.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SomeJobService extends JobService {
    public static final int JOB_ID = 1001;
    private static final String TAG = SomeJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {
        // Perry: "You can do you your background stuff here if you like (lately Android OS is embracing the iOS attitude about "real" background tasks)"
        boolean isAsynchronousJob = true;
        SomeApplication.runInBackgroundThread(new Runnable() {
            @Override
            public void run() {
                AppLogger.log(TAG, "background job service is running...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    jobFinished(params, true);
                }
            }
        });

        //noinspection ConstantConditions
        return isAsynchronousJob;
    }

    @Override
    public boolean onStopJob(JobParameters params) { // Informs that the scheduling requirements are no longer being met.
        return false;
    }
}
