/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.OutputWriter;
import fr.penet.db.DbUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@Log
public class CrawlDbOutputTitleWordsWriter extends OutputWriter<Map<String,Integer>> {

    int runId;
    
    Map<String,Integer> sliceValues = null;
 
    public CrawlDbOutputTitleWordsWriter(int runIdParam) {
        runId = runIdParam;
    }
    
    @Override
    public void write(Map<String, Integer> input) throws IOException {
        for(Entry<String,Integer> curInput : input.entrySet()) {
            Integer sliceVal = sliceValues.get(curInput.getKey());
            if(sliceVal == null) {
                sliceVal = curInput.getValue();
            } else {
                sliceVal += curInput.getValue();
            }
            sliceValues.put(curInput.getKey(), sliceVal);
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
            PreparedStatement insertUpdate = conn.prepareStatement(
                    "INSERT INTO crawl.words (runid,word,occurrences) VALUES (?,?,?) "
                            + "ON DUPLICATE KEY UPDATE occurrences=occurrences+?"
            );
            for( Entry<String,Integer> entry : sliceValues.entrySet()) {
                insertUpdate.setInt(1,runId);
                insertUpdate.setString(2,entry.getKey());
                insertUpdate.setInt(3,entry.getValue());
                insertUpdate.setInt(4,entry.getValue());
                insertUpdate.execute();
            }
            sliceValues = null;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error while saving title words results", ex);
            throw new IOException(ex);
        }
    }

}
