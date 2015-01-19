/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.dao;

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
public class CrawlWordPages {
    int id;
    int word;
    int page;
    
    public int insert(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement insertLink = conn.prepareStatement(
                "INSERT INTO crawl.word_pages (word,page) "
                + "VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertLink.setInt(1, word);
        insertLink.setInt(2, page);
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
                "UPDATE crawl.word_pages set word=?, page = ? "
                + " WHERE id = ?");
        updateLink.setInt(1, word);
        updateLink.setInt(2, page);
        updateLink.setInt(3, id);
        updateLink.executeUpdate();
    }
    
    public void delete(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement deleteLink = conn.prepareStatement(
                "DELETE FROM crawl.word_pages WHERE id = ?");
        deleteLink.setInt(1, id);
        deleteLink.execute();
    }
    
    public static CrawlWordPages getById(Connection conn, int id) throws SQLException {
        @Cleanup
        PreparedStatement stmtGet = conn.prepareStatement("SELECT word,page FROM crawl.page_words WHERE id=?");
        stmtGet.setInt(1, id);
        ResultSet rsFetched = stmtGet.executeQuery();
        if(!rsFetched.next()) {
            return null;
        }
        return CrawlWordPages.builder()
                .id(id)
                .word(rsFetched.getInt(1))
                .page(rsFetched.getInt(2))
                .build();
    }
    
}
