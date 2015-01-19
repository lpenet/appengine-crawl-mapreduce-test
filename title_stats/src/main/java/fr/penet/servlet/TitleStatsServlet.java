/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.servlet;

import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import fr.penet.map_reduce.CrawlDbInputTitles;
import fr.penet.map_reduce.CrawlDbOutputTitleWords;
import fr.penet.map_reduce.TitleWordsMapper;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lpenet
 */
public class TitleStatsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
      resp.setContentType("text/plain");
      String shardsString = req.getParameter("shards");
      if(!StringUtils.isNumeric(shardsString)) {
          resp.getWriter().println("Error : empty shards parameter or not an integer");
          return;
      }
      int shards = Integer.parseInt(shardsString);

      String runIdString = req.getParameter("runId");
      if(!StringUtils.isNumeric(runIdString)) {
          resp.getWriter().println("Error : empty runId parameter or not an integer");
          return;
      }
      int runId = Integer.parseInt(runIdString);

      CrawlDbInputTitles input = new CrawlDbInputTitles(runId, shards);
      TitleWordsMapper mapper = new TitleWordsMapper();
      CrawlDbOutputTitleWords output = new CrawlDbOutputTitleWords(runId);
      MapSpecification<String,
              Map<String,Integer>,
              Void> spec = new MapSpecification.Builder<>(input, mapper, output)
              .setJobName("Title stats").build();
      // default settings should be ok
      MapSettings settings = new MapSettings.Builder().build();
      String jobId = MapJob.start(spec, settings);
      resp.getWriter().println("Mapper started. Job id : " + jobId);
  }
}