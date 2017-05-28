package tech.chicagohacks.tbd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseConnection {
	
	private DatabaseReference readVideoUrlRef;
	
	private DatabaseReference pushVideoDataRef;
	
	public FirebaseConnection(String databaseUrl){
	
		try{
			FileInputStream key = new FileInputStream("project-61475-firebase-adminsdk-b5jjb-6e711a3f18.json");
			
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setDatabaseUrl(databaseUrl)
					.setCredential(FirebaseCredentials.fromCertificate(key))
					.build();
			
			FirebaseApp.initializeApp(options);
			
		}
		catch(FileNotFoundException ex){
			System.out.println("Couldn't load Firebase key");
			return;
		}
		catch(IOException ex){
			System.out.println("IOException with firebase.");
			return;
		}
	
		readVideoUrlRef = FirebaseDatabase.getInstance().getReference("Link").child("link");

		pushVideoDataRef = FirebaseDatabase.getInstance().getReference("Stories");
		
		readVideoUrlRef.addValueEventListener(new ValueEventListener(){

			public void onCancelled(DatabaseError arg0) {}

			public void onDataChange(DataSnapshot snapshot) {
				String url = (String) snapshot.getValue();
				System.out.println(url);
				App.setVideoUrl(url);
			}
		});
	}
	
	public void pushVideoData(String title, String story){
		String newStoryKey = pushVideoDataRef.push().getKey();
		DatabaseReference newStory = pushVideoDataRef.child(newStoryKey);
		newStory.child("title").setValue(title);
		newStory.child("story").setValue(story);
	}
	
	public void writeUrlError(){
		readVideoUrlRef.setValue("Invalid URL");
	}
}
