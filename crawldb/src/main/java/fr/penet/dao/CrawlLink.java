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
public class CrawlLink implements Serializable {
    int id;
    int pageFrom;
    int pageTo;
    String text;
    
    public int insert(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement insertLink = conn.prepareStatement(
                "INSERT INTO crawl.links (page_from,page_to,text) "
                + "VALUES (?, ?,?)", Statement.RETURN_GENERATED_KEYS);
        insertLink.setInt(1, pageFrom);
        insertLink.setInt(2, pageTo);
        insertLink.setString(3, text);
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
                "UPDATE crawl.links set page_from=?, page_to = ?, text=? "
                + " WHERE id = ?");
        updateLink.setInt(1, pageFrom);
        updateLink.setInt(2, pageTo);
        updateLink.setString(3, text);
        updateLink.setInt(4, id);
        updateLink.executeUpdate();
    }
    
    public void delete(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement deleteLink = conn.prepareStatement(
                "DELETE FROM crawl.links WHERE id = ?");
        deleteLink.setInt(1, id);
        deleteLink.execute();
    }
    
    public static CrawlLink getById(Connection conn, int id) throws SQLException {
        @Cleanup
        PreparedStatement stmtGet = conn.prepareStatement("SELECT page_from, page_to, text FROM crawl.links WHERE id=?");
        stmtGet.setInt(1, id);
        @Cleanup
        ResultSet rsFetched = stmtGet.executeQuery();
        if(!rsFetched.next()) {
            return null;
        }
        return CrawlLink.builder()
                .id(id)
                .pageFrom(rsFetched.getInt(1))
                .pageTo(rsFetched.getInt(2))
                .text(rsFetched.getString(3)).build();
    }
    
}
