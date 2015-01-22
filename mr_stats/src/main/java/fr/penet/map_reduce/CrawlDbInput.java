/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;
import fr.penet.dao.CrawlPage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

/**
 *
 * @author lpenet
 */
@AllArgsConstructor
@Log
public class CrawlDbInput extends Input<CrawlPage> {
    int runId;
    int shardSize;
    
    @Override
    public List<? extends InputReader<CrawlPage>> createReaders() throws IOException {
        List<CrawlDbInputReader> readers = new ArrayList<>();
        for(int i = 0 ;i < shardSize ; i++) {
            readers.add(new CrawlDbInputReader(runId));
        }
        return readers;
    }
    
}
