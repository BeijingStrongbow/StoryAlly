package tech.chicagohacks.tbd;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;


public class DetectText {
	
	/*public static void main(String... args) throws Exception {
		upload("Title here");
	    //detectTextGcs("C:/Users/inspire314/workspace/CaptionApp/src/main/resources/caption.PNG", new PrintStream("C:/Users/inspire314/workspace/CaptionApp/src/main/resources/out.txt"));
	}*/
	
	public static String uploadFile(File filePart, final String bucketName) throws IOException {
		Storage storage = StorageOptions.getDefaultInstance().getService();
	    final String fileName = filePart.getName();

	    // the inputstream is closed by default, so we don't need to close it here
	    BlobInfo blobInfo =
	        storage.create(
	            BlobInfo
	                .newBuilder(bucketName, fileName)
	                // Modify access list to allow all users with link to read file
	                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
	                .build(),
	            FileUtils.openInputStream(filePart));
	    // return the public download link
	    return blobInfo.getMediaLink();
	  }
	
	public static String convertFileToText(String path) throws IOException {
		InputStream is = new FileInputStream(path); // path is the file that is being read
		BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
		String line = buf.readLine(); 
		StringBuilder sb = new StringBuilder(); 
		while(line != null) { 
			sb.append(line).append("\n"); 
			line = buf.readLine(); 
		} 
		buf.close();
		String fileAsString = sb.toString(); 
		return fileAsString;

	}
	
	public static void detectTextGcs(String gcsPath, PrintStream out) throws IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();
		ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
		
		//ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
		Image img = Image.newBuilder().setSource(imgSource).build();
		Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);
		
		BatchAnnotateImagesResponse response = ImageAnnotatorClient.create().batchAnnotateImages(requests);
		List<AnnotateImageResponse> responses = response.getResponsesList();
		int temp = -1;
		int count = 0;
		
		for (AnnotateImageResponse res : responses) {
			if (res.hasError()) {
			  out.printf("Error: %s\n", res.getError().getMessage());
			  return;
			}
	
			// For full list of available annotations, see http://g.co/cloud/vision/docs
			// Print text
			
			for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
				if (count > 0) {
					if (count > 1 && !(annotation.getBoundingPoly().getVertices(3).getY() < temp + 5) && (annotation.getBoundingPoly().getVertices(3).getY() > temp - 5)) {
						//out.println();
					}
					out.printf("%s ", annotation.getDescription());
					/*out.printf("Position : %s\n", annotation.getBoundingPoly());
					out.println();*/
					
					//out.println(annotation.getBoundingPoly().getVertices(3).getY());
					temp = annotation.getBoundingPoly().getVertices(3).getY();
				}
				count++;
				System.out.println();
				
			}
		}
	}
	
}