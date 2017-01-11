package com.maijia.mq.console.senior;

import com.maijia.mq.console.AbstractFilter;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class LastSeniorFilter extends AbstractFilter {

	@Override
	public CheckResult filter(Object... inParam) {

		this.pushQueue(inParam);
		return CheckResult.OK;
	}

	/**
	 * 将paramMap(所有参数信息)和e压入快慢队列
	 * 
	 * @param
	 * @param
	 */
	private void pushQueue(Object... inParam) {

		MessageEvent e = (MessageEvent) inParam[0];
		HttpRequest request = (HttpRequest) e.getMessage();
		String uri = request.getUri();
//		if (Constants.WRITE_POOL_URI.contains(uri)) {// 如果是写操作,则压入写队列
//			ThreadHolder.writeThreadTask.registerTask(inParam);
//		} else if (Constants.SLOW_POOL_URI.contains(uri)) {// 如果不是报表,则压入快队列
//			ThreadHolder.slowThreadTask.registerTask(inParam);
//		} else {// 压入快队列
//			ThreadHolder.quickThreadTask.registerTask(inParam);
//		}
	}
}
