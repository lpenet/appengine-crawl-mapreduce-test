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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Data
@Log
public class CrawlRun implements Serializable {
    int id;
    String seed;
    Timestamp start;
    Timestamp end;
    
    public int getPageCount(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement prepareStatement = conn.prepareStatement("select count(p.id) from crawl.pages p where p.runid = ? ");
        prepareStatement.setInt(1,id);
        prepareStatement.execute();
        @Cleanup
        ResultSet resultSet = prepareStatement.getResultSet();
        if(!resultSet.next()) {
            return 0;
        }
        return resultSet.getInt(1);
    }
    
    public int getPagesProcessed(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement prepareStatement = conn.prepareStatement("select count(p.id) from crawl.pages p where p.runid = ? and p.status is not null and p.status <> " + CrawlPage.STATUS_IN_PROCESS);
        prepareStatement.setInt(1,id);
        prepareStatement.execute();
        @Cleanup
        ResultSet resultSet = prepareStatement.getResultSet();
        if(!resultSet.next()) {
            return 0;
        }
        return resultSet.getInt(1);
    }
    
    public int getPagesInProcess(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement prepareStatement = conn.prepareStatement("select count(p.id) from crawl.pages p where p.runid = ? and p.status = " + CrawlPage.STATUS_IN_PROCESS);
        prepareStatement.setInt(1,id);
        prepareStatement.execute();
        @Cleanup
        ResultSet resultSet = prepareStatement.getResultSet();
        if(!resultSet.next()) {
            return 0;
        }
        return resultSet.getInt(1);
    }
    
    public int getPagesToProcess(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement prepareStatement = conn.prepareStatement("select count(p.id) from crawl.pages p where p.runid = ? and p.status is null");
        prepareStatement.setInt(1,id);
        prepareStatement.execute();
        @Cleanup
        ResultSet resultSet = prepareStatement.getResultSet();
        if(!resultSet.next()) {
            return 0;
        }
        return resultSet.getInt(1);
    }
    
    public static List<CrawlRun> getAllRuns(Connection conn) {
        try {
            List<CrawlRun> listRuns = new ArrayList<>();
            @Cleanup
            PreparedStatement prepareStatement = conn.prepareStatement("select r.id, r.seed, r.start, r.end from crawl.runs r order by id;");
            prepareStatement.execute();
            @Cleanup
            ResultSet resultSet = prepareStatement.getResultSet();
            while(resultSet.next()) {
                CrawlRun run = new CrawlRun();
                run.setId(resultSet.getInt(1));
                run.setSeed(resultSet.getString(2));
                run.setStart(resultSet.getTimestamp(3));
                run.setEnd(resultSet.getTimestamp(4));
                listRuns.add(run);
            }
            return listRuns;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,ex.getLocalizedMessage(),ex);
            return null;
        }
    }
    
    public static CrawlRun getRunById(Connection conn, int id) {
        try {
            @Cleanup
            PreparedStatement prepareStatement = conn.prepareStatement("select r.seed, r.start, r.end from crawl.runs r where r.id = ?;");
            prepareStatement.setInt(1, id);
            prepareStatement.execute();
            @Cleanup
            ResultSet resultSet = prepareStatement.getResultSet();
            if(!resultSet.next()) {
                return null;
            }
            CrawlRun run = new CrawlRun();
            run.setId(id);
            run.setSeed(resultSet.getString(1));
            run.setStart(resultSet.getTimestamp(2));
            run.setEnd(resultSet.getTimestamp(3));
            if(resultSet.next()) {
                log.log(Level.WARNING, "Non unique run with id " + id);
            }
            return run;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,ex.getLocalizedMessage(),ex);
            return null;
        }
    }
    
    private PreparedStatement prepareInsertRun(Connection conn) throws SQLException {
        String statement = "INSERT INTO crawl.runs (seed,start) VALUES( ? , ? )";
        PreparedStatement stmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
        return stmt;
    }
    
    private PreparedStatement prepareUpdateRun(Connection conn) throws SQLException {
        String statement = "update crawl.runs set seed=? ,start=?, end=? where id=?";
        PreparedStatement stmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
        return stmt;
    }
    
    public void insert(Connection conn) throws SQLException, Exception {
        @Cleanup
        PreparedStatement prepareInsertRun = prepareInsertRun(conn);
        prepareInsertRun.setString(1, seed);
        prepareInsertRun.setTimestamp(2, start);
        int ret = prepareInsertRun.executeUpdate();
        if(ret != 1) {
            throw new Exception("Could not insert run in db");
        }
        @Cleanup
        ResultSet generatedKeys = prepareInsertRun.getGeneratedKeys();
        generatedKeys.next();
        id = generatedKeys.getInt(1);
    }

    public void update(Connection conn) throws SQLException, Exception {
        @Cleanup
        PreparedStatement prepareInsertRun = prepareUpdateRun(conn);
        prepareInsertRun.setString(1, seed);
        prepareInsertRun.setTimestamp(2, start);
        prepareInsertRun.setTimestamp(3, end);
        prepareInsertRun.setInt(4, id);
        int ret = prepareInsertRun.executeUpdate();
        if(ret != 1) {
            throw new Exception("Could not update run in db");
        }
    }
    
    public void delete(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            @Cleanup
            PreparedStatement deleteFromPages = conn.prepareStatement("delete from crawl.pages where runid=?");
            deleteFromPages.setInt(1, id);
            deleteFromPages.execute();
            
            @Cleanup
            PreparedStatement deleteFromRuns = conn.prepareStatement("delete from crawl.runs where id=?");
            deleteFromRuns.setInt(1, id);
            deleteFromRuns.execute();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
