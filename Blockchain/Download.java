package BlockChain;

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Download extends Transaction
{
    private String data;

    public String getData()
    {
        return data;
    }

    public void execute()
    {

    }

    public boolean validate()
    {
        // Transaction format: DOWNLOAD link
        String[] parts = stringFormat.split(" ");
        if (parts.length != 2)
            return false;
        if (!parts[0].equals("DOWNLOAD"))
            return false;

        // TODO check if the link is valid
        return true;
    }
}
