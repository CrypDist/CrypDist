package UploadUnit; /**
 * Created by furkansahin on 05/04/2017.
 */

import Util.Config;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ServerAccessor {

    private static Logger log = Logger.getLogger("UploadUnit");

    private String bucketName = Config.UPLOAD_BUCKETNAME;
    private AmazonS3 s3client;

    public ServerAccessor(){

        s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.EU_CENTRAL_1).build();
    }

    public void upload(URL url, String filePath, String fileName) throws Exception {
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

        GetObjectRequest request = new GetObjectRequest(bucketName, fileName);
        S3Object object = s3client.getObject(request);
        int size = (int)object.getObjectMetadata().getContentLength();
        S3ObjectInputStream objectContent = object.getObjectContent();
        FileOutputStream fos = new FileOutputStream(toDirectory);

        byte[] buffer = new byte[size];

        int buf = 0;
        while((buf = objectContent.read(buffer)) > 0)
        {
            fos.write(buffer, 0, buf);
        }
        fos.close();
    }

    public boolean doesObjectExist(String fileName)
    {
        log.info("BUCKET:" + bucketName);
        log.info("FILE:" + fileName);
        return s3client.doesObjectExist(bucketName, fileName);
    }

    public void delete(String fileName)
    {
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    }
    public void UploadObject(URL url, String filePath) throws IOException
    {
        HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream());
        out.write(new String(Files.readAllBytes(Paths.get(filePath))));
        out.close();
        int responseCode = connection.getResponseCode();
        log.info("Service returned response code " + responseCode);
    }

    public URL getURL(String fileName)
    {
        log.debug("Generating pre-signed URL.");
        java.util.Date expiration = new java.util.Date();
        long milliSeconds = expiration.getTime();
        milliSeconds += Config.UPLOAD_EXPIRATION_TIME; // Add 10 sec.
        expiration.setTime(milliSeconds);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileName);
        generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
        generatePresignedUrlRequest.setExpiration(expiration);

        return s3client.generatePresignedUrl(generatePresignedUrlRequest);
    }
}