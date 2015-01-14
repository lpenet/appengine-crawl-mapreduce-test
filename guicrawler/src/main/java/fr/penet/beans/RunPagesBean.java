/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import fr.penet.dao.CrawlPage;
import fr.penet.viewconfig.Pages;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.scope.ViewAccessScoped;

/**
 *
 * @author lpenet
 */
@Named
@ViewAccessScoped
public class RunPagesBean implements Serializable {
    @Inject
    Connection conn;
    
    @Getter
    @Setter
    int runid;
    
    @Getter
    @Setter
    List<CrawlPage> filteredPages;
    
    List<CrawlPage> pages = null;
    
    public void setRunid(int runidParam) {
        runid = runidParam;
        pages = null;
    }
    
    public List<CrawlPage> getPages() {
        if(pages == null) {
            pages = CrawlPage.getRunPages(runid, conn);
        }
        return pages;
    }

    public Class<? extends ViewConfig> goAccueil() {
        return Pages.Accueil.class;
    }

}
