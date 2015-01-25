/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import fr.penet.utils.ShardedCounter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author lpenet
 */
@RequiredArgsConstructor
public class NSCrawlWord implements Serializable {
    
    public final static String NOSQL_CRAWL_WORD_ENTITY_BASE = "Word_";
    public final static String NOSQL_CRAWL_WORD_COUNT_PROPERTY = "count";
    // yes, we store word as key and as property
    // key is wrapped in ( ) and truncated
    // and, well, this is just a fun test project. :-)
    public final static String NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY = "realWord";
    
    String type;
    int runId;

    @Getter String word;
    @Getter @Setter int count;

    private static String getKeyFromWord(String word) {
        String ret = "(" + word + ")";
        return ret.substring(0, Math.min(ret.length(), 500)-1);
    }
    
    public ShardedCounter getCounter() {
        return new ShardedCounter(type + getKeyFromWord(word));
    }
    
    Key key = null;
    public NSCrawlWord(int runIdParam) {
        runId = runIdParam;
        type = getType(runId);
    }

    public NSCrawlWord(int runIdParam, String wordParam) {
        this(runIdParam);
        word = wordParam;
    }
    
    private static String getType(int runIdParam) {
        return NOSQL_CRAWL_WORD_ENTITY_BASE + runIdParam;
    }        
    
    public void setWord(String wordParam) {
        word = wordParam;
        key = null;
    }
    
    public Entity prepareForInsertUpdateNoPut() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String keyableWord = getKeyFromWord(word);
        if(key == null) {
            key = KeyFactory.createKey(type, keyableWord);
        }
        Entity wordEntity;
        try {
            wordEntity= datastore.get(key);
        } catch (EntityNotFoundException ex) {
            // entity does not exist, let's create it
            wordEntity = new Entity(type,keyableWord);
        }
        wordEntity.setProperty(NOSQL_CRAWL_WORD_COUNT_PROPERTY, count);
        wordEntity.setProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY, new Text(word));
        return wordEntity;
    }
    
    public Key insertUpdate() throws ConcurrentModificationException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.put(prepareForInsertUpdateNoPut());
    }

    public static NSCrawlWord getWord(int runId, String word) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey(getType(runId), getKeyFromWord(word));
        Entity wordEntity;
        wordEntity= datastore.get(key);
        
        NSCrawlWord cword = new NSCrawlWord(runId);
        cword.setWord(((Text)wordEntity.getProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY)).toString());
        cword.setCount((int) wordEntity.getProperty(NOSQL_CRAWL_WORD_COUNT_PROPERTY));
        cword.key = key;
        return cword;
    }
    
    public static List<NSCrawlWord> getRunWordsByCount(int runId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query(getType(runId)).addSort(NOSQL_CRAWL_WORD_COUNT_PROPERTY, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);
        List<NSCrawlWord> ret = new ArrayList<>();
        FetchOptions fo = FetchOptions.Builder.withChunkSize(100);
        for (Entity result : pq.asIterable(fo)) {
            NSCrawlWord newElem = new NSCrawlWord(runId);
            newElem.setWord((String) result.getProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY));
            newElem.setCount(((Long)result.getProperty(NOSQL_CRAWL_WORD_COUNT_PROPERTY)).intValue());
            ret.add(newElem);
        }
        return ret;
    }
}
