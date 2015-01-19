/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.penet.map_reduce;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lpenet
 */
public class TitleWordsMapper extends MapOnlyMapper<String,Map<String,Integer>> {

    @Override
    public void map(String input) {
        Map<String,Integer> ret = new HashMap<>();
        String[] splitted = input.split(" ");
        for(String cur : splitted) {
            Integer curVal = ret.get(cur);
            if(curVal == null) {
                curVal = 1;
            } else {
                curVal++;
            }
            ret.put(cur,curVal);
        }
        emit(ret);
    }
}
