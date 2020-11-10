#!/bin/sh
echo "===========================================Compiling the source code and generating the artifact==========================================="
mvn clean compile assembly:single
echo "===========================================Running the generated artifact to input the indices that has car in the pictures from S3 into SQS ==========================================="
java -jar target/awspa1-0.0.1-SNAPSHOT-jar-with-dependencies.jar
