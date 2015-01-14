/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import fr.penet.dao.CrawlRun;
import fr.penet.viewconfig.Pages;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.RequestScoped;
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
public class RunsBean implements Serializable {
    @Inject Connection conn;
    
    public List<CrawlRun> getRuns() {
        return CrawlRun.getAllRuns(conn);
    }
    
    @Getter
    @Setter
    private List<CrawlRun> filteredRuns;

    public Class<? extends ViewConfig> goAccueil() {
        return Pages.Accueil.class;
    }
    
    public Class<? extends ViewConfig> goRunPages() {
        return Pages.RunPages.class;
    }

    public int getPageCount(CrawlRun c) throws SQLException {
        return c.getPageCount(conn);
    }

    public int getPagesToProcess(CrawlRun c) throws SQLException {
        return c.getPagesToProcess(conn);
    }

    public int getPagesInProcess(CrawlRun c) throws SQLException {
        return c.getPagesInProcess(conn);
    }

    public int getPagesProcessed(CrawlRun c) throws SQLException {
        return c.getPagesProcessed(conn);
    }
}
