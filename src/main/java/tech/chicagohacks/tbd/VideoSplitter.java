package tech.chicagohacks.tbd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

/**
 * Splits an mp4 video into individual frames. As of now it only saves every 10 frames.
 * 
 * @author Eric
 */
public class VideoSplitter {
	
	private String saveLocation;
	
	private String videoFilePath;
	
	/**
	 * Initialize this VideoSplitter with a video path and a location to save the frames.
	 * @param videoFilePath
	 * @param saveLocation
	 */
	public VideoSplitter(String videoFilePath, String saveLocation){
		this.saveLocation = saveLocation;
		this.videoFilePath = videoFilePath;
	}
	
	/**
	 * Splits the video into frames
	 */
	public void splitIntoFrames(){
		Demuxer demuxer = Demuxer.make();
		try{
			demuxer.open(videoFilePath, null, false, true, null, null);
			int numberOfStreams = demuxer.getNumStreams();
			
			int streamId = -1;
			long streamStartTime = 0;
			Decoder videoDecoder = null;
			
			for(int i = 0; i < numberOfStreams; i++){
				DemuxerStream stream = demuxer.getStream(i);
				streamStartTime = stream.getStartTime();
				videoDecoder = stream.getDecoder();
				
				if(videoDecoder != null && videoDecoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO){
					streamId = i;
					break;
				}
			}
			
			if(streamId == -1){
				System.out.println("No stream was found in " + videoFilePath);
			}
			
			videoDecoder.open(null, null);
			
			MediaPicture frame = MediaPicture.make(videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());
			MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, frame);
			
			MediaPacket packet = MediaPacket.make();
			int offset = 0;
			int bytesRead = 0;
			while(demuxer.read(packet) >= 0){
				if(packet.getStreamIndex() == streamId){
					offset = 0;
					bytesRead = 0;
					
					do{
						bytesRead += videoDecoder.decode(frame, packet, offset);
						if(frame.isComplete()){
							saveFrameToGoogleCloud(frame, converter);
						}
					}while(offset < packet.getSize());
				}
			}
			
			do{
				videoDecoder.decode(frame, null, 0);

				if(frame.isComplete()){
					saveFrameToGoogleCloud(frame, converter);
				}
			} while(frame.isComplete());
			
			demuxer.close();
		}
		
		catch(IOException ex){
			System.out.println("uhhhh...");
		}
		catch(InterruptedException ex){
			System.out.println("uuhhhh again...");
		}
		
	}
	
	private int framesProcessed = 0;
	
	/**
	 * Writes the frame contained in the MediaPicture to Google Cloud
	 * @param picture
	 */
	private void saveFrameToGoogleCloud(MediaPicture picture, MediaPictureConverter converter){
		if(framesProcessed % 10 == 0){
			BufferedImage image = null; 
			image = converter.toImage(image, picture);
			++framesProcessed;
			
			//TODO: actually write it to Google Cloud
		}
	}
	
}
