/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.beans;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import fr.penet.dao.CrawlMapReduceJob;
import fr.penet.dao.CrawlRun;
import fr.penet.viewconfig.Pages;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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
    
    @Getter
    @Setter
    String seed;

    @Getter
    String message;
    
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


    private final static String CRAWLER_MODULE = "crawler";
    private final static String TITLE_STATS_MODULE = "mr_stats";
    
    private String getCrawlerModuleBaseUrl() {
        ModulesService modulesApi = ModulesServiceFactory.getModulesService();
        return "http://" + modulesApi.getVersionHostname(CRAWLER_MODULE,null);
    }
    
    public String getTitleStatsModuleBaseUrl() {
        ModulesService modulesApi = ModulesServiceFactory.getModulesService();
        return "http://" + modulesApi.getVersionHostname(TITLE_STATS_MODULE,null);
    }
    
    public void startCrawl() throws IOException {
        message = "";
        URL startURL = new URL(getCrawlerModuleBaseUrl() + "/startCrawl?seed=" + URLEncoder.encode(seed, "UTF-8"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(startURL.openStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            message += line + "\n";
        }
        reader.close();
    }


    public void stopCrawl(CrawlRun run) throws IOException {
        message = "";
        URL startURL = new URL(getCrawlerModuleBaseUrl() + "/stopCrawl?id=" + run.getId());
        BufferedReader reader = new BufferedReader(new InputStreamReader(startURL.openStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            message += line + "\n";
        }
        reader.close();
    }

    public void resumeCrawl(CrawlRun run) throws IOException {
        message = "";
        URL startURL = new URL(getCrawlerModuleBaseUrl() + "/resumeCrawl?id=" + run.getId());
        BufferedReader reader = new BufferedReader(new InputStreamReader(startURL.openStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            message += line + "\n";
        }
        reader.close();
    }

    public void deleteCrawl(CrawlRun run) throws IOException, SQLException {
        run.delete(conn);
    }

    protected void startTitleStatsCommon(URL urlJob, int runId) throws IOException, SQLException {
        message = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlJob.openStream()));
        String line;
        final String PREFIX = "Mapper started. Job id : ";
        String jobId = null;
        while ((line = reader.readLine()) != null) {
            if(StringUtils.startsWith(line, PREFIX)) {
                jobId = line.substring(PREFIX.length());
                break;
            }
        }
        reader.close();
        if(jobId == null) {
            message = "Error starting job";
            return;
        }
        CrawlMapReduceJob job = CrawlMapReduceJob.builder()
                .runId(runId)
                .appengineMRId(jobId).build();
        job.insert(conn);
        message = "Job " + jobId + " started";
    }

    public void startTitleStats(CrawlRun run) throws IOException, SQLException {
        URL startMRJob = new URL(getTitleStatsModuleBaseUrl() + "/mr_stats?runId="+run.getId()+"&shards=5");
        startTitleStatsCommon(startMRJob,run.getId());
    }

    public void startTitleStatsSQL(CrawlRun run) throws IOException, SQLException {
        URL startMRJob = new URL(getTitleStatsModuleBaseUrl() + "/mr_stats?runId="+run.getId()+"&shards=5&type=sql-output");
        startTitleStatsCommon(startMRJob,run.getId());
    }

    public List<CrawlMapReduceJob> getRunMRJobs(CrawlRun run) throws SQLException {
        return CrawlMapReduceJob.getByRunId(conn, run.getId());
    }
}
