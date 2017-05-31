package previewcode.backend.services;

import com.google.firebase.database.*;
import com.google.firebase.database.Transaction.Handler;
import com.google.firebase.database.Transaction.Result;
import com.google.inject.Singleton;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import previewcode.backend.DTO.ApproveRequest;
import previewcode.backend.DTO.OrderingGroup;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.DTO.Track;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * An abstract class that connects with firebase
 */
@Singleton
public class FirebaseService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    /**
     * The reference to the database
     */
    protected DatabaseReference ref;

    /**
     * Recursion count for the transaction
     */
    private static final int RETRY_COUNT = 5;

    /**
     * Making a connection with the database
     */
    public FirebaseService() {
        logger.debug("Instantiating Firebase connection");
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
    public void setApproved(String owner, String name, String number, ApproveRequest LGTM) {
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
    private CompletableFuture<DataSnapshot> doTransaction(
            final DatabaseReference path,
            final Function<MutableData, Transaction.Result> handler,
            int retries) {
        final CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        final DatabaseReference infoRef = this.ref.child(".info").child("connected");

        logger.debug("Initializing Firebase transaction");
        infoRef.addValueEventListener(new ValueEventListener() {

            final ValueEventListener that = this;

            @Override
            public void onDataChange(DataSnapshot connected) {
                if ((Boolean) connected.getValue()) {
                    logger.debug("Connection with Firebase established");
                    path.runTransaction(new Handler() {

                        @Override
                        public void onComplete(DatabaseError error, boolean isCommited, DataSnapshot currentData) {
                            infoRef.removeEventListener(that);
                            if (error != null) {
                                if (retries > 0) {
                                    logger.debug("Transaction failed with error");
                                    logger.debug("  Error code: " + error.getCode());
                                    logger.debug("  Error message: " + error.getMessage());
                                    logger.debug("  Error details: " + error.getDetails());
                                    logger.debug("  " + retries + " retry attempts left");
                                    FirebaseService.this.doTransaction(path, handler, retries - 1);
                                } else {
                                    logger.error("Transaction failed: ", error.toException());
                                    future.completeExceptionally(new RuntimeException(error.toException()));
                                }
                            } else {
                                logger.debug("Transaction completed successfully");
                                future.complete(currentData);
                            }
                        }

                        @Override
                        public Result doTransaction(MutableData data) {
                            logger.debug("Performing transaction");
                            return handler.apply(data);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.toException()));
            }
        });

        return future;
    }

    /**
     * Sets the ordering of a pull request on firebase
     *
     * @param pullId The identifier object for the pull request
     */
    public void setOrdering(final PullRequestIdentifier pullId, List<OrderingGroup> orderings) {

        DatabaseReference path = this.ref
                .child(pullId.owner)
                .child(pullId.name).child("pulls")
                .child(pullId.number.toString())
                .child("ordering");

        logger.info("Updating ordering on Firebase");
        path.child("lastChanged").setValue(System.currentTimeMillis());
        path.child("groups").setValue(orderings);
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
        DatabaseReference path = this.ref.child("tracking").child(data.time);
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
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                throw new RuntimeException(error.toException());
            }
        });
    }
}
