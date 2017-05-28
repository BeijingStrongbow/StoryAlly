package tech.chicagohacks.tbd;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

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
    	
    	VideoSplitter videoSplitter = new VideoSplitter(videoFile.getAbsolutePath());
    	
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
    }

	
}
