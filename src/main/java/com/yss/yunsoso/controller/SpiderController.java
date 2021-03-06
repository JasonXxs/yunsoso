package com.yss.yunsoso.controller;

import com.alibaba.fastjson.JSONObject;
import com.yss.yunsoso.service.RedisFacade;
import com.yss.yunsoso.service.SolrFacade;
import com.yss.yunsoso.service.SpiderFacade;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by beyondLi on otherConfig.pageSize17/6/19.
 */
//证明是controller层并且返回json
@Controller
public class SpiderController {

    private static final Logger  logger = org.slf4j.LoggerFactory.getLogger(SpiderController.class);

    @Resource
    SpiderFacade spiderFacade;
    @Autowired
    private SolrFacade solrFacade;
    @Autowired
    private RedisFacade redisFacade;
    @Value("${queuekey}")
    private String queue;

    @Resource
    private RedisTemplate redisTemplate;


    @RequestMapping(value = "/find/{kw}/{index}")
    public String find(@PathVariable String kw , @PathVariable Integer index , ModelMap map) {
        String results = solrFacade.getResults(kw, index);
        if(results!=null){
            JSONObject responseObject = JSONObject.parseObject(results);
            map.put("currentPage", responseObject.get("currPage"));
            map.put("totalItem", responseObject.get("total"));
            map.put("results", responseObject.getJSONArray("results"));
            map.put("kw", kw);
            return "/list";
        }else{
            Boolean isok = redisFacade.addReidisQueue(queue, kw);

//            siderFacade.getSpider(kw);
        }


        return "suc";
    }

    /**
     * 预约授权关键字
     * @param kw
     * @return
     */
    @RequestMapping(value = "/reserve/{kw}")
    @ResponseBody
    public String reserve(@PathVariable String kw) {
        Boolean isok = redisFacade.addReidisQueue(queue, kw);

        return "suc";
    }

    //检查solr是否有该关键字数据
    @RequestMapping(value = "/checkThisKeyword/{kw}")
    @ResponseBody
    public String checkThisKeyword(@PathVariable String kw) {
        String results = solrFacade.getResults(kw, 0);
        if(results==null){  //solr未储存 加入队列
            redisFacade.addReidisQueue(queue, kw);
            return "0";
        }
        return "1";
    }

    @RequestMapping(value = "/findAll/{index}")
    public String findAll(ModelMap map,@PathVariable Integer index) {

        String results = solrFacade.getResults(index);
        JSONObject responseObject = JSONObject.parseObject(results);
        map.put("currentPage", responseObject.get("currPage"));
        map.put("totalItem", responseObject.get("total"));
        map.put("results", responseObject.getJSONArray("results"));

        return "/list";
    }




    @RequestMapping(value = "/index")
    public String index() {
        return "/search";
    }

    @RequestMapping(value = "/list")
    public String list() {
        return "/list";
    }


    @RequestMapping(value = "/redis_test")
    @ResponseBody
    public String redis_test()  {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore("reserve_kw", "复仇者联盟", 1.0);
        zSetOperations.incrementScore("reserve_kw", "金刚狼", 4.0);

        return null;
    }
}