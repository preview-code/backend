package me.previewcode.backend;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.google.firebase.database.DatabaseReference;

@Path("/message")
public class MessageRestService extends FirebaseConnection {

	@GET
	@Path("/{param}")
	public Response printMessage(@PathParam("param") String msg) {

		DatabaseReference usersRef = this.ref.child("users");
		usersRef.child("Eva").setValue(msg);
		usersRef.child("Thomas").setValue(msg);
		usersRef.child("Tim").setValue(msg);

		String result = "Restful example : " + msg;

		return Response.status(200).entity(result).build();

	}

}
