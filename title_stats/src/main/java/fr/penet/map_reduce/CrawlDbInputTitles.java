/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@AllArgsConstructor
@Log
public class CrawlDbInputTitles extends Input<String> {
    int runId;
    int shardSize;
    
    @Override
    public List<? extends InputReader<String>> createReaders() throws IOException {
        List<CrawlDbInputTitlesReader> readers = new ArrayList<>();
        try {
            for(int i = 0 ;i < shardSize ; i++) {
                readers.add(new CrawlDbInputTitlesReader(runId, shardSize, i));
            }
            return readers;
        } catch(SQLException ex) {
            log.log(Level.SEVERE, "Error creating readers", ex);
            return null;
        }
    }
    
}
