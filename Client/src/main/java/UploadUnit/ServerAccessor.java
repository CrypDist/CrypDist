package UploadUnit; /**
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
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;


public class ServerAccessor {

    private static Logger log = Logger.getLogger("UploadUnit");

    private String bucketName;
    private AmazonS3 s3client;

    public ServerAccessor(){
        bucketName = System.getenv("BUCKET_NAME");

        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.EU_CENTRAL_1).build();
    }

    public void upload(String fileName, String filePath) throws Exception {
        if (doesObjectExist(fileName)) {

            throw new Exception("file already exists!");
        }
        try {
            log.info("Uploading a new object to S3 from a file\n");
            log.warn("FILE_NAME_IN_UPLOAD:\t" + fileName);
            log.warn("FILE_PATH_IN_UPLOAD:\t" + filePath);

            File file = new File(filePath);
            s3client.putObject(new PutObjectRequest(bucketName, fileName, file));

        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }
    }

    public void download(String fileName, String toDirectory) throws IOException {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
        InputStream objectData = object.getObjectContent();
        byte[] buffer = IOUtils.toByteArray(objectData);

        FileUtils.writeByteArrayToFile(new File(toDirectory), buffer);

        try {
            objectData.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean doesObjectExist(String fileName)
    {
        System.out.println("BUCKET:" + bucketName);
        System.out.println("FILE:" + fileName);
        return s3client.doesObjectExist(bucketName, fileName);
    }

    public void delete(String fileName)
    {
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }
}