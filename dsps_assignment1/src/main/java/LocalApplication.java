import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.apache.commons.codec.binary.Base64;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

public class LocalApplication {

    private static final String MANAGER_FILE = "";
    private static final String WORKER_FILE = "";
    private static final String RESULT_FILE = "" + ".html";
    private static final String LocalToManagerQueue = "LocalToManager";
    private static final String ManagerToLocalQueue = "ManagerToLocal";
    private static String URL_FILE_NAME = "";
    public static String managerInstanceId;
    public static String keyPairName;
    public static String bucketName = "aviv-and-eden-cool-bucket";
    private static AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());

    public static void main(String[] args) throws Exception {

        String filename = "inputfiles";
        AmazonEC2 ec2 = initializeManager();
        AmazonS3 s3 = initializeS3();
        uploadFile(filename, s3);
        AmazonSQS sqs = InitializeSQS();
    }


    private static AmazonEC2 initializeManager() {
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();
        try {
            RunInstancesRequest request = new RunInstancesRequest("ami-00068cd7555f543d5", 1, 1);
            request.setInstanceType(InstanceType.T1Micro.toString());
            List<Instance> instances = ec2.runInstances(request).getReservation().getInstances();
            System.out.println("Launch instances: " + instances);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
        /*try {
            System.out.println("Starting the Manager instance.\n");
            //check if manager already running
            DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();
            boolean done = false;
            boolean managerRunning = false;
            while (!done && !managerRunning) {
                DescribeInstancesResult response = ec2.describeInstances(describeRequest);
                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {
                        if (instance.getPublicIpAddress() != null) {
                            for (Tag tag : instance.getTags()) {
                                if (tag.getValue().compareTo("MANAGER") == 0) {
                                    managerRunning = true;
                                    managerInstanceId = instance.getInstanceId();
                                }
                            }
                        }
                    }
                }
                describeRequest.setNextToken(response.getNextToken());
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
            //if manager is not active yet
            if (!managerRunning) {
                Tag tag = new Tag().withKey("MANAGER").withValue("MANAGER");
                RunInstancesRequest request = new RunInstancesRequest("ami-00068cd7555f543d5", 1, 1)
                        .withInstanceType(InstanceType.T1Micro.toString())
                        .withKeyName(keyPairName)
                        .withIamInstanceProfile(new IamInstanceProfileSpecification().withName("EC2"))
                        .withTagSpecifications(new TagSpecification().withResourceType(ResourceType.Instance).withTags(tag))
                        .withUserData(getManagerUserData());
                List<Instance> instances = ec2.runInstances(request).getReservation().getInstances();
                managerInstanceId = instances.get(0).getInstanceId();
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }*/
        return ec2;
    }


    public static String getManagerUserData() {
        String output = "#!/bin/bash" + "\n" +
                "# Installing Java" + "\n" +
                "sudo yum -y install java-1.8.0-openjdk.x86_64" + "\n" +
                "# Download Manager.jar from Our S3 Bucket" + "\n" +
                "wget https://" + bucketName + ".s3.amazonaws.com/" + MANAGER_FILE + " -O ./" + MANAGER_FILE + "\n" +
                "java8 -jar ./" + MANAGER_FILE + " " + LocalToManagerQueue + "\n";
        return new String(Base64.encodeBase64(output.getBytes()));
    }


    private static AmazonS3 initializeS3() {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("eu-central-1")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon S3");
        System.out.println("===========================================\n");
        try {
            System.out.println("Creating bucket " + bucketName + "\n");
            s3.createBucket(bucketName);
            /*  List the buckets in your account  */
            System.out.println("Listing buckets");
            for (Bucket bucket : s3.listBuckets()) {
                System.out.println(" - " + bucket.getName());
            }
            System.out.println();
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return s3;
    }


    private static void uploadFile(String fileName, AmazonS3 s3){
            System.out.println("Uploading a new object to S3 from a file\n");
            String key = null;
            File dir = new File(fileName);
            for (File file : dir.listFiles()) {
                key = file.getName().replace('\\', '_').replace('/', '_').replace(':', '_');
                PutObjectRequest req = new PutObjectRequest(bucketName, key, file);
                s3.putObject(req);
            }
    }


    private static AmazonSQS InitializeSQS() {
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-west-2")
                .build();
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        try {
            // Create a queue
            System.out.println("Creating a new SQS queue called MyQueue.\n");
            CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue"+ UUID.randomUUID());
            String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
            // List queues
            System.out.println("Listing all queues in your account.\n");
            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
                System.out.println("  QueueUrl: " + queueUrl);
            }
            System.out.println();
            // Send a message
            System.out.println("Sending a message to MyQueue.\n");
            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text."));
            // Receive messages
            System.out.println("Receiving messages from MyQueue.\n");
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (Message message : messages) {
                System.out.println("  Message");
                System.out.println("    MessageId:     " + message.getMessageId());
                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
                System.out.println("    Body:          " + message.getBody());
                for (Entry<String, String> entry : message.getAttributes().entrySet()) {
                    System.out.println("  Attribute");
                    System.out.println("    Name:  " + entry.getKey());
                    System.out.println("    Value: " + entry.getValue());
                }
            }
            System.out.println();
            // Delete a message
            System.out.println("Deleting a message.\n");
            String messageRecieptHandle = messages.get(0).getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
            // Delete a queue
            System.out.println("Deleting the test queue.\n");
            sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return sqs;
    }
}




