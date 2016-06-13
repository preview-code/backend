package me.previewcode.backend.api.v1;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.FirebaseConnection;
import me.previewcode.backend.DTO.StatusBody;

import com.google.firebase.database.DatabaseReference;

@Path("/pr")
public class PullRequestAPI extends FirebaseConnection {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{owner}/{name}/{branch}/status/")
    public StatusBody setStatus(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("branch") String number,
            StatusBody data) throws IOException {
        owner = owner.toLowerCase();
        name = name.toLowerCase();
        DatabaseReference projectRef = this.ref;
        projectRef.child(owner).child(name).child("pulls").child(number)
                .child("status").setValue(data.status);
        return data;
    }
}
