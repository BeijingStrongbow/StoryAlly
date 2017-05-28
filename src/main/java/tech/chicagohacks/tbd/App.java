package tech.chicagohacks.tbd;

import java.io.File;
import java.io.FilenameFilter;
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

public class App 
{
	
	private static FirebaseConnection firebase;
			
	private static final String DatabaseUrl = "https://project-61475.firebaseio.com/";
	private static final String VideoSaveLocation = "resources";
	
	private static String videoUrl = "";
	
	public static void setVideoUrl(String url){
		videoUrl = url;
	}
	
    public static void main(String[] args)
    {
    	firebase = new FirebaseConnection(DatabaseUrl);
    	
    	while(true){
    		System.out.println("." + videoUrl);
    		if(!videoUrl.equals("")){
    			processVideo(videoUrl);
    		}
    	}
    }
    
    public static void processVideo(String url){
    	videoUrl = "";
    	System.out.println(url);
    	VideoDownloader videoDownloader = new VideoDownloader(url, VideoSaveLocation);
    	videoDownloader.downloadVideo();
    	
    	File file = new File(VideoSaveLocation);
    	File videoFile = file.listFiles(new FileTest())[0];
    	
    	VideoSplitter videoSplitter = new VideoSplitter(videoFile.getAbsolutePath(), VideoSaveLocation + "\\frames");
    	
    	try{
        	videoSplitter.splitIntoFrames();
    	}
    	catch(InterruptedException ex){
    		System.err.println("Video processing was interrupted");
    		return;
    	}
    	catch(IOException ex){
    		System.err.println("Unable to process video");
    		return;
    	}
    	
    	//process the story
    	String story = "test meme asdf haha";
    	firebase.pushVideoData(getVideoTitle(videoFile.getName()), story);
    }
    
    private static String getVideoTitle(String path){
    	
    	String[] splitPath = path.split("\\.");
    	
    	return splitPath[splitPath.length - 2];
    }
    
    public static FirebaseConnection getFirebaseConnection(){
    	return firebase;
    }
    
    public static class FileTest implements FilenameFilter {
    	public boolean accept(File dir, String name) {
    		if(dir.getName().equals(VideoSaveLocation)){
    			String[] splitName = name.split("\\.");
    			
    			if(splitName.length == 0){
    				return false;
    			}
    			else if(splitName[splitName.length - 1].equals("mp4") || splitName[splitName.length - 1].equals("webm")){
    				return true;
    			}
    			else{
    				return false;
    			}
    			
    		}
    		
    		else{
    			return false;
    		}
    	}
    	
    	/*try {
    		PrintStream out = new PrintStream("resources\\out.txt");
			final File file = new File("resources\\frames");
			for(File child : file.listFiles()) {
				//DetectText.uploadFile(child, "bucket-caption");
				//DetectText.detectTextGcs("gs://bucket-caption/" + child.getName(), out);
			}
			//DetectText.detectTextGcs("resources/caption.png", new PrintStream("resources/out.txt"));
		} catch (IOException e) {
			System.out.println("Uploading and detecting text did not work");
		}*/
    }
}
