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
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;

/**
 *
 * @author lpenet
 */
@AllArgsConstructor
public class CrawlDbOutputWordsDatastore extends Output<Map<String,List<Integer>>,Void> {
    int runId;
    
    @Override
    public List<? extends OutputWriter<Map<String,List<Integer>>>> createWriters(int count) {
        List<CrawlDbOutputWordsDatastoreWriter> ret = new ArrayList<>();
        for(int i = 0 ; i < count ; i++) {
            ret.add(new CrawlDbOutputWordsDatastoreWriter(runId));
        }
        return ret;
    }

    @Override
    public Void finish(Collection<? extends OutputWriter<Map<String,List<Integer>>>> clctn) throws IOException {
        return null;
    }
    
}
