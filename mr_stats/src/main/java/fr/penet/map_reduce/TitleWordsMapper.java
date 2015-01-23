/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import fr.penet.dao.CrawlPage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lpenet
 */
public class TitleWordsMapper extends MapOnlyMapper<CrawlPage,Map<String,List<Integer>>> {

    @Override
    public void map(CrawlPage input) {
        Map<String,List<Integer>> ret = new HashMap<>();
        if( (input != null) && (input.getText() != null) ) {
            String[] splitted = input.getText().split(" ");
            HashSet<String> consolidated = new HashSet<>();
            for(String cur : splitted) {
                consolidated.add(cur);
            }
            for(String cur : consolidated) {
                List<Integer> curVal = ret.get(cur);
                if(curVal == null) {
                    curVal = new LinkedList<>();
                    ret.put(cur,curVal);
                }
                curVal.add(input.getId());
            }
        }
        emit(ret);
    }
}
