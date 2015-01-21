/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.db;

import com.google.appengine.api.utils.SystemProperty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
 public class DbUtils {
    
    private String getConnURL() throws ClassNotFoundException {
        // very basic example. copy/paste google doc, hard coding urls...
        String url = null;
        if (SystemProperty.environment.value() ==
            SystemProperty.Environment.Value.Production) {
          // Load the class that provides the new "jdbc:google:mysql://" prefix.
          Class.forName("com.mysql.jdbc.GoogleDriver");
          url = "jdbc:google:mysql://lpcrawlermapreduce:crawldbus";
        } else {
          // Local MySQL instance to use during development.
          Class.forName("com.mysql.jdbc.Driver");
          url = "jdbc:mysql://127.0.0.1:3306/crawl";

          // Alternatively, connect to a Google Cloud SQL instance using:
          // jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
        }
        url+="?jdbcCompliantTruncation=false";
        return url;
    }

    public Connection createConnection() {
        Connection conn = null;
        try {
            String connURL = getConnURL();
            conn = DriverManager.getConnection(connURL,"crawl", "crawl_2015");
        } catch (ClassNotFoundException | SQLException ex) {
            log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return conn;
    }
    
    public void closeConnection(Connection conn) {
        try {
            if ((conn == null) || conn.isClosed()) {
                return;
            }
            conn.close();
        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }
}