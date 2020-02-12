package com.perrchick.someapplication.utilities;

import androidx.annotation.Nullable;

import com.perrchick.someapplication.SomeApplication;

import java.util.ArrayList;

/**
 * A helper class that syncs all callbacks into one callback.
 *
 * Created by roee on 18/01/2017, supervised and improved by Perry.
 */
public class Synchronizer<RESULT_TYPE> {
    private boolean hasBeenCanceled;
    private int holdersCount = 0;
    private final SynchronizerCallback<RESULT_TYPE> futureTask;
    private ArrayList<RESULT_TYPE> allHoldersResults;

    public Synchronizer(SynchronizerCallback<RESULT_TYPE> lastAction) {
        futureTask = lastAction;
        allHoldersResults = new ArrayList<>();
        hasBeenCanceled = false;
    }

    public static <CLASS> SyncedSynchronizer<CLASS> makeSyncedSynchronizer() {
        return new SyncedSynchronizer<>();
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

    public ArrayList<RESULT_TYPE> getAllHoldersResults() {
        return allHoldersResults;
    }

    public boolean didAllDone() {
        return holdersCount == 0;
    }

    public static class SyncedSynchronizer<RESULT_CLASS> {
        private boolean hasBeenCanceled;
        private boolean isDone;
        private boolean isStarted;
        private ArrayList<OperationHolder<RESULT_CLASS>> operations;
        private ArrayList<RESULT_CLASS> operationResults;

        private SyncedSynchronizer() {
            operations = new ArrayList<>();
            operationResults = new ArrayList<>();
            hasBeenCanceled = false;
        }

        private void add(OperationHolder<RESULT_CLASS> operationHolder) {
            operations.add(operationHolder);
        }

        public ArrayList<RESULT_CLASS> getOperationResults() {
            return new ArrayList<>(operationResults);
        }

        public boolean isCanceled() {
            return hasBeenCanceled;
        }

        public void cancel() {
            hasBeenCanceled = true;
        }

        public SyncedSynchronizer<RESULT_CLASS> addOperation(OperationHolder<RESULT_CLASS> operationHolder) {
            if (operationHolder == null) return this;
            add(operationHolder);
            return this;
        }

        private void allDone() {
            isDone = true;
        }

        public boolean isDone() {
            return isDone;
        }

        public SyncedSynchronizer<RESULT_CLASS> carryOn() {
            return carryOn(null);
        }

        public SyncedSynchronizer<RESULT_CLASS> carryOn(RESULT_CLASS extra) {
            if (hasBeenCanceled) return this;

            if (isStarted) {
                operationResults.add(extra);
            } else {
                isStarted = true;
            }

            if (operations.size() == 0) {
                allDone();
                return this;
            }

            OperationHolder<RESULT_CLASS> nexOperation = operations.remove(0);

            if (nexOperation == null || nexOperation.isDone) {
                carryOn(extra);
                return this;
            }

            nexOperation.isDone = true;
            nexOperation.onMyTurn(this);
            return this;
        }

        @Nullable
        public RESULT_CLASS lastResult() {
            if (operationResults.size() < 1) return null;
            return operationResults.get(operationResults.size() - 1);
        }
    }

    public class Holder {
        private boolean isReleased;

        private Holder() {
            isReleased = false;
        }

        public void release() {
            release(null);
        }

        public void release(RESULT_TYPE extra) {
            release(extra, false);
        }

        private void release(final RESULT_TYPE extra, boolean afterDelay) {
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

    public abstract static class OperationHolder<CLASS> {
        boolean isDone = false;
        protected abstract void onMyTurn(Synchronizer.SyncedSynchronizer<CLASS> synchronizer);
    }

    public interface SynchronizerCallback<TYPE> {
        void done(ArrayList<TYPE> extra);
    }
}