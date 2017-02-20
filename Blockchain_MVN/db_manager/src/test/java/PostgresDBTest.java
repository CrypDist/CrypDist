
import static org.junit.Assert.*;

/**
 * Created by furkansahin on 18/02/2017.
 */
public class PostgresDBTest {
    private static PostgresDB db = new PostgresDB("blockchain_test", "furkansahin", "", true);
    String hash_to_test = "00000000126748912643A126482:TEST";
    String new_hash_to_test = "00000000126748912643A12648:TEST2";
    String data_to_test = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan''IN kol geni\", \"nonce\": 123}";
    String data_to_test_2 = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan''IN ayak geni\", \"nonce\": 123}";
    String data_expected = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan'IN kol geni\", \"nonce\": 123}";
    String data_expected_2 = "{\"prev_hash\": \"00000000126748912643A1_PREV:TEST\", \"description\": \"Furkan'IN ayak geni\", \"nonce\": 123}";

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        db = new PostgresDB("blockchain_test", "furkansahin", "", true);

        assertNotEquals(null, db);
    }

    @org.junit.Before
    public void setUp(){
        db.deleteAllTable();
        db.addBlock(hash_to_test, data_to_test);

        assertNotEquals(null, db);
    }

    @org.junit.Test
    public void addBlock() throws Exception {
        db.addBlock(new_hash_to_test, data_to_test_2);

        String result = db.getData(new_hash_to_test);

        assertEquals(data_expected_2,result);
        System.out.println("addBlock is done!");
    }

    @org.junit.Test
    public void deleteAllTable() throws Exception {
        db.deleteAllTable();

        String result = db.getAllData();

        assertEquals("", result);
        System.out.println("deleteAllTable is done!");
    }

    @org.junit.Test
    public void newHashForBlock() throws Exception {
        String res_old = db.getData(hash_to_test);

        db.newHashForBlock(hash_to_test, new_hash_to_test);

        String res_new_emp = db.getData(hash_to_test);
        String res_new = db.getData(new_hash_to_test);

        assertEquals("", res_new_emp);
        assertEquals(res_old, res_new);

        db.newHashForBlock(new_hash_to_test, hash_to_test);
        System.out.println("newHashForBlock is done!");
    }

    @org.junit.Test
    public void updateData() throws Exception {
        String res_old = db.getData(hash_to_test);

        db.updateData(hash_to_test, data_to_test_2);

        String res_new = db.getData(hash_to_test);

        assertEquals(data_expected, res_old);
        assertEquals(data_expected_2, res_new);
        System.out.println("updateData is done!");
    }

    @org.junit.Test
    public void getData() throws Exception {
        String res = db.getData(hash_to_test);

        assertEquals(res, data_expected);
    }

    @org.junit.Test
    public void getAllData() throws Exception {
        db = new PostgresDB("blockchain_test", "furkansahin", "", true);
        db.addBlock(hash_to_test, data_to_test);
        db.addBlock(new_hash_to_test, data_to_test_2);

        String res = db.getAllData();

        assertEquals(data_expected + data_expected_2, res);
    }

    @org.junit.Test
    public void getSize() throws Exception {
        int size_1 = db.getSize();

        db.addBlock(new_hash_to_test, data_to_test_2);

        int size_2 = db.getSize();

        assertEquals(1, size_1);
        assertEquals(size_1 + 1, size_2);
    }

    @org.junit.AfterClass
    public static void tearDown() {
        db = new PostgresDB("blockchain_test", "furkansahin", "", true);
        assertNotEquals(null, db);
    }

}