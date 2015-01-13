/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.crawler;

import java.util.List;
import lombok.AllArgsConstructor;

/**
 *
 * @author lpenet
 */
@AllArgsConstructor
public class CrawlerThread implements Runnable {
    CustomCrawler crawler;
    List<String> seeds;
    @Override
    public void run() {
        crawler.collectUrls(seeds);
    }
    
}
