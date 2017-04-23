package previewcode.backend.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Handler;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;
import com.google.inject.Singleton;
import previewcode.backend.DTO.Approve;
import previewcode.backend.DTO.Ordering;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.DTO.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * An abstract class that connects with firebase
 */
@Singleton
public class FirebaseService {
    /**
     * The reference to the database
     */
    protected DatabaseReference ref;

    /**
     * Recursion count for the transaction
     */
    private static final int RECURSION_COUNT = 5;

    /**
     * Making a connection with the database
     */
    public FirebaseService() {

        ref = FirebaseDatabase.getInstance().getReference();
    }

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
    public void setApproved(String owner, String name, String number, Approve LGTM) {
        this.ref.child(owner).child(name).child("pulls").child(number)
                .child("hunkApprovals").child(LGTM.hunkId).child(String.valueOf(LGTM.githubLogin)).setValue(LGTM.isApproved);
    }

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

    /**
     * Makes the transaction this is a workaround as stated in: https://groups.google.com/forum/#!msg/firebase-talk/u1mgEEODF-o/v55dOFZiAAAJ
     *
     * @param path
     *                  The path where the transaction takes place
     * @param handler
     *                  The function that is executed in order to do the transaction
     */
    private void doTransaction(final DatabaseReference path, final Function<MutableData, Transaction.Result> handler, int recursion) {
        final DatabaseReference infoRef = this.ref.child(".info").child("connected");

        infoRef.addValueEventListener(new ValueEventListener() {

            final ValueEventListener that = this;

            @Override
            public void onDataChange(DataSnapshot connected) {
                if ((Boolean) connected.getValue()) {
                    path.runTransaction(new Handler() {

                        @Override
                        public void onComplete(DatabaseError error, boolean isCommited, DataSnapshot currentData) {
                            infoRef.removeEventListener(that);
                            if (error != null) {
                                if (recursion > 0) {
                                    FirebaseService.this.doTransaction(path, handler, recursion - 1);
                                } else {
                                    throw new RuntimeException(error.toException());
                                }
                            }
                        }

                        @Override
                        public Result doTransaction(MutableData data) {
                            return handler.apply(data);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException(error.toException());
            }
        });
    }

    /**
     * Sets the ordering of a pull request on firebase
     *
     * @param pullId The identifier object for the pull request
     */
    public void setOrdering(final PullRequestIdentifier pullId, List<Ordering> orderings) {

        DatabaseReference path = this.ref
                .child(pullId.owner)
                .child(pullId.name).child("pulls")
                .child(pullId.number.toString())
                .child("ordering");

        this.doTransaction(path, data -> {
            data.child("lastChanged").setValue(System.currentTimeMillis());
            data.child("groups").setValue(orderings);
            return Transaction.success(data);
        }, RECURSION_COUNT);
    }

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
    public void setStatus(String owner, String name, String number, String status) {
        this.ref.child(owner).child(name).child("pulls").child(number)
                .child("status").setValue(status);
    }

    public void addTracker(Track data) {
        DatabaseReference path = this.ref.child("users").child(data.userID).child("tracker").child(data.time);
        path.child("new").setValue(data.newPath);
        path.child("old").setValue(data.oldPath);
    }

    /**
     * Adds default information about a pull request, as there is no data present in our service.
     *
     * @param pullId The identifier object for the pull request
     */
    public void addDefaultData(PullRequestIdentifier pullId) {
        FirebaseService that = this;
        this.ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(pullId.owner + "/" + pullId.name + "/pulls/" + pullId.number + "/status")) {
                    that.ref.child(pullId.owner)
                            .child(pullId.name)
                            .child("pulls")
                            .child(pullId.number.toString())
                            .child("status")
                            .setValue("No status yet");

                    that.setOrdering(pullId, new ArrayList<>());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException(error.toException());
            }
        });
    }
}
