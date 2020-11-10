package cloud.awspa1;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class App {

	public static void main(String[] args) {
		//Bucket name
        String bucketName = "njit-cs-643";
        
        //SQS URL
        String SQSQueueUrl="https://sqs.us-east-1.amazonaws.com/<ID>/Rekognition-SQS.fifo";
        
        String content="";
        
        try {
        	//Rekognition Connection Client
        	AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
    				.withRegion("us-east-1")
    				.build();
        	
        	//SQS Connection Client
        	AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();
        	
        	ReceiveMessageRequest receive_request = new ReceiveMessageRequest(SQSQueueUrl).withMaxNumberOfMessages(10);
        	boolean flag=true;
        	
        	while(flag) {
        		final List<Message> messages = sqs.receiveMessage(receive_request).getMessages();
        	    for (Message messageObject : messages) {
        	        String message = messageObject.getBody();
        	        if(!message.equals("-1")) {
        	        	System.out.println("Received message: " + message);
        	        DetectTextRequest request = new DetectTextRequest()
        	                .withImage(new Image()
        	                .withS3Object(new S3Object()
        	                .withName(message)
        	                .withBucket(bucketName)));
        	            DetectTextResult result = rekognitionClient.detectText(request);
        	            List<TextDetection> textDetections = result.getTextDetections();

        	            System.out.println("Detected lines and words for " + message);
        	            for (TextDetection text: textDetections) {
        	            		if(!text.getDetectedText().isEmpty()) {
        	            			System.out.println("Image: "+ message+" "+text.getDetectedText());
        	            			content+=message+" "+text.getDetectedText()+"\n";
        	            		}
        	            }
        	        }
        	        else {
        	        	sqs.deleteMessage(SQSQueueUrl,messageObject.getReceiptHandle());
        	        	flag=false;
        	        	break;
        	        }
        	        sqs.deleteMessage(SQSQueueUrl,messageObject.getReceiptHandle());
        	    }
            }
        }
        catch (AmazonServiceException e) {
        	e.printStackTrace();
        } catch (SdkClientException e) {
        	e.printStackTrace();
        }
        
        try {
            File myObj = new File("/home/ec2-user/output.txt");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
            FileWriter myWriter = new FileWriter("/home/ec2-user/output.txt");
            myWriter.write(content);
            myWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
	}

}
