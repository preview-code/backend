package previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import previewcode.backend.DTO.Track;
import previewcode.backend.services.FirebaseService;

import com.google.inject.Inject;

/**
 * API endpoint for the status of a pull request
 */
@Path("tracker/")
public class TrackerAPI {

    @Inject
    private FirebaseService firebaseService;

    /**
     * Adds a tracker event to firebase
     *
     * @param body
     *            The body in which the status is saved
     * @return The body of the status
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void addTracker(Track body) {
        firebaseService.addTracker(body);
    }
}
