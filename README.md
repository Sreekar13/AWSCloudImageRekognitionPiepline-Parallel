# Setting up the AWS cloud environment

***To setup the resources:***
We need to launch two **Amazon linux 2 AMI** EC2 instances for hosting our code. 

We need to create a **SQS - FIFO type queue** using AWS console. I named it, **Rekognition-SQS.fifo**

***To setup the running environment on the EC2 instances:***
We need to install **Java** and **maven** for compiling and running the code.

To install them, use the following commands:

1) `sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo`

2) `sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo`

3) `sudo yum install -y apache-maven`

Now, check if they are installed correctly, 

    java -version
 The above command should display the java version.

    mvn --version
The above should display the maven version

All the above **should be done in both the EC2 instances**

***To setup the AWS SDK Keys in both the EC2 instances:***
We need AWS SDK Keys to grant access to the AWS resources from the code. These keys should be overwritten into **~/.aws/credentials** file in both the instances.

The keys look like below,
[default]
aws_access_key_id=**\<KEY_ID\>**
aws_secret_access_key=**\<SECRET_ACCESS_KEY\>**
aws_session_token=**\<SESSION_TOKEN\>**

Later, we need to copy the source code into EC2-instances,

**awspa1**  to be copied into instanceA
**awspa1-instanceb** to be copied into instanceB

***Running code in instance A:***

Log into instance A which has **awspa1**,

To change the SQS FIFO Queue URL, go to the file **'/home/ec2-user/awspa1/src/main/java/cloud/awspa1/App.java'** and change the value of the variable **SQSQueueUrl** in **line number 34**.

Ex: - 
String SQSQueueUrl="<FIFO_QUEUE_URL>";

Change directory to **/home/ec2-user/awspa1** and execute the below command,

    sh run.sh
*P.S - run.sh is a shell script* 

Please refer to images - **InstanceA-*** for demo execution.

**Running code in instance B:**
Log into instance B which has **awspa1-instanceb**,

To change the SQS FIFO Queue URL, go to the file **'/home/ec2-user/awspa1-instanceb/src/main/java/cloud/awspa1/App.java'** and change the value of the variable **SQSQueueUrl** in **line number 28**.

Ex: - 
String SQSQueueUrl="<FIFO_QUEUE_URL>";

Change directory to **/home/ec2-user/awspa1-instanceb** and execute the below command,

    sh run.sh
*P.S - run.sh is a shell script* 

Please refer to images - **InstanceB-*** for demo execution.

					**OR**

If you want to execute jars, 

Copy the folders InstanceAJar into InstanceA and InstanceBJar into InstanceB,

In **Instance A**, execute the jar present in InstanceAJar directory by below command,

**java -jar InstanceAJar/awspa1-0.0.1-SNAPSHOT-jar-with-dependencies.jar**

In **Instance B**, execute the jar present in InstanceBJar directory by below command,

**java -jar InstanceBJar/awspa1-0.0.1-SNAPSHOT-jar-with-dependencies.jar**

After successful execution, the file with the required data will be in **/home/ec2-user/output.txt** in instanceB's **ELB**.

Execute the below command **in InstanceB EC2-instance**

    cat /home/ec2-user/output.txt

The output will be like below,

1.jpg SBR8167

1.jpg SBR8167

4.jpg YHI9 OTZ

4.jpg YHI9

4.jpg OTZ

7.jpg LP 610 LB

7.jpg LP

7.jpg 610

7.jpg LB
