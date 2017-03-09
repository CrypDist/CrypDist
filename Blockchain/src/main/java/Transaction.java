import java.io.Serializable;

/**
 * Created by Kaan on 19-Feb-17.
 */
public abstract class Transaction implements Serializable
{
    protected String stringFormat;

    public String getStringFormat()
    {
        return stringFormat;
    }

    public abstract void execute();
    public abstract boolean validate();
}
