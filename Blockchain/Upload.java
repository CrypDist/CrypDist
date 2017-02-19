package BlockChain;

import java.io.File;

/**
 * Created by Kaan on 19-Feb-17.
 */
public class Upload extends Transaction
{
    public void execute()
    {

    }

    public boolean validate()
    {
        // Transaction format: UPLOAD filename link
        String parts[] = stringFormat.split(" ");
        if (parts.length != 3)
            return false;
        if (!parts[0].equals("UPLOAD"))
            return false;

        File file = new File(parts[1]);
        if (!file.exists())
            return false;

        // TODO check if the link is valid
        return true;
    }
}
