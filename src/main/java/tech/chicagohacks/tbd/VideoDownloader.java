package tech.chicagohacks.tbd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.VGet;

public class VideoDownloader {

	private VGet downloader;
	
	public VideoDownloader(String download, String saveLocation){
		try{
			URL url = new URL(download);
			File saveTo = new File(saveLocation);
			System.out.println("here");
			downloader = new VGet(url, saveTo);
			System.out.println("here1");
		}
		catch(Exception ex){
			App.getFirebaseConnection().writeUrlError();
		}
	}
	
	public void downloadVideo(){
		System.out.println("here2");
		downloader.download();
		System.out.println("here3");
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
}
