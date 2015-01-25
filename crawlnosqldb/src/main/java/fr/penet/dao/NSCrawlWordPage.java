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
public class NSCrawlWordPage implements Serializable {
    
    public final static String NOSQL_CRAWL_WORD_PAGE_ENTITY_BASE = "WordPage_";
    public final static String WORD_PROPERTY = "word";
    
    String type;
    int runId;

    @Getter String word;
    @Getter int pageId;
    
    Key key = null;
    public NSCrawlWordPage(int runIdParam, String wordParam) {
        runId = runIdParam;
        word = wordParam;
        type = getType(runId, word);
    }

    private static String getType(int runIdParam, String wordParam) {
        return NOSQL_CRAWL_WORD_PAGE_ENTITY_BASE + runIdParam + "_" + wordParam;
    }        
    
    public void setWord(String wordParam) {
        word = wordParam;
        key = null;
    }
    
    public void setPageId(int param) {
        pageId = param;
        key = null;
    }
    
    public Entity prepareForInsertUpdateNoPut() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if(key == null) {
            key = KeyFactory.createKey(type, pageId);
        }
        Entity wordPage;
        try {
            wordPage= datastore.get(key);
        } catch (EntityNotFoundException ex) {
            // entity does not exist, let's create it
            wordPage = new Entity(type,pageId);
        }
        return wordPage;
    }
    
    public Key insertUpdate() throws ConcurrentModificationException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.put(prepareForInsertUpdateNoPut());
    }

    public static NSCrawlWordPage getWord(int runId, String word, int pageIdParam) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey(getType(runId,word), pageIdParam);
        Entity wordEntity;
        wordEntity= datastore.get(key);
        
        NSCrawlWordPage cword = new NSCrawlWordPage(runId,word);
        cword.setPageId(pageIdParam);
        cword.key = key;
        return cword;
    }
    
    public static List<NSCrawlWordPage> getRunWordPagesByCount(int runId, String word) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query(getType(runId, word));
        PreparedQuery pq = datastore.prepare(q);
        List<NSCrawlWordPage> ret = new ArrayList<>();
        FetchOptions fo = FetchOptions.Builder.withChunkSize(100);
        for (Entity result : pq.asIterable(fo)) {
            NSCrawlWordPage newElem = new NSCrawlWordPage(runId,word);
            newElem.setPageId(Integer.parseInt(result.getKey().getName()));
            ret.add(newElem);
        }
        return ret;
    }
}
