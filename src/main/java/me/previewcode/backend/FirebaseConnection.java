package me.previewcode.backend;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class FirebaseConnection {

    protected DatabaseReference ref;

    public FirebaseConnection() {

        ref = FirebaseDatabase.getInstance().getReference();
    }
}
