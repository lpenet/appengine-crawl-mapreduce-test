/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import com.google.appengine.api.ThreadManager;
import fr.penet.crawler.CrawlerThread;
import fr.penet.crawler.CustomCrawler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.deltaspike.core.api.scope.ViewAccessScoped;
import org.primefaces.context.RequestContext;

/**
 *
 * @author lpenet
 */
@Named
@ViewAccessScoped
public class CrawlingBean implements Serializable {
    CustomCrawler crawler = null;
    
    @Getter
    List<String> visitedURLS;
    
    @Getter
    @Setter
    List<String> filteredVisitedURLS;
    
    @Getter
    @Setter
    String seed;
    
    Thread backgroundThread = null;

    public boolean isThreadRunning() {
        return (backgroundThread != null) && backgroundThread.isAlive();
    }
    
    public void refresh() {
        if(crawler == null) {
            visitedURLS = null;
        }
        visitedURLS = new ArrayList<String>(crawler.getVisitedUrls());
    }
    
    public void start() {
        if( isThreadRunning() ) {
            RequestContext.getCurrentInstance().execute("alert('Un crawl est déjà en cours!');");
            return;
        }
        List<String> seeds = new ArrayList<>();
        seeds.add(seed);
        crawler = new CustomCrawler(false);
        backgroundThread = ThreadManager.createBackgroundThread(new CrawlerThread(crawler, seeds));
        backgroundThread.start();
        refresh();
    }
}
