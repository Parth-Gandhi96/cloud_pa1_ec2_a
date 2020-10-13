package com.parth.carReck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
//import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import java.util.List;

public class carReck {

	public static void detectLabels(String photo) throws Exception{
		//String photo = "1.jpg";
      		String bucket = "njit-cs-643";
		String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/700559207820/cloudpro1";
		//System.out.println("Inside ");
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
		
		 final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

		 //System.out.println("Detected labels forihiiiiiiii " + photo);
		 for (Label label: labels) {
			 if(label.getName().equals("Car") && label.getConfidence() > 90){
			 	SendMessageRequest sendMessageRequest =
                    			new SendMessageRequest(myQueueUrl,
                           		photo.substring(0, photo.length()-4));
				//sendMessageRequest.setMessageGroupId("messageGroup1");
				final SendMessageResult sendMessageResult = sqs
                    			.sendMessage(sendMessageRequest);
				System.out.println("Car Detected!");		
				System.out.println("Sending " +photo+ " to second EC2!");
			 }
		    //System.out.println(label.getName() + ": " + label.getConfidence().toString());
			 }
		 System.out.println();
	      } catch(AmazonRekognitionException e) {
		 e.printStackTrace();
	      }
	}


    public static void main( String[] args)throws Exception{
    	//AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        //        .withRegion(Regions.US_EAST_1)
        //        .build();
	//
	//detectLabels();

	AmazonS3 s3 = new AmazonS3Client();
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(usWest2);
    	String BUCKETNAME = "njit-cs-643";
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(BUCKETNAME));
        for (S3ObjectSummary obj : objectListing.getObjectSummaries()) {	
            System.out.println(" - " + obj.getKey() + "  " +
                    "(size = " + obj.getSize() + ")");
		detectLabels(obj.getKey());
		//TimeUnit.SECONDS.sleep(10);		
        }

	System.out.println();
	System.out.println("Sending Final msg to queue");
	String myQueueUrl = "https://sqs.us-east-1.amazonaws.com/700559207820/cloudpro1";
	final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
	SendMessageRequest sendMessageRequest =
                                        new SendMessageRequest(myQueueUrl,
                                        "-1");
	//sendMessageRequest.setMessageGroupId("messageGroup1");
                                final SendMessageResult sendMessageResult = sqs
                                        .sendMessage(sendMessageRequest);
    }
}
