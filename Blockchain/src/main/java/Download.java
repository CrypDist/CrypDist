

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Download extends Transaction
{
    private String data;

    public Download(String stringFormat)
    {
        this.stringFormat = stringFormat;
    }

    public String getData()
    {
        return data;
    }

    public void execute()
    {

    }

    public boolean validate()
    {
        // Transaction format: DOWNLOAD link source
        String[] parts = stringFormat.split(" ");
        if (parts.length != 3)
            return false;
        if (!parts[0].equals("DOWNLOAD"))
            return false;

        // TODO check if the source exists
        // TODO check if the link is valid
        return true;
    }
}
