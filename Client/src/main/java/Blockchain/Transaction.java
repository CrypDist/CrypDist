package Blockchain;

import UploadUnit.ServerAccessor;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Transaction implements Comparable<Transaction>
{
    private static Logger log = BlockchainManager.log;

    private final String TIME_SERVER = "nist1-macon.macon.ga.us";
    private final String amazonServer = "https://s3.eu-central-1.amazonaws.com/";
    private final String bucketName = System.getenv("BUCKET_NAME");
    private String filePath;
    private String fileName;
    private Long timeStamp;
    private String dataSummary;
    private long dataSize;
    private URL url;

    public int compareTo(Transaction t) {
        if (t.getTimeStamp() > this.timeStamp)
            return -1;
        else if (this.timeStamp > t.getTimeStamp())
            return 1;
        else
            return 0;
    }
    public Transaction(String filePath, String fileName, String dataSummary,
                       long dataSize, URL url)
    {
        this.filePath = filePath;
        this.fileName = fileName;
        this.dataSummary = dataSummary;
        this.dataSize = dataSize;
        this.url = url;
        // TODO will be fixed

        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            log.info("Time:" + returnTime);
            Date time = new Date(returnTime);

            this.timeStamp = time.getTime();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void execute(ServerAccessor serverAccessor)
    {
        try {
            if (serverAccessor == null)
                log.info("HEYYYYYYYYYYYYYYYYYOOOOOOOOOOOOOOOOOOOOOOOOO");
            serverAccessor.upload(url, filePath, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStringFormat() {
        return amazonServer + bucketName + "/" + fileName;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getDataSummary()
    {
        return dataSummary;
    }

    public long getDataSize()
    {
        return dataSize;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

}
