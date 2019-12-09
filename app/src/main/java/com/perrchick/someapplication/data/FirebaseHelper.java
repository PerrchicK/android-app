package com.perrchick.someapplication.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.perrchick.someapplication.SomeApplication;
import com.perrchick.someapplication.utilities.AppLogger;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class FirebaseHelper {

    private class Keys {
        private static final String TEMP_NODE_FOR_TIMESTAMP_KEY = "tempNodeForFirebaseTimestamp";
        private static final String CLOCK_DIFF_REFERENCE_KEY = "ClockDiff";
    }
    private static final String TAG = FirebaseHelper.class.getSimpleName();
    private static Boolean _isFirebaseConnected;
    private static ValueEventListener isConnectedEventListener;
    @Nullable
    private static Long _timestampDelta;
    private static final DatabaseReference isConnectedReference;

    private static final DatabaseReference DB_ROOT;
    private static final DatabaseReference CLOCK_DIFF_REFERENCE;

    static {
        FirebaseApp.initializeApp(SomeApplication.getContext());
        DB_ROOT = FirebaseDatabase.getInstance().getReference();
        isConnectedReference = FirebaseDatabase.getInstance().getReference(".info/connected");
        CLOCK_DIFF_REFERENCE = DB_ROOT.child(Keys.CLOCK_DIFF_REFERENCE_KEY);
        updateIsConnectedValue();
    }

    private static DatabaseReference PATH_TO_CLOCK_DIFF(String uid) {
        return uid != null ? CLOCK_DIFF_REFERENCE.child(uid) : null;
    }

    public static void initialize() {
        //FirebaseApp.initializeApp(SomeApplication.getContext());
        AppLogger.log(TAG, FirebaseApp.class.getName());
    }

    private static void updateIsConnectedValue() {
        if (isConnectedEventListener != null) {
            isConnectedReference.removeEventListener(isConnectedEventListener);
            isConnectedEventListener = null;
        }

        isConnectedEventListener = isConnectedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                _isFirebaseConnected = snapshot.getValue(Boolean.class);
                if (_isFirebaseConnected == null) {
                    _isFirebaseConnected = false;
                }

                if (_isFirebaseConnected) {
                    refreshTimestampDelta();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AppLogger.error(TAG, "onCancelled: Listener was cancelled");
            }
        });
    }

    // Uncompleted test
    public static void queryIngredients(AppCompatActivity activity, final PerrFuncs.CallbacksHandler<String> callbacksHandler) {
        LiveData liveData = null;
        liveData.observe(activity, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {

            }
        });

        FirebaseAuth.getInstance().signInWithEmailAndPassword("", "").addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String uid;
                if (task.getResult() != null) {
                    uid = task.getResult().getUser().getUid();
                } else {
                    uid = null;
                }

                callbacksHandler.onCallback(uid);
            }
        });
    }

    @Nullable
    public static Long getTimestampDiff() {
        if (_timestampDelta == null) {
            refreshTimestampDelta();
        }
        return _timestampDelta;
    }

    private static void refreshTimestampDelta() {
        //String uid = LoginManager.getInstance().getUid();
        String uid = PerrFuncs.generateGuid();
        if (uid == null || _isFirebaseConnected == null || !_isFirebaseConnected) return;

        DatabaseReference tempTimestampRef = PATH_TO_CLOCK_DIFF(uid).child(Keys.TEMP_NODE_FOR_TIMESTAMP_KEY).child("deleteMe");
        final long deviceTimestamp = PerrFuncs.getCurrentDeviceTimestamp();

        tempTimestampRef.addListenerForSingleValueEvent(new OnValueEventListener() {
            @Override
            protected void onDataChanged(DataSnapshot dataSnapshot, DatabaseError databaseError) {
                // Step #2
                long updateCallbackTimestamp = PerrFuncs.getCurrentDeviceTimestamp();
                // https://en.wikipedia.org/wiki/Round-trip_delay_time
                final long updateRequestRoundTrip = (updateCallbackTimestamp - deviceTimestamp);

                if (dataSnapshot != null) {
                    // http://searchnetworking.techtarget.com/definition/round-trip-time
                    Long firebaseTimestamp = dataSnapshot.getValue(Long.class);

                    if (firebaseTimestamp != null) {
                        _timestampDelta = firebaseTimestamp - deviceTimestamp;
                        AppLogger.log(TAG, "refreshTimestampDelta: The request round trip took " + updateRequestRoundTrip + " millis, (firebaseTimestamp - deviceTimestamp) = timestamp delta = (" + firebaseTimestamp + " - " + deviceTimestamp + ") = " + _timestampDelta);
                    }

                    // PerrFuncs.getCurrentDeviceTimestamp() + (realTimestamp - PerrFuncs.getCurrentDeviceTimestamp());
                    // 16:30 + ( {15:00} - {16:00} ) = 15:30
                    dataSnapshot.getRef().removeValue();
                    SomeApplication.PrivateEventBus.notify(SomeApplication.PrivateEventBus.Action.FIREBASE_IS_READY);
                } else if (databaseError != null) {
                    AppLogger.error(TAG, "refreshTimestampDelta: Failed, database error = " + databaseError);
                } else {
                    AppLogger.error(TAG, "refreshTimestampDelta: Failed");
                }
            }
        });

        // Step #1
        tempTimestampRef.setValue(ServerValue.TIMESTAMP);
    }
}
