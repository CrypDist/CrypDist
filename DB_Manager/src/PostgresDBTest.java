import static org.junit.Assert.*;

/**
 * Created by furkansahin on 18/02/2017.
 */
public class PostgresDBTest {
    private PostgresDB db = new PostgresDB("blockchain_test", "furkansahin", "", true);
    String hash_to_test = "00000000126748912643A126482:TEST";
    String new_hash_to_test = "00000000126748912643A12648:TEST2";
    String data_to_test = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan''IN kol geni\", \"nonce\": 123}";
    String data_expected = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan'IN kol geni\", \"nonce\": 123}";

    @org.junit.Before
    public void setUp() throws Exception {
        db = new PostgresDB("blockchain_test", "furkansahin", "", false);
        assertNotEquals(null, db);
    }

    @org.junit.Test
    public void addBlock() throws Exception {
        db.addBlock(hash_to_test, data_to_test);

        String result = db.getData(hash_to_test);

        assertEquals(data_expected,result);
    }

    @org.junit.Test
    public void deleteAllTable() throws Exception {
        db.deleteAllTable();

        String result = db.getAllData();

        assertEquals("", result);
    }

    @org.junit.Test
    public void newHashForBlock() throws Exception {
        addBlock();
        String res_old = db.getData(hash_to_test);

        db.newHashForBlock(hash_to_test, new_hash_to_test);

        String res_new_emp = db.getData(hash_to_test);
        String res_new = db.getData(new_hash_to_test);

        assertEquals("", res_new_emp);
        assertEquals(res_old, res_new);

        db.newHashForBlock(new_hash_to_test, hash_to_test);
    }

    @org.junit.Test
    public void updateData() throws Exception {

    }

    @org.junit.Test
    public void getData() throws Exception {

    }

}