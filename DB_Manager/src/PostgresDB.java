import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.Properties;

/**
 * Created by furkansahin on 15/02/2017.
 */
public class PostgresDB {

    Connection conn;
    final String TABLE_NAME = "blocks";
    public PostgresDB(String dbName, String user, String secret, boolean reset)
    {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Postgres driver couldn't be reached. Download the jar file and link it to the project");
        }

        try {
            setupDB(dbName, user, secret, reset);
        } catch (SQLException e) {
            System.out.println("Postgres could not setup the desired database.");
        }


    }

    private void setupDB(String dbName, String user, String secret, boolean reset) throws SQLException {
        String url = "jdbc:postgresql://localhost/";

        try {
            conn = DriverManager.getConnection(url, user, secret);
        } catch (SQLException e) {
            System.out.println("DB could not be created, there is a possible problem related to properties");
        }

        String query = "SELECT datname FROM pg_database WHERE datname=\'" + dbName + "\'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        boolean exists = false;
        while (rs.next())
            exists = true;
        rs.close();

        // Drop the db if it exists and reset is desired
        if (exists && reset)
        {
            query = "DROP DATABASE " + dbName;
            st.executeUpdate(query);
        }

        // create the db if dropped or not even existed
        if (!exists || reset) {
            query = "CREATE DATABASE " + dbName;
            st.executeUpdate(query);
        }
        st.close();
        // close connectio to server
        conn.close();

        // connect directly to the db
        url += dbName;
        conn = DriverManager.getConnection(url, user, secret);
        st = conn.createStatement();

        query = "CREATE TABLE "+ TABLE_NAME + " (hash CHAR(32), data TEXT);";
        st.executeUpdate(query);

        System.out.println("blocks table is created!");
    }

    public boolean addBlock(String hash, String data)
    {
        
    }
}
