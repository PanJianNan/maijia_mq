package com.maijia.mq.console;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;


public class RequestFilter extends AbstractFilter {

	@Override
	public CheckResult filter(Object... inParam) {
		if(!ServerManager.isServerStart()){
			return CheckResult.NOT_STARTSERVER;
		}
		MessageEvent e=(MessageEvent)inParam[0];
		HttpRequest request = (HttpRequest) e.getMessage();
		if(request.getMethod() == HttpMethod.POST){
			if (request.getUri() == null || "/".equals(request.getUri())||request.getUri().contains("//")) {
				return CheckResult.ILLEGAL_URI;
			}else{
				// deliver to the next filter
				return getSuccessor().filter(inParam);
			}
		}else {
			return CheckResult.NONE_HTTPREQUEST_POST;
		}
	}

}
