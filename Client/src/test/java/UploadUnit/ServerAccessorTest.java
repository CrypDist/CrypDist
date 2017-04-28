package java.UploadUnit;

import UploadUnit.ServerAccessor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotEquals;

/**
 * Created by furkansahin on 08/04/2017.
 */

public class ServerAccessorTest {
    private static ServerAccessor serverAccessor;
    private final String fileName = "tester.txt";
    private static final String filePath = "src/test/resources/tester.txt";

  /*  @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        serverAccessor = new ServerAccessor();
        Path fileToDeletePath = Paths.get(filePath + "_returned");
        Files.deleteIfExists(fileToDeletePath);

        assertNotEquals(null, serverAccessor);
    }

    @Before
    public void uploadSample() throws Exception {
        if (serverAccessor.doesObjectExist(fileName))
            serverAccessor.delete(fileName);
        serverAccessor.upload(fileName, filePath);
    }

    @Test
    public void uploadTest() throws Exception {
        if (serverAccessor.doesObjectExist(fileName))
            serverAccessor.delete(fileName);

        serverAccessor.upload(fileName, filePath);

        assert serverAccessor.doesObjectExist(fileName);
    }

    @Test
    public void downloadTest() throws IOException {
        String return_path = filePath + "_returned";
        serverAccessor.download(fileName, return_path);

        File f = new File(return_path);
        assert f.exists() && !f.isDirectory();
    }
*/

}
