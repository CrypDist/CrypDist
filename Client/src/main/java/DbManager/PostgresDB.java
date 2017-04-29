package DbManager;

import Util.Config;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by furkansahin on 15/02/2017.
 */
public class PostgresDB {

    private static org.apache.log4j.Logger log = Logger.getLogger("DbManager");

    Connection conn;
    final String TABLE_NAME = Config.DB_TABLE_NAME;
    public PostgresDB(String dbName, String user, String secret, boolean reset)
    {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.fatal("Postgres driver couldn't be reached. Download the jar file and link it to the project");
            log.trace(e);
        }

        try {
            setupDB(dbName, user, secret, reset);
        } catch (SQLException e) {
            log.fatal("Postgres could not setup the desired database.");
            log.trace(e);
        }


    }

    private void setupDB(String dbName, String user, String secret, boolean reset) throws SQLException {
        String url = "jdbc:postgresql://localhost/";

        try {
            conn = DriverManager.getConnection(url, user, secret);
        } catch (SQLException e) {
            log.fatal("DB could not be created, there is a possible problem related to properties.");
            log.trace(e);
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


        query = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (blockchain JSON);";

        st.executeUpdate(query);

        if (reset)
            deleteAllTable();

        st.close();
    }

    public void deleteAllTable()
    {
        String query = "DROP TABLE if exists " + TABLE_NAME + " CASCADE;";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
            st.executeUpdate();
            query = "CREATE TABLE " + TABLE_NAME + " (blockchain JSON);";
            st = conn.prepareStatement(query);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBlockchain(String blockchain)
    {
        deleteAllTable();
        String query = "Insert into " + TABLE_NAME + " (blockchain) VALUES (to_json(?::json))";
        PreparedStatement st = null;

        try {
            st = conn.prepareStatement(query);
            st.setString(1, blockchain);
        }catch (SQLException e) {
            e.printStackTrace();
        }
        executeQuery(st);
    }

    public String getBlockchain()
    {
        String query = "SELECT blockchain FROM " + TABLE_NAME + ";";
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataFetcher(st);
    }

    private String dataFetcher(PreparedStatement st)
    {
        try {
            ResultSet rs = st.executeQuery();
            StringBuilder result = new StringBuilder();

            while (rs.next()) {
                result.append(rs.getString(1));
                break;
            }

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
}
