package Util;

/**
 * Created by od on 27.04.2017.
 */
public class Config {

    public static int MESSAGE_OUTGOING = 200;
    public static int MESSAGE_OUTGOING_RESPONSE = 201;
    public static int MESSAGE_MAX_TRIALS = 4;
    public static int MESSAGE_ACK = 900;
    public static int MESSAGE_REQUEST_KEYSET = 301;
    public static int MESSAGE_REQUEST_BLOCK = 302;

    public static int UPLOAD_EXPIRATION_TIME = 10000;
    public static int BLOCKCHAIN_BATCH_TIMEOUT = 10000;
    public static int BLOCKCHAIN_BATCH_PERIOD = 8000;
    public static int TRANSACTION_VALIDATION_TIMEOUT = 1000;

    public static String DB_TABLE_NAME;

    public static String SERVER_ADDRESS = "207.154.219.184";
    public static int SERVER_PORT = 4141;
    public static int SERVER_TIMEOUT = 3000;

    public static int CLIENT_HEARTBEAT_PORT = 4141;
    public static int CLIENT_SERVER_PORT = 4142;
    public static String CLIENT_MESSAGE_SPLITTER = "////";
    public static String CLIENT_MESSAGE_PEERSIZE = "X";

    public static int HEARTBEAT_FLAG_CLIENT = 101;
    public static int HEARTBEAT_FLAG_SERVER = 100;
    public static int HEARTBEAT_ACK = 102;
    public static int HEARTBEAT_PERIOD = 5000;
    public static int HEARTBEAT_TIMEOUT = 10000;
    public static int HEARTBEAT_MAX_TRIALS = 3;

    public static int FLAG_BROADCAST_TRANSACTION = 1;
    public static int FLAG_BROADCAST_HASH = 2;
    public static int FLAG_TRANSACTION_VALIDATION = 3;
    public static int FLAG_BLOCKCHAIN_INVALID = 4;

    public static String UPLOAD_BUCKETNAME = "crypdist-trial-bucket-mfs";


    public static String MESSAGE_BROADCAST_SPLITTER = "%%%%";

}
