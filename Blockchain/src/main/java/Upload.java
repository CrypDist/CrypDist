
import java.io.File;

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Upload extends Transaction
{
    public Upload(String stringFormat)
    {
        this.stringFormat = stringFormat;
    }

    public void execute()
    {

    }

    public boolean validate()
    {
        // Transaction format: UPLOAD filename source link
        String parts[] = stringFormat.split(" ");
        if (parts.length != 4)
            return false;
        if (!parts[0].equals("UPLOAD"))
            return false;

        File file = new File(parts[1]);
        if (!file.exists())
            return false;

        // TODO check if the source exists
        // TODO check if the link is valid
        return true;
    }
}
