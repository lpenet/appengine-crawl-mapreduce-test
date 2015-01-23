/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.tools.mapreduce.OutputWriter;
import fr.penet.utils.ShardedCounter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
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


    private String getKeyFromWord(String word) {
        String ret = "(" + word + ")";
        return ret.substring(0, Math.min(ret.length(), 500)-1);
    }
    
    @Override
    public void endSlice() throws IOException {
        final String WORD_TYPE = "Word";
        final String WORD_PAGE_TYPE = "WordPage";
        final String WORD_PROPERTY = "word";
        final String PAGE_ID_PROPERTY = "pageid";
        final int BATCH_SIZE=100;
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        List<Entity> listWordPages = new ArrayList<>(BATCH_SIZE);
//        int curEntIdx = -1;
        for( Entry<String,List<Integer>> entry : sliceValues.entrySet()) {
//            curEntIdx++;
//            log.log(Level.INFO, "Shard " + getContext().getShardNumber() + " : processing entry " + curEntIdx + " out of " + sliceValues.size() + " : " + entry.getKey());
            // let's add the word to datastore, if it does not exist
            String wordKey = getKeyFromWord(entry.getKey());
            Key keyWord = KeyFactory.createKey(WORD_TYPE, wordKey);
            try {
                Entity word= datastore.get(keyWord);
            } catch (EntityNotFoundException ex) {
                try {
                    Entity word = new Entity(WORD_TYPE,wordKey);
                    datastore.put(word);
                } catch(ConcurrentModificationException e) {
                    // ok, the entity has been created...
                }
            }

            List<Integer> pageIds = entry.getValue();
            if(!pageIds.isEmpty()) {
                // let's increment its sharded counter
                ShardedCounter sc = new ShardedCounter(wordKey);
                sc.increment(pageIds.size());

                for(int pageId : pageIds) {
                    Entity wordPage = new Entity(WORD_PAGE_TYPE);
                    wordPage.setProperty(WORD_PROPERTY, new Text(entry.getKey()));
                    wordPage.setProperty(PAGE_ID_PROPERTY, pageId);
                    // we should not raise an exception there, as we are the only processor of the page
                    listWordPages.add(wordPage);
                }
                if(listWordPages.size() >= BATCH_SIZE) {
                    datastore.put(listWordPages);
                    listWordPages.clear();
                }
            }
        }
        datastore.put(listWordPages);
        sliceValues = null;
    }

}
