package com.perrchick.someapplication.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.perrchick.someapplication.SomeApplication;
import com.perrchick.someapplication.utilities.AppLogger;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.perrchick.someapplication.utilities.RunnableWithExtra;

/**
 * Created by perrchick on 18/10/2018.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SomeJobService extends JobService {
    public static final int JOB_ID = 1001;
    public static final long JOB_PERIOD_MILLISECONDS = PerrFuncs.ONE_MINUTE_MILLISECONDS * 3;
    private static final String TAG = SomeJobService.class.getSimpleName();
    private RunnableWithExtra bgTask = new RunnableWithExtra() {
        @Override
        public void run() {
            AppLogger.log(TAG, "background job service is running...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                JobParameters params = getExtraData().getParcelable("params");
                jobFinished(params, true);
            }
        }
    };

    @Override
    public boolean onStartJob(final JobParameters params) {
        boolean isAsynchronousJob = true;
        Bundle bundle = new Bundle();
        bundle.putParcelable("params", params);
        bgTask.setExtraData(bundle);
        SomeApplication.runInBackgroundThread(bgTask);

        //noinspection ConstantConditions
        return isAsynchronousJob;
    }

    @Override
    public boolean onStopJob(JobParameters params) { // Informs that the scheduling requirements are no longer being met.
        //noinspection UnnecessaryLocalVariable
        boolean pleaseRetryThisJob = true;
        SomeApplication.cancelBackgroundThread(bgTask);

        //noinspection ConstantConditions
        return pleaseRetryThisJob;
    }
}
