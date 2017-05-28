package tech.chicagohacks.tbd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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
		this.videoFilePath = videoFilePath;
		this.saveLocation = saveLocation;
	}
	
	/**
	 * Splits the video into frames
	 */
	public void splitIntoFrames() throws IOException, InterruptedException{
		Demuxer demuxer = Demuxer.make();
		demuxer.open(videoFilePath, null, false, true, null, null);
		int numberOfStreams = demuxer.getNumStreams();
		
		File me = new File(videoFilePath);
		fileSize = me.length();
		
		int streamId = -1;
		long streamStartTime = 0;
		Decoder videoDecoder = null;
		
		for(int i = 0; i < numberOfStreams; i++){
			final DemuxerStream stream = demuxer.getStream(i);
			streamStartTime = stream.getStartTime();
			final Decoder decoder = stream.getDecoder();
			
			if(decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO){
				streamId = i;
				videoDecoder = decoder;
				break;
			}
		}
		
		if(streamId == -1){
			System.out.println("No stream was found in " + videoFilePath);
		}
		
		videoDecoder.open(null, null);
		
		final MediaPicture frame = MediaPicture.make(videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());
		final MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, frame);
		
		final MediaPacket packet = MediaPacket.make();

		while(demuxer.read(packet) >= 0){
			if(packet.getStreamIndex() == streamId){
				int offset = 0;
				int bytesRead = 0;
				
				do{
					bytesRead += videoDecoder.decode(frame, packet, offset);
					if(frame.isComplete()){
						saveFrameToGoogleCloud(frame, converter);
					}
					
					offset += bytesRead;
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
	
	private int framesProcessed = 0;
	
	private long fileSize;
	
	/**
	 * Writes the frame contained in the MediaPicture to Google Cloud
	 * @param picture
	 */
	private void saveFrameToGoogleCloud(MediaPicture picture, MediaPictureConverter converter){
		if(framesProcessed % 60 == 0){
			BufferedImage image = null; 
			image = converter.toImage(image, picture);
			
			try{
				File file = new File(saveLocation + "\\frame" + framesProcessed + ".png");
				ImageIO.write(image, "png", file);
				
				App.getFirebaseConnection().addProgress((int) (file.length() / fileSize * 0.4));
				
				//DetectText.uploadFile(file, "bucket-caption");
				// Upload images to Google Cloud Storage
				
			}
			catch(IOException ex){
				
			}
		}
		++framesProcessed;
	}
	
}
