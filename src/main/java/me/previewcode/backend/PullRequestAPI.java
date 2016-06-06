package me.previewcode.backend;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.DTO.StatusBody;

import com.google.firebase.database.DatabaseReference;

@Path("/pr")
public class PullRequestAPI extends FirebaseConnection {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{owner}/{name}/{branch}/status/")
    public StatusBody getStatus(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("branch") String number,
            StatusBody data) throws IOException {
        owner = owner.toLowerCase();
        name = name.toLowerCase();
        DatabaseReference projectRef = this.ref.child("pulls");
        projectRef.child(owner).child(name).child("branch").child(number)
                .child("status").setValue(data.status);
        return data;
    }
}
