package me.previewcode.backend.services.firebase;

import me.previewcode.backend.FirebaseConnection;

import com.google.inject.Singleton;

/**
 * Service for handling the status of a pull request
 *
 */
@Singleton
public class StatusService extends FirebaseConnection {

    /**
     * Sets the status of a pull request on firebase
     * 
     * @param owner
     *            The owner of the repository where the pull request is located
     * @param name
     *            The name of the repository where the pull request is located
     * @param number
     *            The number of the pull request
     * @param status
     *            The status of the pull request
     */
    public void setStatus(String owner, String name, String number,
            String status) {
        this.ref.child(owner).child(name).child("pulls").child(number)
                .child("status").setValue(status);
    }
}
