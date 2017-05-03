import Util.Config;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by od on 27.04.2017.
 */
public class Property {
    public Property() {

        PropertyConfigurator.configure(this.getClass().getResourceAsStream("log4j_custom.properties"));

        try {
            Config.PRIVATE_KEY = new String((IOUtils.toByteArray(getClass().getResourceAsStream("private.pem"))) );
        } catch (Exception e) {

        }
    }
}
