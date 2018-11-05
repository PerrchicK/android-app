package com.perrchick.someapplication.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

abstract class OnValueEventListener implements ValueEventListener {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        onDataChanged(dataSnapshot, null);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        onDataChanged(null, databaseError);
    }

    abstract void onDataChanged(@Nullable DataSnapshot dataSnapshot, @Nullable DatabaseError databaseError);
}
