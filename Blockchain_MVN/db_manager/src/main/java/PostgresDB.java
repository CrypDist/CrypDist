
import java.sql.*;

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
            conn.close();

            url += dbName;
            conn = DriverManager.getConnection(url, user, secret);
            deleteAllTable();
        }
        else if (!exists)
        {
            query = "CREATE DATABASE " + dbName;
            st.executeUpdate(query);
            conn.close();
            url += dbName;
            conn = DriverManager.getConnection(url, user, secret);
        }
        st.close();

        st = conn.createStatement();


        query = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (hash CHAR(32) UNIQUE PRIMARY KEY NOT NULL, data TEXT);";

        st.executeUpdate(query);

        if (reset)
            deleteAllTable();

        st.close();
        //System.out.println("blocks table is created!");
    }

    public boolean addBlock(String hash, String data)
    {
        String query = "INSERT INTO " + TABLE_NAME  + " VALUES(\'" + hash + "\', \'" + data + "\');";
        Statement st = null;
        
        try {
            st = conn.createStatement();
            st.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deleteAllTable()
    {
        String query = "DROP TABLE " + TABLE_NAME;
        boolean result = executeQuery(query);
        if (result)
        {
            query = "CREATE TABLE "+ TABLE_NAME + " (hash CHAR(32) UNIQUE PRIMARY KEY NOT NULL, data TEXT);";
            return executeQuery(query);
        }
        return false;
    }

    public boolean newHashForBlock(String oldHash, String newHash)
    {
        String query = "UPDATE " + TABLE_NAME + " SET hash=\'" + newHash + "\' WHERE hash=\'" + oldHash + "\';";
        return executeQuery(query);
    }

    public boolean updateData(String hash, String data)
    {
        String query = "UPDATE " + TABLE_NAME + " SET data=\'" + data + "\' WHERE hash=\'" + hash + "\'";
        return executeQuery(query);
    }

    public String getData(String hash)
    {
        String query = "SELECT data FROM " + TABLE_NAME + " WHERE hash=\'" + hash + "\';";

        return dataFetcher(query);
    }

    public String getAllData()
    {
        String query = "SELECT data FROM " + TABLE_NAME + ";";

        return dataFetcher(query);
    }
    public int getSize()
    {
        String query = "SELECT count(*) FROM " + TABLE_NAME + ";";

        String output = dataFetcher(query);

        return Integer.parseInt(output);
    }

    private String dataFetcher(String query)
    {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            StringBuilder result = new StringBuilder();

            while (rs.next())
                result.append(rs.getString(1));

            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    private boolean executeQuery(String query)
    {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.executeUpdate(query);
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
