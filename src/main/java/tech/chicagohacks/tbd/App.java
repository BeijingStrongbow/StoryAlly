package tech.chicagohacks.tbd;

import java.io.IOException;

import io.humble.video.Demuxer;

public class App 
{
    public static void main( String[] args )
    {
    	VideoSplitter splitter = new VideoSplitter("C:\\Users\\ericd\\Desktop\\video.mp4", "C:\\Users\\ericd\\Desktop\\frames");
    	try{
        	splitter.splitIntoFrames();
    	}
    	catch(InterruptedException ex){
    		System.out.println("sdlfksjdlf");
    	}
    	catch(IOException ex){
    		System.out.println("lkjsldkfjsldk");
    	}
    }
}
