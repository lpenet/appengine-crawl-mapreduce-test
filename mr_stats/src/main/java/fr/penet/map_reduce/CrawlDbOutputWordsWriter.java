/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.OutputWriter;
import fr.penet.dao.CrawlWord;
import fr.penet.dao.CrawlWordPage;
import fr.penet.db.DbUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import lombok.Cleanup;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
public class CrawlDbOutputWordsWriter extends OutputWriter<Map<String,List<Integer>>> {

    int runId;
    
    HashMap<String,List<Integer>> sliceValues = null;
 
    public CrawlDbOutputWordsWriter(int runIdParam) {
        runId = runIdParam;
    }
    
    @Override
    public void write(Map<String,List<Integer>> input) throws IOException {
        for(Entry<String,List<Integer>> curInput : input.entrySet()) {
            List<Integer> sliceVal = sliceValues.get(curInput.getKey());
            if(sliceVal == null) {
                sliceVal = curInput.getValue();
                sliceValues.put(curInput.getKey(), sliceVal);
            } else {
                sliceVal.addAll(curInput.getValue());
            }
            
        }
    }
    
    @Override
    public void beginSlice() {
      sliceValues = new HashMap<>();
    }


    @Override
    public void endSlice() throws IOException {
        try {
            @Cleanup
            Connection conn = new DbUtils().createConnection();
            @Cleanup
            PreparedStatement insertLink = conn.prepareStatement(
                    "INSERT INTO crawl.word_pages (word,page) "
                    + "VALUES (?, ?)");
            
            @Cleanup
            PreparedStatement insertWords = conn.prepareStatement(
                "INSERT INTO crawl.words (runid,word,occurrences) VALUES (?,?,?) "
                        + "ON DUPLICATE KEY UPDATE occurrences=occurrences+?");

            for( Entry<String,List<Integer>> entry : sliceValues.entrySet()) {
                int listLength = entry.getValue().size();
                insertWords.setInt(1, runId);
                insertWords.setString(2, entry.getKey());
                insertWords.setInt(3, listLength);
                insertWords.setInt(4, listLength);
                insertWords.addBatch();

                List<Integer> pageIds = entry.getValue();
                if(!pageIds.isEmpty()) {
                    for(int pageId : pageIds) {
                        insertLink.setString(1, entry.getKey());
                        insertLink.setInt(2, pageId);
                        insertLink.addBatch();
                    }
                }
            }
            insertLink.executeBatch();
            insertWords.executeBatch();
            conn.commit();
            sliceValues = null;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error while saving title words results", ex);
            throw new IOException(ex);
        }
    }

}
