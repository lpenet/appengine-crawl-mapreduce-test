/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.InputReader;
import com.google.appengine.tools.mapreduce.impl.shardedjob.JobFailureException;
import fr.penet.dao.CrawlPage;
import fr.penet.db.DbUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
public class CrawlDbInputReader extends InputReader<CrawlPage> {
    transient DbUtils dbUtils = null;
    transient Connection conn = null;
    transient PreparedStatement shardStmt = null;
    transient ResultSet shard = null;
    int runId;
    
    @Override
    public void beginSlice() throws IOException {
        try {
            log.entering("CrawlDbInputTitlesReader", "beginSlice");
            dbUtils = new DbUtils();
            conn = dbUtils.createConnection();
            conn.setAutoCommit(false);
            shardStmt = CrawlPage.getShardPagesStmt(runId, getContext().getShardCount(), getContext().getShardNumber(), conn);
            shardStmt.setFetchSize(10);
            shard = shardStmt.executeQuery();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error initializing db reader for processing titles", ex);
            throw new JobFailureException("Error initializing db reader for processing titles : " + ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void endSlice() throws IOException {
        log.entering("CrawlDbInputTitlesReader", "endSlice");
        if(dbUtils == null) {
            // improbable, nothing to do.
            return;
        }
        try {
            // shard will automatically be closed when @PreparedStatement is closed
            if ( (shardStmt != null) && !shardStmt.isClosed()) {
                shardStmt.close();
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Error closing result set of slice", ex);
        }
        if(conn != null ) {
            dbUtils.closeConnection(conn);
        }
    }
    
    public CrawlDbInputReader(int runIdParam) {
        runId = runIdParam;
    }
    
    @Override
    public CrawlPage next() throws IOException, NoSuchElementException {
        log.entering("CrawlDbInputTitlesReader", "next");
        try {
            if(shard.next()) {
                return new CrawlPage(shard);
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL error while reading input for titles MR job", ex);
            throw new JobFailureException("SQL error while reading input for titles MR job : " + ex.getLocalizedMessage());
        }
        throw new NoSuchElementException();
    }
    
}
