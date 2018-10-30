package com.perrchick.someapplication.utilities;

import android.support.annotation.Nullable;

import com.perrchick.someapplication.SomeApplication;

import java.util.ArrayList;

/**
 * Created by roee on 18/01/2017, supervised and improved by Perry.
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

    public static <CLASS> SyncedSynchronizer<CLASS> makeSyncedSynchronizer() {
        SyncedSynchronizer<CLASS> syncedSynchronizer = new SyncedSynchronizer<>();
        return syncedSynchronizer;
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

    public static class SyncedSynchronizer<CLASS> {
        private boolean hasBeenCanceled;
        private ArrayList<OperationHolder<CLASS>> operations;
        private ArrayList<CLASS> operationResults;

        private SyncedSynchronizer() {
            operations = new ArrayList<>();
            operationResults = new ArrayList<>();
            hasBeenCanceled = false;
        }

        private void add(OperationHolder<CLASS> operationHolder) {
            operations.add(operationHolder);
        }

        public ArrayList<CLASS> getOperationResults() {
            return new ArrayList<>(operationResults);
        }

        public boolean isCanceled() {
            return hasBeenCanceled;
        }

        public void cancel() {
            hasBeenCanceled = true;
        }

        public SyncedSynchronizer<CLASS> addOperation(OperationHolder<CLASS> operationHolder) {
            if (operationHolder == null) return this;
            add(operationHolder);
            return this;
        }

        private void allDone() {
            //finalOperation();
        }

        public SyncedSynchronizer<CLASS> doNext() {
            return doNext(null);
        }

        public SyncedSynchronizer<CLASS> doNext(CLASS extra) {
            return _doNext(extra);
        }

        private SyncedSynchronizer<CLASS> _doNext(final CLASS extra) {
            if (hasBeenCanceled) return this;
            if (operations.size() == 0) {
                allDone();
                return this;
            }
            operationResults.add(extra);
            OperationHolder<CLASS> nexOperation = operations.remove(0);

            if (nexOperation == null || nexOperation.isDone) {
                _doNext(extra);
                return this;
            }

            nexOperation.isDone = true;
            nexOperation.onMyTurn(this);
            return this;
        }

        @Nullable
        public CLASS lastResult() {
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

    public abstract static class OperationHolder<CLASS> {
        boolean isDone = false;
        protected abstract void onMyTurn(Synchronizer.SyncedSynchronizer<CLASS> synchronizer);
    }

    public interface SynchronizerCallback<T> {
        void done(ArrayList<T> extra);
    }
}