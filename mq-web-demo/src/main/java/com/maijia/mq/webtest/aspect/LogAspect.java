package com.maijia.mq.webtest.aspect;

import com.alibaba.fastjson.JSONArray;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by panjiannan on 2018/7/30.
 */
@Aspect
@Component
public class LogAspect {
    @PostConstruct
    public void init() {
        System.err.println("LogAspect init $$$$$$$");
    }

    @Pointcut(value = "execution(public * com.maijia.mq.webtest.controllers..*(..))")
    public void logPoint() {

    }

    @Before(value = "logPoint()")
    public void before() {
        System.out.println("准备执行方法！");
    }

    @Around(value = "logPoint()")
    public Object costtatistics(ProceedingJoinPoint proceedingJoinPoint) {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println(proceedingJoinPoint.getSignature().getName() + " cost:" + (System.currentTimeMillis()-startTime) + "ms");
        return result;
    }

//    @Pointcut(value = "execution(public * com.maijia.mq.webtest.controllers.aop.*.*(..))")
//    public void argsPoint() {
//
//    }

    @Before(value = "@annotation(com.maijia.mq.webtest.annotation.LogArgs)")
    public void beforeArgs(JoinPoint joinPoint) {
        System.err.println("args:" + JSONArray.toJSONString(joinPoint.getArgs()));
    }


}
