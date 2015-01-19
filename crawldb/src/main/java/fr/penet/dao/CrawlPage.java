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
public class CrawlPage implements Serializable {
    int id;
    int runid;
    String url;
    Integer status;
    String title;
    
    public static final int STATUS_IN_PROCESS = -1;
    
    public static List<CrawlPage> getRunPages(int runid, Connection conn) {
        List<CrawlPage> listRet = new ArrayList<>();
        try {
            @Cleanup
            PreparedStatement stmtRunPages = conn.prepareStatement("select id, runid, url, status, title from crawl.pages where runid = ?");
            stmtRunPages.setInt(1, runid);
            @Cleanup
            ResultSet resultPages = stmtRunPages.executeQuery();
            while(resultPages.next()) {
                CrawlPage page = new CrawlPage();
                page.setId(resultPages.getInt(1));
                page.setRunid(resultPages.getInt(2));
                page.setUrl(resultPages.getString(3));
                page.setStatus(resultPages.getInt(4));
                if(resultPages.wasNull()) {
                    page.setStatus(null);
                }
                page.setTitle(resultPages.getString(5));
                
                listRet.add(page);
            }
            
        } catch(SQLException ex) {
            log.log(Level.SEVERE,ex.getLocalizedMessage(),ex);
        }
        return listRet;
    }
    
    public static CrawlPage getFrontierPageForProcessing(int runid, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            @Cleanup
            PreparedStatement stmtFrontierPage = conn.prepareStatement("select id, runid, url, status, title from crawl.pages where runid= ? and status is null having min(id) for update",
                    ResultSet.CONCUR_UPDATABLE);
            stmtFrontierPage.setInt(1, runid);
            @Cleanup
            ResultSet resultFrontierPage = stmtFrontierPage.executeQuery();
            if(!resultFrontierPage.next()) {
                return null;
            }
            CrawlPage ret = new CrawlPage();
            ret.setId(resultFrontierPage.getInt(1));
            ret.setRunid(resultFrontierPage.getInt(2));
            ret.setUrl(resultFrontierPage.getString(3));
            ret.setStatus(STATUS_IN_PROCESS);
            ret.setTitle(resultFrontierPage.getString(5));
            PreparedStatement stmtUpdateFrontierPage = conn.prepareStatement("update crawl.pages set status = " + STATUS_IN_PROCESS + " where id = " + ret.getId());
            stmtUpdateFrontierPage.execute();
            return ret;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    public static int checkAndInsertURL(int runid, Connection conn, String url) throws SQLException, Exception {
        conn.setAutoCommit(false);
        int ret = -1;
        try {
            @Cleanup
            PreparedStatement lockingStatement = conn.prepareStatement("select p.id from crawl.pages p, crawl.runs r where r.id = ? and p.runid = r.id and p.url = r.seed for update");
            lockingStatement.setInt(1, runid);
            lockingStatement.execute();
            @Cleanup
            PreparedStatement stmtFrontierPage = conn.prepareStatement("select id from crawl.pages p where runid= ? and url = ?");
            stmtFrontierPage.setInt(1, runid);
            stmtFrontierPage.setString(2, url);
            @Cleanup
            ResultSet resultFrontierPage = stmtFrontierPage.executeQuery();
            if(!resultFrontierPage.next()) {
                CrawlPage toInsert = new CrawlPage();
                toInsert.setRunid(runid);
                toInsert.setStatus(null);
                toInsert.setUrl(url);
                ret = toInsert.insert(conn);
            } else {
                ret = resultFrontierPage.getInt(1);
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
        return ret;
    }
    public int insert(Connection conn) throws SQLException, Exception {
        @Cleanup
        PreparedStatement prepareInsertURL = prepareInsertURL(conn);
        prepareInsertURL.setInt(1, runid);
        prepareInsertURL.setString(2, url);
        prepareInsertURL.setString(3, title);
        int ret = prepareInsertURL.executeUpdate();
        if(ret != 1) {
                throw new Exception("Could not insert run in db");
        }
        @Cleanup
        ResultSet generatedKeys = prepareInsertURL.getGeneratedKeys();
        generatedKeys.next();
        id = generatedKeys.getInt(1);
        return id;
    }
    
    public void update(Connection conn) throws SQLException, Exception {
        @Cleanup
        PreparedStatement prepareUpdateURL = prepareUpdateURL(conn);
        prepareUpdateURL.setInt(1, runid);
        prepareUpdateURL.setString(2, url);
        prepareUpdateURL.setInt(3, status);
        prepareUpdateURL.setString(4, title);
        prepareUpdateURL.setInt(5, id);
        int ret = prepareUpdateURL.executeUpdate();
        if(ret != 1) {
                throw new Exception("Could not update run in db");
        }
    }
    
    private PreparedStatement prepareInsertURL(Connection conn) throws SQLException {
        String statement = "INSERT INTO crawl.pages (runid, url,title) VALUES( ?, ?, ? )";
        PreparedStatement stmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
        return stmt;
    }

    private PreparedStatement prepareUpdateURL(Connection conn) throws SQLException {
        String statement = "update crawl.pages set runid = ?, url = ?, status = ?, title = ? where id = ?";
        PreparedStatement stmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
        return stmt;
    }
}
