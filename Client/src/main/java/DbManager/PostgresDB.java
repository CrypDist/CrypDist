package DbManager;

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
            e.printStackTrace();
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


        query = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (hash TEXT UNIQUE PRIMARY KEY NOT NULL, data TEXT);";

        st.executeUpdate(query);

        if (reset)
            deleteAllTable();

        st.close();
        //System.out.println("blocks table is created!");
    }

    public boolean addBlock(String hash, String data)
    {
        String query = "INSERT INTO " + TABLE_NAME  + " VALUES(?, ?);";
        PreparedStatement st = null;
        
        try {
            st = conn.prepareStatement(query);
            st.setString(1, processText(hash));
            st.setString(2, processText(data));
            st.executeUpdate();
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

    public void deleteAllTable()
    {
        String query = "DROP TABLE if exists " + TABLE_NAME + " CASCADE;";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
            st.executeUpdate();
            query = "CREATE TABLE " + TABLE_NAME + " (hash TEXT UNIQUE PRIMARY KEY NOT NULL, data TEXT);";
            st = conn.prepareStatement(query);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean newHashForBlock(String oldHash, String newHash)
    {
        String query = "UPDATE " + TABLE_NAME + " SET hash=? WHERE hash=?;";

        return executeQuery(prepareStatement(query,newHash, oldHash));    }

    public boolean updateData(String hash, String data)
    {
        String query = "UPDATE " + TABLE_NAME + " SET data=? WHERE hash=?;";

        return executeQuery(prepareStatement(query,data, hash));
    }

    private PreparedStatement prepareStatement(String query, String firstData, String secondData)
    {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
            st.setString(1,processText(firstData));
            st.setString(2, processText(secondData));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return st;
    }
    public String getData(String hash)
    {
        String query = "SELECT data FROM " + TABLE_NAME + " WHERE hash=?;";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
            st.setString(1, processText(hash));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataFetcher(st);
    }

    public String getAllData()
    {
        String query = "SELECT data FROM " + TABLE_NAME + ";";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataFetcher(st);
    }
    public int getSize()
    {
        String query = "SELECT count(*) FROM " + TABLE_NAME + ";";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String output = dataFetcher(st);

        return Integer.parseInt(output);
    }

    private String dataFetcher(PreparedStatement st)
    {
        try {
            ResultSet rs = st.executeQuery();
            StringBuilder result = new StringBuilder();

            while (rs.next())
                result.append(rs.getString(1));

            return processTextReversed(result.toString());
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
    private boolean executeQuery(PreparedStatement st)
    {
        try {
            st.executeUpdate();
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

    private String processText(String text)
    {
        return text.replaceAll("\0", "NONCHAR");
    }
    private String processTextReversed(String text)
    {
        return text.replaceAll("NONCHAR", "\0");
    }
}
