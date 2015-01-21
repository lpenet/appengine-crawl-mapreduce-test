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
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
public class CrawlDbInputTitlesReader extends InputReader<String> {
    int runId;
    int shardCount;
    int shardIndex;
    int minId = -1;
    
    public CrawlDbInputTitlesReader(int runIdParam, int shardCountParam, int shardIndexParam) {
        runId = runIdParam;
        shardCount = shardCountParam;
        shardIndex = shardIndexParam;
    }
    
    @Override
    public String next() throws IOException, NoSuchElementException {
        try {
            DbUtils dbUtils = new DbUtils();
            @Cleanup
            Connection conn = dbUtils.createConnection();
            @Cleanup
            PreparedStatement shardStmt = conn.prepareStatement("SELECT id, title from crawl.pages p where runid=? and p.id % ? = ? and p.id > ?");
            shardStmt.setInt(1, runId);
            shardStmt.setInt(2, shardCount);
            shardStmt.setInt(3, shardIndex);
            shardStmt.setInt(4, minId);
            @Cleanup
            ResultSet shard = shardStmt.executeQuery();
            if(shard.next()) {
                minId = shard.getInt(1);
                return shard.getString(2);
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL error while reading input for titles MR job", ex);
        }
        throw new NoSuchElementException();
    }
    
}
