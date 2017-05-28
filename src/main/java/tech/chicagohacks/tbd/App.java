package tech.chicagohacks.tbd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.FileReader;
import java.io.BufferedReader;

public class App {
	
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
    	
    	ArrayList<String> frameText = (ArrayList<String>) processFrames();
    	
    	try {
			PrintWriter output = new PrintWriter("resources\\out.txt");
			
			for(String s : frameText){
				output.println(s);
			}
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String story = "";
    	String current = "";
    	BufferedReader br = null;
    	try {
			br = new BufferedReader(new FileReader("resources\\out.txt"));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try {
			while ((current = br.readLine()) != null) {
				story = story + current + "</br>";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	firebase.pushVideoData(getVideoTitle(videoFile.getName()), story);
    }
    
    private static String getVideoTitle(String path){
    	
    	String[] splitPath = path.split("\\.");
    	
    	return splitPath[splitPath.length - 2];
    }
    
    public static FirebaseConnection getFirebaseConnection(){
    	return firebase;
    }
    
    public static List<String> processFrames(){
		List<String> messages = new ArrayList<String>();
		BlockingQueue<String> imagesToProcess = new LinkedBlockingQueue<String>();
		int currentNumMessages = 0;
		
		try {
			final File f = new File("resources\\frames");
			for(File child : f.listFiles()) {
				DetectText.uploadFile(child, "bucket-caption");
				imagesToProcess.add("gs://bucket-caption/" + child.getName());
				
				if(messages.size() > currentNumMessages){
					String process = imagesToProcess.poll();
					if(process == null){
						break;
					}
					
					DetectText.detectTextGcs(process, messages);
				}
				
				currentNumMessages = messages.size();
			}
			
			//DetectText.detectTextGcs("resources/caption.png", new PrintStream("resources/out.txt"));
		} catch (IOException e) {
			System.out.println("Uploading and detecting text did not work");
			return null;
		}
		
		return messages;
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
