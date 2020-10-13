package com.parth.carReck;

import java.io.*;
import java.util.concurrent.TimeUnit;
import com.amazonaws.*;
import com.amazonaws.services.sqs.*;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.*;
import com.amazonaws.regions.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.rekognition.model.*;
import java.util.List;

public class carReck {

	public static void detectLabels(String photo) throws Exception{
      	String bucket = "njit-cs-643";
		String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/700559207820/cloudpro1";
      	AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

	    DetectLabelsRequest request = new DetectLabelsRequest()
		   .withImage(new Image()
		   .withS3Object(new S3Object()
           	   .withName(photo).withBucket(bucket)))
		   .withMaxLabels(10)
		   .withMinConfidence(75F);

	    try {
			DetectLabelsResult result = rekognitionClient.detectLabels(request);
			List <Label> labels = result.getLabels();

			// final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

			for (Label label: labels) {
				System.out.println(label.getName().equals("Car"));
				// if(label.getName().equals("Car") && label.getConfidence() > 90){
				// 	SendMessageRequest sendMessageRequest = new SendMessageRequest(myQueueUrl,
				// 	photo.substring(0, photo.length()-4));
				// 	final SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
				// 	System.out.println("Car Detected!");		
				// 	System.out.println("Sending " +photo+ " to second EC2!");
				// }
			}
			System.out.println();
	    } catch(AmazonRekognitionException e) {
		 	e.printStackTrace();
	    }
	}


    public static void main(String[] args)throws Exception{

		AmazonS3 s3 = new AmazonS3Client();
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(usWest2);
    	String BUCKETNAME = "njit-cs-643";
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(BUCKETNAME));
        for (S3ObjectSummary obj : objectListing.getObjectSummaries()) {	
            System.out.println(" - " + obj.getKey() + "  " +
                    "(size = " + obj.getSize() + ")");
			// detectLabels(obj.getKey());
		}

		// System.out.println();
		// System.out.println("Sending Final msg to queue");
		// String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/700559207820/cloudpro1";
		// final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		// SendMessageRequest sendMessageRequest = new SendMessageRequest(myQueueUrl,"-1");

  //       final SendMessageResult sendMessageResult = sqs
  //               .sendMessage(sendMessageRequest);
    }
}