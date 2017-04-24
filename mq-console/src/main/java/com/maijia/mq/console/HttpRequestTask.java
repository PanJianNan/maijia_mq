package com.maijia.mq.console;

import org.jboss.netty.channel.MessageEvent;

import java.util.concurrent.BlockingQueue;


public class HttpRequestTask<T extends MessageEvent> extends AbstractTask<T> {
	
	private FilterChain filterChain;
	
	public HttpRequestTask(BlockingQueue<T> queue){
		this.taskQueue=queue;
	}
	public HttpRequestTask(BlockingQueue<T> queue,FilterChain filterChain){
		this.taskQueue=queue;
		this.filterChain=filterChain;
	}
	
	@Override
	public void processTaskQueue() {
		while (true) {
			//从队列里面取出e
			T e;
			try {
				e = taskQueue.take();
				
				//高级过滤
//				Filter superiorFilter=Filter.getInstance();
//				superiorFilter.superFilter(e);
				filterChain.filter(e);
				
			} catch (InterruptedException e1) {
				logger.error(e1.getMessage());
			}
		}
	}

	@Override
	public void reNameThread() {
		
		if(thread==null){
			thread= Thread.currentThread();
		}
		thread.setName("HttpRequestThread:"+thread.getName());
	}
}
