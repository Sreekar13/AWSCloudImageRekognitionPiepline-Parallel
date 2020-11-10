package cloud.awspa1;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;




public class App {
	
	static int count=0;

	public static void main(String[] args) {
        String bucketName = "njit-cs-643";   
        String SQSQueueUrl="https://sqs.us-east-1.amazonaws.com/<ID>/Rekognition-SQS.fifo";

        try {
    		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion("us-east-1")
                    .build();
    		
    		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
    				.withRegion("us-east-1")
    				.build();
    		
    		AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();

            System.out.println("Listing objects");

            ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName);
            ListObjectsV2Result result;

            do {
                result = s3Client.listObjectsV2(req);

                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    if(objectSummary.getKey().contains(".jpg")) {
                    //System.out.printf(" - %s\n", objectSummary.getKey());	
            		DetectLabelsRequest request = new DetectLabelsRequest()
         		           .withImage(new Image()
         		           .withS3Object(new S3Object()
         		           .withName(objectSummary.getKey()).withBucket(bucketName)))
         		           .withMaxLabels(10)
         		           .withMinConfidence(75F);
            	      try {
            	          DetectLabelsResult res = rekognitionClient.detectLabels(request);
            	          List <Label> labels = res.getLabels();
            	          for (Label label: labels) {
            	        	  if(label.getName().equals("Car") && label.getConfidence()>90) {
            	             //System.out.println(label.getName() + ": " + label.getConfidence());
            	        	 System.out.println("Image name: "+objectSummary.getKey()+" "+label.getName()+", has the confidence of car: "+label.getConfidence());
            	             count=count+1;  
            	             SendMessageRequest send_msg_request = new SendMessageRequest()
            	            	        .withQueueUrl(SQSQueueUrl)
            	            	        .withMessageBody(objectSummary.getKey())
            	            	        .withMessageGroupId("Rekognition");
            	             sqs.sendMessage(send_msg_request);

            	        	  }
            	          }
            	       } catch(AmazonRekognitionException e) {
            	          e.printStackTrace();
            	       }
                    }
                    
                }
	             SendMessageRequest send_msg_request = new SendMessageRequest()
	            	        .withQueueUrl(SQSQueueUrl)
	            	        .withMessageBody("-1")
	            	        .withMessageGroupId("Rekognition");
	             sqs.sendMessage(send_msg_request);
                System.out.println("Total number of pics with cars: "+count);
            } while (result.isTruncated());
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

}
