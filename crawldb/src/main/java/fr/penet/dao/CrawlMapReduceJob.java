/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.Cleanup;
import lombok.Data;
import lombok.experimental.Builder;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Builder
@Data
@Log
public class CrawlMapReduceJob implements Serializable {
    int id;
    int runId;
    String appengineMRId;
    
    public int insert(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement insertLink = conn.prepareStatement(
                "INSERT INTO crawl.map_reduce_jobs (runid,appenginemrid) "
                + "VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertLink.setInt(1, runId);
        insertLink.setString(2, appengineMRId);
        insertLink.executeUpdate();
        
        @Cleanup
        ResultSet generatedKeys = insertLink.getGeneratedKeys();
        generatedKeys.next();
        id = generatedKeys.getInt(1);
        return id;
    }
    
    public void update(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement updateLink = conn.prepareStatement(
                "UPDATE crawl.map_reduce_jobs set runid=?, appenginemrid = ?"
                + " WHERE id = ?");
        updateLink.setInt(1, runId);
        updateLink.setString(2, appengineMRId);
        updateLink.setInt(3, id);
        updateLink.executeUpdate();
    }
    
    public void delete(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement deleteLink = conn.prepareStatement(
                "DELETE FROM crawl.map_reduce_jobs WHERE id = ?");
        deleteLink.setInt(1, id);
        deleteLink.execute();
    }
    
    public static CrawlMapReduceJob getById(Connection conn, int id) throws SQLException {
        @Cleanup
        PreparedStatement stmtGet = conn.prepareStatement("SELECT runid, appenginemrid FROM crawl.map_reduce_jobs WHERE id=?");
        stmtGet.setInt(1, id);
        @Cleanup
        ResultSet rsFetched = stmtGet.executeQuery();
        if(!rsFetched.next()) {
            return null;
        }
        return CrawlMapReduceJob.builder()
                .id(id)
                .runId(rsFetched.getInt(1))
                .appengineMRId(rsFetched.getString(2))
                .build();
    }
    
    public static List<CrawlMapReduceJob> getByRunId(Connection conn, int runid) throws SQLException {
        @Cleanup
        PreparedStatement stmtGet = conn.prepareStatement("SELECT id, appenginemrid FROM crawl.map_reduce_jobs WHERE runid=?");
        stmtGet.setInt(1, runid);
        List<CrawlMapReduceJob> ret = new ArrayList<>();
        @Cleanup
        ResultSet rsFetched = stmtGet.executeQuery();
        while(rsFetched.next()) {
            ret.add(CrawlMapReduceJob.builder()
                    .id(rsFetched.getInt(1))
                    .runId(runid)
                    .appengineMRId(rsFetched.getString(2))
                    .build());
        }
        return ret;
    }
    
}
