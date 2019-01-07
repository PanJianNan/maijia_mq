package com.maijia.mq.webtest.controllers.aop;

import com.maijia.mq.webtest.annotation.LogArgs;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * Created by panjiannan on 2018/7/30.
 */
@RestController
@RequestMapping("/aop")
public class AopTestController {

    @PostConstruct
    public void init() {
        System.out.println("AopTestController init!!!!!!!!!!");
    }

    @RequestMapping("test/{msg}")
    public String test(@PathVariable String msg) {
        System.out.println("test got msg! :" + msg);
        return "OK!";
    }

    @RequestMapping("test2/{msg}")
    @LogArgs
    public String test2(@PathVariable String msg) {
        System.out.println("test2 got msg! :" + msg);
        return "OK!";
    }

}
