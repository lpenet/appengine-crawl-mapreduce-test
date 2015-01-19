package fr.penet.crawler;

import com.google.appengine.api.ThreadManager;
import fr.penet.dao.CrawlPage;
import fr.penet.dao.CrawlRun;
import fr.penet.db.DbUtils;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Log
public class CustomCrawler implements Serializable {

    DbUtils dbUtils = new DbUtils();
    
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
    
    public int startCollectUrls(String seedURL, int threads) {
        Connection conn = null;
        int runId = -1;
        try {
            conn = dbUtils.createConnection();
    
            CrawlRun run = new CrawlRun();
            run.setSeed(seedURL);
            run.setStart(new Timestamp(new Date().getTime()));
            run.insert(conn);
            runId = run.getId();
            
            CrawlPage.checkAndInsertURL(runId, conn, seedURL);
            for(int i = 0 ; i < threads ; i++) {
                ThreadManager.createBackgroundThread(new CrawlRunner(this, run)).start();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, userAgent, ex);
        } finally {
            dbUtils.closeConnection(conn);
        }
        return runId;
    }
    
    public void resumeCollectUrls(int runId, int threads) {
        Connection conn = null;
        try {
            conn = dbUtils.createConnection();
    
            CrawlRun run = CrawlRun.getRunById(conn, runId);
            if(run == null) {
                log.log(Level.WARNING, "No run with id " + runId);
                return;
            }
            
            run.setStart(new Timestamp(new Date().getTime()));
            run.setEnd(null);
            run.update(conn);
            
            for(int i = 0 ; i < threads ; i++) {
                ThreadManager.createBackgroundThread(new CrawlRunner(this,run)).start();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, userAgent, ex);
        } finally {
            dbUtils.closeConnection(conn);
        }
    }
    
    public void endCollectUrls(int runId) {
        Connection conn = null;
        try {
            conn = dbUtils.createConnection();
    
            CrawlRun run = CrawlRun.getRunById(conn, runId);
            run.setStart(new Timestamp(new Date().getTime()));
            run.setEnd(new Timestamp(new Date().getTime()));
            run.update(conn);
        } catch (Exception ex) {
            log.log(Level.SEVERE, userAgent, ex);
        } finally {
            dbUtils.closeConnection(conn);
        }
    }
    
    @AllArgsConstructor
    public class CrawlRunner implements Runnable {
        final CustomCrawler parent;
        CrawlRun run;
        @Override
        public void run() {
            Connection conn = null;
            try {
                org.jsoup.Connection jsoupConn = null;
                CrawlPage toVisit;
                conn = dbUtils.createConnection();
                while ( true ) {
                    int runId = run.getId();
                    run = CrawlRun.getRunById(conn, runId);
                    if(run == null) {
                        log.log(Level.INFO, "No run with id " + runId + ". Exiting thread.");
                        return;
                    }
                    if(run.getEnd() != null) {
                        // an end date is recorded. This is a signal for the threads to stop
                        log.log(Level.INFO, "End date found for run " + runId + ". Exiting thread.");
                        return;
                    }
                    toVisit = CrawlPage.getFrontierPageForProcessing(run.getId(), conn);
                    try {
                        if(toVisit == null) {
                            if(run.getPagesInProcess(conn) == 0) {
                                break;
                            } else {
                                synchronized(parent) {
                                    parent.wait();
                                }
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
                                synchronized(parent) {
                                    parent.notifyAll();
                                }
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
            } finally {
                dbUtils.closeConnection(conn);
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
