package com.perrchick.someapplication.utilities;

import com.perrchick.someapplication.SomeApplication;

import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by roee on 18/01/2017 and supervised by Perry.
 *
 * Improved by Perry, using:
 * https://developer.android.com/reference/java/util/concurrent/Future.html
 */
public class Synchronizer {
    private static final String TAG = Synchronizer.class.getSimpleName();
    private static final long DEFAULT_SEMAPHORE_TIMEOUT_MILLISECONDS = 60 * 1000;

    private static final HashMap<String, SynchronizerSemaphore> semaphores;

    static {
        semaphores = new HashMap<>();
    }

    private boolean beenCanceled;
    private int holdersCount = 0;
    private final FutureTask<Object> futureTask;
    private boolean allHoldersDoneSuccessfully = true;

    public Synchronizer(Runnable lastAction) {
        futureTask = new FutureTask<Object>(lastAction, "done");

        beenCanceled = false;

        AppLogger.log(TAG, "*** creating Synchronizer: " + this);
    }

    public Holder createHolder() {
        AppLogger.log(TAG, "*** createHolder: " + this);

        holdersCount++;

        return new Holder(this);
    }


    public void done() {
        if (futureTask != null) futureTask.run();
    }

    public boolean isBeenCanceled() {
        return beenCanceled;
    }

    public boolean isWaiting() {
        return holdersCount > 0;
    }

    public void cancel() {
        beenCanceled = true;

        if (futureTask != null) {
            futureTask.cancel(false);
        }
    }

    public boolean didAllHoldersDoneSuccessfully() {
        return allHoldersDoneSuccessfully;
    }

    public static SynchronizerSemaphore getSemaphore(String semaphoreKey) {
        SynchronizerSemaphore semaphore;
        synchronized (semaphores) {
            semaphore = semaphores.get(semaphoreKey);
            if (semaphore == null) {
                semaphore = new SynchronizerSemaphore(semaphoreKey);
                semaphores.put(semaphoreKey, semaphore);
            }
        }

        return semaphore;
    }

    public static SynchronizerSemaphore lock(String semaphoreKey) {
        SynchronizerSemaphore semaphore = getSemaphore(semaphoreKey);

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return semaphore;
    }

    public boolean didAllDone() {
        return holdersCount == 0;
    }

    public static class SynchronizerSemaphore extends Semaphore {
        private static final String _TAG = SynchronizerSemaphore.class.getSimpleName();
        private int acquiresCounter;
        private String semaphoreKey;


        private SynchronizerSemaphore(String semaphoreKey) {
            super(1);
            acquiresCounter = 0;
            this.semaphoreKey = semaphoreKey;
        }

        @Override
        public void acquire() throws InterruptedException {
            acquire(null);
        }

        private void acquire(Long timeout) throws InterruptedException {
            if (PerrFuncs.isRunningOnMainThread()) {
                AppLogger.log(_TAG, "creating semaphore on the main thread!");
            }

            acquiresCounter++;
            AppLogger.log(_TAG, "acquiring '" + semaphoreKey + "', count == " + acquiresCounter);

            if (timeout == null) {
                timeout = DEFAULT_SEMAPHORE_TIMEOUT_MILLISECONDS;
            }

            if (acquiresCounter > 1) {
                AppLogger.error(_TAG, "potential dead lock detected! '" + semaphoreKey + "', count == " + acquiresCounter);
            }

            super.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        }

        @Override
        public void release() {
        }
    }

    public class Holder {
        private final Synchronizer parent; // Is this necessary? Or it's automatically supplied by JAVA.
        private boolean isReleased;


        private Holder(Synchronizer parent) {
            this.parent = parent;
            isReleased = false;
        }

        public void release() {
            release(true);
        }

        public void release(boolean doneSuccessfully) {
            release(doneSuccessfully, false);
        }

        private void release(final boolean doneSuccessfully, boolean afterDelay) {
            if (isReleased) return;

            if (holdersCount == 1 && !afterDelay) {
                SomeApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        release(doneSuccessfully, true);
                    }
                }, 100);
                return;
            }

            isReleased = true;

            allHoldersDoneSuccessfully &= doneSuccessfully;
//            synchronized (releaseSemaphore) {
            holdersCount--;

            if (holdersCount == 0) {
                AppLogger.log(TAG, "*** release -> done: " + parent);

                futureTask.run();
            } else {
                AppLogger.log(TAG, "*** release: " + parent);
            }
        }
    }
}