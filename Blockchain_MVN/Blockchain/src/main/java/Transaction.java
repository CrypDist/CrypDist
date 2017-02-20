package BlockChain;

/**
 * Created by Kaan on 19-Feb-17.
 */
public abstract class Transaction
{
    protected String stringFormat;

    public String getStringFormat()
    {
        return stringFormat;
    }

    public abstract void execute();
    public abstract boolean validate();
}
