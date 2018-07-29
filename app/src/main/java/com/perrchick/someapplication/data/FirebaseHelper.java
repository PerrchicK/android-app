package com.perrchick.someapplication.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class FirebaseHelper {
    public static void queryIngredients(AppCompatActivity activity, final PerrFuncs.CallbacksHandler<String> callbacksHandler) {
        LiveData liveData = null;
        liveData.observe(activity, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {

            }
        });
        Firebase firebaseRef = new Firebase("");
        firebaseRef.authWithPassword("email", "password", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                callbacksHandler.onCallback(authData.getUid());
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                callbacksHandler.onCallback(null);
            }
        });

    }
}
