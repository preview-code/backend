package me.previewcode.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Module;
import com.google.inject.Binder;

public class HelloModule implements Module
{
	public void configure(final Binder binder)
	{
		binder.bind(MessageRestService.class);
		FileInputStream file = null;
		try {
			file = new FileInputStream("src/main/resources/firebase-auth.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Initialize the app with a service account, granting admin privileges
		FirebaseOptions options = new FirebaseOptions.Builder()
		.setServiceAccount(file)
		.setDatabaseUrl("https://preview-code.firebaseio.com/")
		.build();
		FirebaseApp.initializeApp(options);
	}	
}
