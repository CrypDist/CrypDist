/**
 * Created by od on 3.05.2017.
 */
public class Config {

    static int SERVER_PORT = 4141;
    static String KEY_SPLITTER = "////";

    static int HEARTBEAT_FLAG_SERVER = 100;
    static int HEARTBEAT_ACK = 102;
    static int HEARTBEAT_MAX_TRIALS = 3;
    static int HEARTBEAT_PERIOD = 5000;
    static int HEARTBEAT_TIMEOUT = 1000;
    static int HEARTBEAT_DELAY = 1000;

    static int MESSAGE_ACK = 900;
    static int MESSAGE_TEST_TRIALS = 2;
    static int MESSAGE_SERVER_TEST = 999;
    static int SERVER_TEST_TIMEOUT = 2500;
}