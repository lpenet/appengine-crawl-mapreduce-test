/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import com.google.appengine.api.datastore.EntityNotFoundException;
import fr.penet.dao.NSCrawlWord;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author lpenet
 */
@Log
public class LazyNSCrawlWordModel extends LazyDataModel<NSCrawlWord> {
    @Getter int runId;
    
    Integer count = null;
    
    @Override
    public NSCrawlWord getRowData(String wordStr) {
        try {
            return NSCrawlWord.getWord(runId, wordStr);
        } catch (EntityNotFoundException ex) {
            return null;
        }
    }
 
    @Override
    public Object getRowKey(NSCrawlWord word) {
        return word.getWord();
    }
 
    @Override
    public List<NSCrawlWord> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        return NSCrawlWord.getRunWordsByCount(runId, first, pageSize);
    }
    
    @Override
    public int getRowCount() {
        if(count == null) {
            count = NSCrawlWord.getRunWordsByCountCount(runId);
        }
        return count;
    }
    
    public void setRunId(int runIdParam) {
        runId = runIdParam;
        count = null;
        
    }
}
