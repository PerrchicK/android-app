package com.perrchick.someapplication.utilities;

import android.util.Log;

import androidx.annotation.Nullable;

import com.perrchick.someapplication.SomeApplication;

/**
 * Created by perrchick on 06/11/2017.
 * This class is responsible to output the logs only if the current version is running on debug mode (in contrary of the release mode).
 * If the project built well, there's no reason of "import android.util.Log;" occurrences.
 */
public class AppLogger {
    public static void log(Object reporter, String logMessage) {
        if (reporter == null) return;

        log(reporter.getClass().getSimpleName(), logMessage, false);
    }

    public static void log(String tag, String logMessage) {
        log(tag, logMessage, false);
    }

    public static void error(String tag, String errorMessage) {
        error(tag, errorMessage, false);
    }

    public static void error(Object reporter, String logMessage) {
        if (reporter == null) return;

        error(reporter.getClass().getSimpleName(), logMessage);
    }

    public static void error(Object reporter, Throwable e) {
        if (reporter == null) return;

        error(reporter.getClass().getSimpleName(), null, e);
    }

    public static void error(String tag, Throwable throwable) {
        error(tag, null, throwable, false);
    }

    public static void error(String tag, String errorMessage, Throwable throwable) {
        error(tag, errorMessage, throwable, false);
    }

    public static void error(String tag, @Nullable String errorMessage, Throwable throwable, boolean shouldUploadMessage) {
        if (throwable == null) return;

        if (errorMessage == null) {
            errorMessage = throwable.getMessage();
        } else {
            errorMessage += "throwable: " + throwable.getMessage();
        }

        error(tag, errorMessage, shouldUploadMessage);
    }

    public static void error(String tag, String errorMessage, boolean shouldUploadMessage) {
        if (errorMessage == null) return;

        if (!SomeApplication.isReleaseVersion) {
            Log.e(tag, errorMessage);
        }

        if (shouldUploadMessage) {
            //uploadErrorReport(errorMessage);
        }
    }

    public static void log(String tag, String logMessage, boolean shouldUploadMessage) {
        if (logMessage == null) return;

        if (!SomeApplication.isReleaseVersion) {
            Log.d(tag, logMessage);
        }

        if (shouldUploadMessage) {
            //uploadLogReport(logMessage);
        }
    }

}
