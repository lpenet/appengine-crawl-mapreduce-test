package fr.penet.crawler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CustomCrawler {

    private final boolean followExternalLinks;
    ArrayDeque<String> linksToCrawl = new ArrayDeque<>();
    @Getter
    Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<String>());
    UrlValidator urlValidator = new UrlValidator();
    final static Pattern IGNORE_SUFFIX_PATTERN = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))\\$");

    private final int timeout = 3000;
    private final String userAgent = "Mozilla";

    List<String> seedURLs = null;

    public CustomCrawler(boolean followExternalLinksParam) {
        followExternalLinks = followExternalLinksParam;
    }
    
    public void collectUrls(List<String> seedURLsParam) {
        seedURLs = seedURLsParam;
        linksToCrawl.addAll(seedURLs);
        try {
            while (!linksToCrawl.isEmpty()) {
                String urlToCrawl = linksToCrawl.poll();
                try {
                    visitedUrls.add(urlToCrawl);
                    // extract URL from HTML using Jsoup
                    Document doc = Jsoup.connect(urlToCrawl).userAgent(userAgent).timeout(timeout).get();
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {

                        String absHref = link.attr("abs:href");
                        if (shouldVisit(absHref)) {
                            // If this set already contains the element, the call leaves the set unchanged and returns false.
                            if (visitedUrls.add(absHref)) {
                                if (!linksToCrawl.contains(absHref)) {
                                    linksToCrawl.add(absHref);
                                }
                            }
                        }
                    }
                } catch (org.jsoup.HttpStatusException e) {
          // ignore 404
                    // handle exception
                } catch (java.net.SocketTimeoutException e) {
                    // handle exception
                }
            }
        } catch (Exception e) {
            // handle exception
        }
    }

    private boolean shouldVisit(String url) {
        // filter out invalid links
        boolean visitUrl = false;
        try {
            boolean followUrl = false;
            Matcher match = IGNORE_SUFFIX_PATTERN.matcher(url);
            boolean isUrlValid = urlValidator.isValid(url);

            if (!followExternalLinks) {
                // follow only urls which starts with any of the seed urls
                for (String seedURL : seedURLs) {
                    if (url.startsWith(seedURL)) {
                        followUrl = true;
                        break;
                    }
                }
            } else {
                // follow any url
                followUrl = true;
            }
            visitUrl = (!match.matches() && isUrlValid && followUrl);
        } catch (Exception e) {
            // handle exception
        }
        return visitUrl;
    }
}
