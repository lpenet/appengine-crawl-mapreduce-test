/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.servlet;

import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.Output;
import fr.penet.dao.CrawlPage;
import fr.penet.map_reduce.CrawlDbInput;
import fr.penet.map_reduce.CrawlDbOutputWords;
import fr.penet.map_reduce.CrawlDbOutputWordsDatastore;
import fr.penet.map_reduce.TitleWordsMapper;
import java.io.IOException;
import java.util.List;
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

      
      String runType = req.getParameter("type");
      CrawlDbInput input = new CrawlDbInput(runId, shards);
      TitleWordsMapper mapper = new TitleWordsMapper();
      Output<Map<String,List<Integer>>,Void> output;
      String jobName = "MR stats for " + runId;
      if(StringUtils.equals(runType, "sql-output")) {
          output = new CrawlDbOutputWords(runId);
          jobName += " (SQL)";
      } else {
          output = new CrawlDbOutputWordsDatastore(runId);
          jobName += " (datastore)";
      }
      MapSpecification<CrawlPage,
              Map<String,List<Integer>>,
              Void> spec = new MapSpecification.Builder<>(input, mapper, output)
              .setJobName(jobName).build();
      // default settings should be ok
      MapSettings settings = new MapSettings.Builder().build();
      String jobId = MapJob.start(spec, settings);
      resp.getWriter().println("Mapper started. Job id : " + jobId);
  }
}