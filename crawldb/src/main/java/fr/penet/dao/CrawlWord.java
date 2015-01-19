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
public class CrawlWord {
    int id;
    int runId;
    String word;
    int occurrences = 0;
    
    public int insert(Connection conn) throws SQLException {
        @Cleanup
        PreparedStatement insertLink = conn.prepareStatement(
                "INSERT INTO crawl.words (runid,word,occurrences) "
                + "VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        insertLink.setInt(1, runId);
        insertLink.setString(2, word);
        insertLink.setInt(3, occurrences);
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
                "UPDATE crawl.words set runid=?, word = ?, occurrences=? "
                + " WHERE id = ?");
        updateLink.setInt(1, runId);
        updateLink.setString(2, word);
        updateLink.setInt(3, occurrences);
        updateLink.setInt(4, id);
        updateLink.executeUpdate();
    }
    
    public void delete(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            @Cleanup
            PreparedStatement deleteWordPages = conn.prepareStatement(
                    "DELETE FROM crawl.word_pages WHERE word = ?");
            deleteWordPages.setInt(1, id);
            @Cleanup
            PreparedStatement deleteWord = conn.prepareStatement(
                    "DELETE FROM crawl.words WHERE id = ?");
            deleteWord.setInt(1, id);
            deleteWord.execute();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    public static CrawlWord getById(Connection conn, int id) throws SQLException {
        @Cleanup
        PreparedStatement stmtGet = conn.prepareStatement("SELECT runid, word, occurrences FROM crawl.words WHERE id=?");
        stmtGet.setInt(1, id);
        ResultSet rsFetched = stmtGet.executeQuery();
        if(!rsFetched.next()) {
            return null;
        }
        return CrawlWord.builder()
                .id(id)
                .runId(rsFetched.getInt(1))
                .word(rsFetched.getString(2))
                .occurrences(rsFetched.getInt(3)).build();
    }
    
}
