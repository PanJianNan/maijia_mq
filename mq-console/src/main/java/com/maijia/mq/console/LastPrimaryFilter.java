package com.maijia.mq.console;

import org.jboss.netty.channel.MessageEvent;

import static com.maijia.mq.console.ThreadHolder.httpRequestThreadTask;


public class LastPrimaryFilter extends AbstractFilter {

    @Override
    public CheckResult filter(Object... inParam) {

        httpRequestThreadTask.registerTask((MessageEvent) inParam[0]);

        return CheckResult.OK;
    }

}
