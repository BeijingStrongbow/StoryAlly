package tech.chicagohacks.tbd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
}
