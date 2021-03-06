package com.yss.yunsoso.service.impl;

import com.yss.yunsoso.pipline.MysqlPipeline;
import com.yss.yunsoso.service.SpiderFacade;
import com.yss.yunsoso.spdier.BaiduYunFindFileFromBaidu;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.PhantomJSDownloader;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
@Transactional
@Lazy(false)
public class SpiderFacadeImpl implements SpiderFacade {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SpiderFacadeImpl.class);

    @Value("${phantomjsPath}")
    private String phantomjsPath;
    @Value("${searchPage}")
    private Integer searchPage;
    @Value("${crwalPath}")
    private String crwalPath;

    @Resource
    private MysqlPipeline mysqlPipeline;


    @Override
    public boolean getSpider(String keyword) {
        String moviesName = "\""+keyword+"\" ";
        String postfix = "\"pan.baidu.com\"";
        try {
            moviesName = URLEncoder.encode(moviesName, "gb2312");
            postfix = URLEncoder.encode(postfix, "gb2312");
            System.out.println(moviesName);

            for (int i = 0; i < searchPage; i++) {
//                String url = ("https://www.baidu.com/s?wd="+moviesName+postfix+"&pn="+i+"0");
                String url = ("http://pan.baidu.com/s/1pJ6XYHl");
                Spider.create(new BaiduYunFindFileFromBaidu())
                        .addUrl(url)
                        .setDownloader(new PhantomJSDownloader(phantomjsPath,crwalPath))
                        .thread(1)
                        .addPipeline(mysqlPipeline)
                        .run();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }
}
