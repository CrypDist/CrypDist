/**
 * Created by furkansahin on 05/04/2017.
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.FileUtils;

import java.io.*;


public class ServerAccessor {
    private String bucketName;
    private AmazonS3 s3client;

    public ServerAccessor(){
        bucketName = System.getenv("BUCKET_NAME");

        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.EU_CENTRAL_1).build();
    }

    public void upload(String fileName, String filePath) throws Exception {
        if (doesObjectExist(fileName))
            throw new Exception("file already exists!");

        try {
            System.out.println("Uploading a new object to S3 from a file\n");
            File file = new File(filePath);
            s3client.putObject(new PutObjectRequest(
                    bucketName, fileName, file));

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public void download(String fileName, String toDirectory) throws IOException {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
        InputStream objectData = object.getObjectContent();

        byte[] buffer = new byte[(int) object.getObjectMetadata().getContentLength()];
        objectData.read(buffer);

        FileUtils.writeByteArrayToFile(new File(toDirectory), buffer);

        try {
            objectData.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean doesObjectExist(String fileName)
    {
        return s3client.doesObjectExist(bucketName, fileName);
    }

    void delete(String fileName)
    {
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }
}