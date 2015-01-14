package fr.penet.crawler;

import com.google.appengine.api.ThreadManager;
import fr.penet.dao.CrawlPage;
import fr.penet.dao.CrawlRun;
import fr.penet.db.DbSessionProducer;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Named
@Log
public class CustomCrawler implements Serializable {

    @Inject DbSessionProducer dbSessionProducer;
    
    private final boolean followExternalLinks;

    UrlValidator urlValidator = new UrlValidator();
    final static Pattern IGNORE_SUFFIX_PATTERN = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private final int timeout = 3000;
    private final String userAgent = "Mozilla";

    public CustomCrawler() {
        this(false);
    }
    
    public CustomCrawler(boolean followExternalLinksParam) {
        followExternalLinks = followExternalLinksParam;
    }
    
    public void collectUrls(String seedURL, int threads) {
        Connection conn = null;
        int runId = -1;
        try {
            conn = dbSessionProducer.createConnection();
    
            CrawlRun run = new CrawlRun();
            run.setSeed(seedURL);
            run.setStart(new Timestamp(new Date().getTime()));
            run.insert(conn);
            runId = run.getId();
            
            CrawlPage.checkAndInsertURL(runId, conn, seedURL);
            for(int i = 0 ; i < threads ; i++) {
                ThreadManager.createBackgroundThread(new CrawlRunner(run)).start();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, userAgent, ex);
        } finally {
            dbSessionProducer.closeConnection(conn);
        }
    }
    
    @AllArgsConstructor
    public class CrawlRunner implements Runnable {
        CrawlRun run;
        @Override
        public void run() {
            try {
                org.jsoup.Connection jsoupConn = null;
                CrawlPage toVisit;
                Connection conn = dbSessionProducer.createConnection();
                while ( true ) {
                    toVisit = CrawlPage.getFrontierPageForProcessing(run.getId(), conn);
                    try {
                        if(toVisit == null) {
                            if(run.getPagesInProcess(conn) == 0) {
                                break;
                            } else {
                                Thread.sleep(5000);
                                continue;
                            }
                        }
                        Document doc;
                        String urlToCrawl = toVisit.getUrl();
                        if(jsoupConn == null) {
                            jsoupConn = Jsoup.connect(urlToCrawl).userAgent(userAgent).timeout(timeout);
                        } else {
                            jsoupConn.url(urlToCrawl);
                        }
                        doc = jsoupConn.get();
                        Elements links = doc.select("a[href]");
                        for (Element link : links) {

                            String absHref = link.attr("abs:href");
                            //poor man anchor removal
                            int sharpIndex = absHref.indexOf("#");
                            if(sharpIndex != -1) {
                                absHref = absHref.substring(0,sharpIndex);

                            }
                            if (shouldVisit(run.getSeed(), absHref)) {
                                CrawlPage.checkAndInsertURL(run.getId(), conn, absHref);
                            }
                        }
                        toVisit.setStatus(200);
                    } catch (org.jsoup.HttpStatusException e) {
                        log.log(Level.FINE, userAgent, e);
                        toVisit.setStatus(e.getStatusCode());
                    } catch (org.jsoup.UnsupportedMimeTypeException ex) {
                        log.log(Level.FINEST, "Type mime inconnu",ex);
                        toVisit.setStatus(999);
                    } catch(IOException ex) {
                        log.log(Level.INFO, "Exception IO : " + ex.getLocalizedMessage(), ex);
                        jsoupConn = null;
                        toVisit.setStatus(998);
                    }
                    toVisit.update(conn);
                }
                run.setEnd(new Timestamp(new Date().getTime()));
                run.update(conn);
            } catch (SQLException ex) {
                Logger.getLogger(CustomCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        private boolean shouldVisit(String seedURL, String url) {
            // filter out invalid links
            boolean visitUrl = false;
            try {
                boolean followUrl = false;
                Matcher match = IGNORE_SUFFIX_PATTERN.matcher(url);
                boolean isUrlValid = urlValidator.isValid(url);

                if (!followExternalLinks) {
                    if (url.startsWith(seedURL)) {
                        followUrl = true;
                    }
                } else {
                    followUrl = true;
                }
                visitUrl = (!match.matches() && isUrlValid && followUrl);
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            return visitUrl;
        }
    }
}
