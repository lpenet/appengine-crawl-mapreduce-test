/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import com.google.appengine.api.ThreadManager;
import fr.penet.crawler.CrawlerThread;
import fr.penet.crawler.CustomCrawler;
import fr.penet.viewconfig.Pages;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.primefaces.context.RequestContext;

/**
 *
 * @author lpenet
 */
@Named
@SessionScoped
public class CrawlingBean implements Serializable {
    @Inject
    CustomCrawler crawler;
    
    @Getter
    @Setter
    String seed;
    
    Thread backgroundThread = null;

    public boolean isThreadRunning() {
        return (backgroundThread != null) && backgroundThread.isAlive();
    }
    
    public Class<? extends ViewConfig>  start() {
        if( isThreadRunning() ) {
            RequestContext.getCurrentInstance().execute("alert('Un crawl est déjà en cours!');");
            return null;
        }
        backgroundThread = ThreadManager.createBackgroundThread(new CrawlerThread(crawler, seed));
        backgroundThread.start();
        return Pages.Accueil.class;
    }


}


