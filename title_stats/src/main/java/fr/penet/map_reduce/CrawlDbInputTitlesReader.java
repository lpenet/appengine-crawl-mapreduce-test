/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.InputReader;
import fr.penet.db.DbUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Cleanup;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
public class CrawlDbInputTitlesReader extends InputReader<String> {
    
    ResultSet shard;
    Connection conn;
    public CrawlDbInputTitlesReader(int runId, int shardCount, int shardIndex) throws SQLException {
        DbUtils dbUtils = new DbUtils();
        conn = dbUtils.createConnection();
        @Cleanup
        PreparedStatement shardStmt = conn.prepareStatement("SELECT title from crawl.pages p where runid=? and p.id % ? = ?");
        shardStmt.setInt(1, runId);
        shardStmt.setInt(2, shardCount);
        shardStmt.setInt(3, shardIndex);
        shard = shardStmt.executeQuery();
    }
    
    @Override
    public String next() throws IOException, NoSuchElementException {
        try {
            if(shard.next()) {
                return shard.getString(1);
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL error when querying shard", ex);
        }
        try {
            if( (conn != null) && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL error when closing connection", ex);
        }
        throw new NoSuchElementException();
    }
    
}
