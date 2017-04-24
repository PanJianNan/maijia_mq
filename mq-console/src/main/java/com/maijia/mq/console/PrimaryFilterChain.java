package com.maijia.mq.console;

import com.maijia.mq.console.AbstractFilter.CheckResult;
import org.jboss.netty.channel.MessageEvent;

public class PrimaryFilterChain extends FilterChain {

    private static PrimaryFilterChain primaryFilterChain = null;

    public synchronized static PrimaryFilterChain getInstance() {
        if (primaryFilterChain != null) {
            return primaryFilterChain;
        } else {
            primaryFilterChain = new PrimaryFilterChain();
            primaryFilterChain.createChain();
            return primaryFilterChain;
        }
    }

    @Override
    public void createChain() {
        this.addToChain(1, new RequestFilter());
        this.addToChain(2, new LastPrimaryFilter());
    }

    @Override
    public void processReturn(MessageEvent e, CheckResult type) {
        String returnMsg;
        switch (type) {
            case OK:
                return;
            case NOT_STARTSERVER:
//			returnMsg=JsonUtil.stringToJson(Constants.NOT_STARTSERVER,"服务器还未启动完毕！", false);
                returnMsg = "服务器还未启动完毕!";
                break;
            case NONE_HTTPREQUEST_POST:
//			returnMsg=JsonUtil.stringToJson(Constants.NONE_POST_HTTPREQUEST,"仅支持HTTP_POST类型请求！", false);
                returnMsg = "仅支持HTTP_POST类型请求！";
                break;
            case ILLEGAL_URI:
//			returnMsg=JsonUtil.stringToJson(Constants.ILLEGAL_URI,"uri为空或含有非法字符！", false);
                returnMsg = "uri为空或含有非法字符！";
                break;
            default:
//			returnMsg=JsonUtil.stringToJson("","出现错误！", false);
                returnMsg = "出现错误！";
                break;
        }
        HttpServer.writeResponse(e, returnMsg);
    }

}
