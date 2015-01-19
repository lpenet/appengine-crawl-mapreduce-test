/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.servlet;

import fr.penet.crawler.CustomCrawler;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lpenet
 */
public class StopCrawlServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
      resp.setContentType("text/plain");
      String runIdString = req.getParameter("id");
      if(StringUtils.isEmpty(runIdString)) {
          resp.getWriter().println("Error : empty id parameter");
          return;
      }
      if(!StringUtils.isNumeric(runIdString)) {
          resp.getWriter().println("Error : id parameter is not an integer");
          return;
      }
      int runId = Integer.parseInt(runIdString);
      CustomCrawler crawler = new CustomCrawler();
      crawler.endCollectUrls(runId);
      resp.getWriter().println("Requested stop of crawl run " + runId);
  }
}