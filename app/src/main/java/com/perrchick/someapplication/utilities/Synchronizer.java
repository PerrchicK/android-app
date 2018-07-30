package com.perrchick.someapplication.utilities;

import com.perrchick.someapplication.SomeApplication;

import java.util.ArrayList;

/**
 * Created by roee on 18/01/2017 and supervised by Perry.
 *
 * Improved by Perry, using:
 * https://developer.android.com/reference/java/util/concurrent/Future.html
 */
public class Synchronizer<T> {
    private boolean hasBeenCanceled;
    private int holdersCount = 0;
    private final SynchronizerCallback<T> futureTask;
    private ArrayList<T> allHoldersResults;

    public Synchronizer(SynchronizerCallback<T> lastAction) {
        futureTask = lastAction;
        allHoldersResults = new ArrayList<>();
        hasBeenCanceled = false;
    }

    public Holder createHolder() {
        holdersCount++;

        return new Holder();
    }


    public void done() {
        if (futureTask != null) futureTask.done(null);
    }

    public boolean isHasBeenCanceled() {
        return hasBeenCanceled;
    }

    public boolean isWaiting() {
        return holdersCount > 0;
    }

    public void cancel() {
        hasBeenCanceled = true;
    }

    public ArrayList<T> getAllHoldersResults() {
        return allHoldersResults;
    }

    public boolean didAllDone() {
        return holdersCount == 0;
    }

    public class Holder {
        private boolean isReleased;


        private Holder() {
            isReleased = false;
        }

        public void release() {
            release(null);
        }

        public void release(T extra) {
            release(extra, false);
        }

        private void release(final T extra, boolean afterDelay) {
            if (hasBeenCanceled) return;

            if (isReleased) return;

            if (holdersCount == 1 && !afterDelay) {
                SomeApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        release(extra, true);
                    }
                }, 100);
                return;
            }

            isReleased = true;

            allHoldersResults.add(extra);
            holdersCount--;

            if (holdersCount == 0) {
                futureTask.done(allHoldersResults);
            }
        }
    }

    public interface SynchronizerCallback<T> {
        void done(ArrayList<T> extra);
    }
}