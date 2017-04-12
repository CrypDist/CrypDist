import java.io.Serializable;
import java.security.Timestamp;
import java.util.Date;

/**
 * Created by Kaan on 19-Feb-17.
 */
public abstract class Transaction implements Serializable
{
    protected String filePath;
    protected String fileName;
    protected Date timeStamp;

    public String getFilePath()
    {
        return filePath;
    }
    public String getFileName()
    {
        return fileName;
    }

    public abstract void execute(ServerAccessor serverAccessor);
    public abstract String getStringFormat();
}
