package tech.chicagohacks.tbd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.SQLAdminScopes;

import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class App {
	
    public static void main( String[] args )
    {    	
    	GoogleCredential credential = null;
    	JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    	HttpTransport httpTransport = null;
    	
    	try {
			credential = GoogleCredential.fromStream(new FileInputStream("resources\\Captions-264752100053.json"))
				    .createScoped(Collections.singleton(SQLAdminScopes.SQLSERVICE_ADMIN));
	    	
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	SQLAdmin sqladmin =
    		    new SQLAdmin.Builder(httpTransport, JSON_FACTORY, credential).build();
    	
    	VideoSplitter splitter = new VideoSplitter("resources\\video.mp4", "resources\\frames");
    	try{
        	splitter.splitIntoFrames();
    	}
    	catch(InterruptedException ex){
    		System.out.println("sdlfksjdlf");
    	}
    	catch(IOException ex){
    		System.out.println("lkjsldkfjsldk");
    	}
    	
    	try {
    		PrintStream out = new PrintStream("resources\\out.txt");
			final File file = new File("resources\\frames");
			for(File child : file.listFiles()) {
				DetectText.uploadFile(child, "bucket-caption");
				DetectText.detectTextGcs("gs://bucket-caption/" + child.getName(), out);
				//System.out.println(child.getPath());
			    out.println();
			}
			//DetectText.detectTextGcs("resources/caption.png", new PrintStream("resources/out.txt"));
		} catch (IOException e) {
			System.out.println("Uploading and detecting text did not work");
		}
    }
    
}
