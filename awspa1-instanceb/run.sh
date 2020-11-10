#!/bin/sh
echo "===========================================Compiling the source code and generating the artifact==========================================="
mvn clean compile assembly:single
echo "===========================================Running the generated artifact to recieve indices from the SQS Queue and checking for the text in the images and producing a text file==========================================="
java -jar target/awspa1-0.0.1-SNAPSHOT-jar-with-dependencies.jar
echo "Please check the file /home/ec2-user/output.txt for the output"