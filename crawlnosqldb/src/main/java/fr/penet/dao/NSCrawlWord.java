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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
    
    public final static String NOSQL_CRAWL_WORD_ENTITY_BASE = "Word";
    public final static String NOSQL_CRAWL_WORD_COUNT_PROPERTY = "count";
    public final static String NOSQL_CRAWL_RUNID_PROPERTY = "runId";
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
        return NOSQL_CRAWL_WORD_ENTITY_BASE;
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
        wordEntity.setProperty(NOSQL_CRAWL_RUNID_PROPERTY, runId);
        wordEntity.setProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY, new Text(word));
        return wordEntity;
    }
    
    public Key insertUpdate() throws ConcurrentModificationException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.put(prepareForInsertUpdateNoPut());
    }

    private static Filter getRunIdFilter(int runId) {
        return new FilterPredicate(NOSQL_CRAWL_RUNID_PROPERTY,
                FilterOperator.EQUAL,
                runId);
    }
    public static NSCrawlWord getWord(int runId, String word) throws EntityNotFoundException {
        Key key = KeyFactory.createKey(getType(runId), getKeyFromWord(word));
                
        Query q = new Query(getType(runId)).setFilter(getRunIdFilter(runId));
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity wordEntity = datastore.prepare(q).asSingleEntity();
        
        NSCrawlWord cword = new NSCrawlWord(runId);
        cword.setWord(((Text)wordEntity.getProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY)).getValue());
        cword.setCount((int) wordEntity.getProperty(NOSQL_CRAWL_WORD_COUNT_PROPERTY));
        cword.key = key;
        return cword;
    }
    
    public static List<NSCrawlWord> getRunWordsByCount(int runId) {
        return getRunWordsByCount(runId,0,0);
    }

    public static int getRunWordsByCountCount(int runId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query(getType(runId)).setFilter(getRunIdFilter(runId)).addSort(NOSQL_CRAWL_WORD_COUNT_PROPERTY, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);
        FetchOptions fo = FetchOptions.Builder.withDefaults();
        return pq.countEntities(fo);
    }

    public static List<NSCrawlWord> getRunWordsByCount(int runId, int first, int pageSize) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query(getType(runId)).setFilter(getRunIdFilter(runId)).addSort(NOSQL_CRAWL_WORD_COUNT_PROPERTY, SortDirection.DESCENDING);
        PreparedQuery pq = datastore.prepare(q);
        List<NSCrawlWord> ret = new ArrayList<>();
        FetchOptions fo = FetchOptions.Builder.withChunkSize(Math.min(100,pageSize));
        if(first > 0) {
            fo = fo.offset(first);
        }
        if(pageSize > 0) {
            fo = fo.limit(pageSize);
        }
        for (Entity result : pq.asIterable(fo)) {
            NSCrawlWord newElem = new NSCrawlWord(runId);
            newElem.setWord(((Text) result.getProperty(NOSQL_CRAWL_WORD_FULL_WORD_PROPERTY)).getValue());
            newElem.setCount(((Long)result.getProperty(NOSQL_CRAWL_WORD_COUNT_PROPERTY)).intValue());
            ret.add(newElem);
        }
        return ret;
    }
}
