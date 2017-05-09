import Util.Config;
import Util.CustomAppender;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by od on 27.04.2017.
 */
public class Property {
    public Property() {

        PropertyConfigurator.configure(this.getClass().getResourceAsStream("log4j_custom.properties"));
        Logger l1 = Logger.getLogger("P2P");
        Appender a = new CustomAppender();
        l1.addAppender(a);

        l1 = Logger.getLogger("BlockchainManager");
        l1.addAppender(a);

        l1 = Logger.getLogger("DbManager");
        l1.addAppender(a);

        l1 = Logger.getLogger("CrypDist");
        l1.addAppender(a);

        try {
            Config.PRIVATE_KEY = new String((IOUtils.toByteArray(getClass().getResourceAsStream("private.pem"))) );

        } catch (Exception e) {

        }
    }
}
