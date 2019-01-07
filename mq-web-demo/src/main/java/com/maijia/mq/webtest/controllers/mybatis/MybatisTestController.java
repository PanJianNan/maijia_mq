package com.maijia.mq.webtest.controllers.mybatis;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.webtest.controllers.aop.AopTestController;
import com.maijia.mq.webtest.dao.TUserMapper;
import com.maijia.mq.webtest.domain.TUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by panjiannan on 2018/8/3.
 */
@RestController
@RequestMapping("/mybatis")
public class MybatisTestController {

    @PostConstruct
    public void init() {
        logger.info("<<<<<<<<<<MybatisTestController init>>>>>>>>");
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TUserMapper tUserMapper;

    @RequestMapping("/test")
    public String test() {
        logger.info("=========test method query==========");
        TUser user = tUserMapper.selectByPrimaryKey(4L);
        logger.info("=========test method query==========");
        TUser user2 = tUserMapper.selectByPrimaryKey(4L);
        logger.info("=========test method query==========");
        TUser user3 = tUserMapper.selectByPrimaryKey(12L);
        logger.info("=========test method query==========");
        TUser user4 = tUserMapper.selectByPrimaryKey(4L);
        return JSONObject.toJSONString(user);
    }

    @Transactional
    @RequestMapping("/test2")
    public String test2() {
        logger.info("=========test2222 method query==========");
        TUser user = tUserMapper.selectByPrimaryKey(4L);
        logger.info("=========test2222 method query==========");
        TUser user2 = tUserMapper.selectByPrimaryKey(4L);
        return JSONObject.toJSONString(user);
    }

    @RequestMapping("/addTest")
    public String addTest() {
        logger.info("=========addTest method query==========");
        TUser user = tUserMapper.selectByPrimaryKey(4L);
        logger.info("=========addTest method query==========");
        TUser user2 = tUserMapper.selectByPrimaryKey(4L);
        logger.info("=========addTest method query==========");
        TUser user3 = tUserMapper.selectByPrimaryKey(12L);
        logger.info("=========addTest method query==========");
        TUser user4 = tUserMapper.selectByPrimaryKey(4L);
        return JSONObject.toJSONString(user);
    }


}
