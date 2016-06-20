package me.previewcode.backend.services.firebase;

import me.previewcode.backend.FirebaseConnection;
import me.previewcode.backend.DTO.Approve;

import com.google.inject.Singleton;

/**
 * Service for handling the status of a pull request
 *
 */
@Singleton
public class AssigneesService extends FirebaseConnection {

    /**
     * Sets the LGTM of a hunk in firebase
     * 
     * @param owner
     *            The owner of the repository where the pull request is located
     * @param name
     *            The name of the repository where the pull request is located
     * @param number
     *            The number of the pull request
     * @param LGTM
     *            The approved object with information for firebase
     */
    public void setLGTM(String owner, String name, String number,
            Approve LGTM) {
        //          return project.owner + '/' + project.name + '/pulls/' + pullRequest.number + '/hunkApprovals';
        this.ref.child(owner).child(name).child("pulls").child(number)
                .child("hunkApprovals").child(LGTM.hunkId).child(LGTM.githubLogin).setValue(LGTM.isApproved);
    }
}
