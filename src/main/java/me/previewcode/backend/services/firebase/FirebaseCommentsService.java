package me.previewcode.backend.services.firebase;

import me.previewcode.backend.FirebaseConnection;

import com.google.inject.Singleton;

/**
 * Service for handling comments on Firebase
 *
 */
@Singleton
public class FirebaseCommentsService extends FirebaseConnection {

    /**
     * Setting comments on Firebase
     * 
     * @param owner
     *            The owner of the repository where the comment is posted
     * @param name
     *            The name of the repository where the comment is posted
     * @param number
     *            The number of the pull request
     * @param commentID
     *            The id of the comment
     * @param groupID
     *            The id of the group
     */
    public void setComments(String owner, String name, int number,
            Integer commentID, String groupID) {
        owner = owner.toLowerCase();
        name = name.toLowerCase();
        this.ref.child(owner).child(name).child("pulls")
                .child(Integer.toString(number)).child("groupcomments")
                .child(Integer.toString(commentID)).setValue(groupID);
    }
}
