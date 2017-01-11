package com.maijia.mq;

import com.maijia.mq.rpc.CustomBeanFactory;
import com.maijia.mq.rpc.RpcFramework;
import com.maijia.mq.service.ICacheMqService;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.IFileMqService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * InitRPC
 *
 * @author panjn
 * @date 2017/1/3
 */
@Component
public class InitRPC {
    @Resource
    CustomBeanFactory customBeanFactory;

    @PostConstruct
    public void init() throws Exception {
//        IFastMqService fastMqService = new FastMqServiceImpl();
        RpcFramework.export(IFastMqService.class, customBeanFactory.getBean("fastMqServiceImpl"), 3198);
//        ICacheMqService cacheMqService = new CacheMqServiceImpl();
        RpcFramework.export(ICacheMqService.class, customBeanFactory.getBean("cacheMqServiceImpl"), 3198);
//        FileMqServiceImpl fileMqService = new FileMqServiceImpl();
        RpcFramework.export(IFileMqService.class, customBeanFactory.getBean("fileMqServiceImpl"), 3198);
    }
}