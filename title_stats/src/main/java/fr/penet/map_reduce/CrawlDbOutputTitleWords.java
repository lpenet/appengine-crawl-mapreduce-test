/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

/**
 *
 * @author lpenet
 */
@AllArgsConstructor
public class CrawlDbOutputTitleWords extends Output<Map<String,Integer>,Void> {
    int runId;
    
    @Override
    public List<? extends OutputWriter<Map<String, Integer>>> createWriters(int count) {
        List<CrawlDbOutputTitleWordsWriter> ret = new ArrayList<>();
        for(int i = 0 ; i < count ; i++) {
            ret.add(new CrawlDbOutputTitleWordsWriter(runId));
        }
        return ret;
    }

    @Override
    public Void finish(Collection<? extends OutputWriter<Map<String, Integer>>> clctn) throws IOException {
        return null;
    }
    
}
