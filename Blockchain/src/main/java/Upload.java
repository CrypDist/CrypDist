
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Timestamp;

import java.net.InetAddress;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Upload extends Transaction
{
    private final String TIME_SERVER = "nist1-macon.macon.ga.us";
    private final String amazonServer = "https://s3.eu-central-1.amazonaws.com/";
    private final String bucketName = System.getenv("BUCKET_NAME");

    public Upload(String filePath, String fileName)
    {
        this.filePath = filePath;
        this.fileName = fileName;
        // TODO will be fixed

        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            System.out.println(returnTime);
            Date time = new Date(returnTime);

            this.timeStamp = time;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void execute(ServerAccessor serverAccessor)
    {
        try {
            serverAccessor.upload(fileName, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getStringFormat() {
        return amazonServer + bucketName + "/" + fileName;
    }
}
