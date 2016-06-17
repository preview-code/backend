package me.previewcode.backend;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * An abstract class that connects with firebase
 */
public abstract class FirebaseConnection {
    /**
     * The reference to the database
     */
    protected DatabaseReference ref;

    /**
     * Making a connection with the database
     */
    public FirebaseConnection() {

        ref = FirebaseDatabase.getInstance().getReference();
    }
}
