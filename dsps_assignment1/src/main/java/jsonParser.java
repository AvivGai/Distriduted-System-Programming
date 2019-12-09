import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

public class jsonParser {
    private static AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());

    public static void main(String[] args) throws Exception{
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("eu-central-1")
                .build();

        String bucketName =
                "aviv-and-eden-cool-bucket-inputfiles";
        String key=null;


        //System.out.println("Downloading an object");
        //S3Object object = s3.getObject(new GetObjectRequest(bucketName));
        //System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
        //displayTextInputStream(object.getObjectContent());

        /*
         * List objects in your bucket by prefix - There are many options for
         * listing the objects in your bucket.  Keep in mind that buckets with
         * many objects might truncate their results when listing their objects,
         * so be sure to check if the returned object listing is truncated, and
         * use the AmazonS3.listNextBatchOfObjects(...) operation to retrieve
         * additional results.
         */
        System.out.println("Listing objects");
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("My"));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
        }
        System.out.println();
    }

    public static List<Review> ParseJasonToReviewsPlease(String filePath){
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        try {
            Object obj = parser.parse(new FileReader(filePath));
            JsonArray jsonArray = (JsonArray) obj;
            TitleReviews[] reviews = gson.fromJson(jsonArray, TitleReviews[].class); // creating the reviews array

            List<Review> reviewsList = new LinkedList<Review>();
            for(TitleReviews t:reviews){
                for(Review r:t.getReviews()){
                    reviewsList.add(r);
                }
            }

            return reviewsList;

        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
