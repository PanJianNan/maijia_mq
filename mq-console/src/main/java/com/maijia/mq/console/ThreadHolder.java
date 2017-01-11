package com.maijia.mq.console;

import com.maijia.mq.console.senior.AuthenticationFilter;
import com.maijia.mq.console.senior.LastSeniorFilter;
import com.maijia.mq.console.senior.ParameterFilter;
import org.jboss.netty.channel.MessageEvent;

import java.util.concurrent.ArrayBlockingQueue;

public class ThreadHolder {
	/**
	 * 请求线程
	 */
//	public static HttpRequestTask<MessageEvent> httpRequestThreadTask = new HttpRequestTask<MessageEvent>(
//			new ArrayBlockingQueue<MessageEvent>(1024 * 1024));
	
	public static AbstractTask<MessageEvent> httpRequestThreadTask = new HttpRequestTask<MessageEvent>(
			new ArrayBlockingQueue<MessageEvent>(1024 * 1024),new SeniorFilterChain(){
				
				public void createChain(){
					this.addToChain(1, new ParameterFilter());
					this.addToChain(2, new AuthenticationFilter());
					this.addToChain(3, new LastSeniorFilter());
				}
				
			});
/*	*//**
	 * 快队列线程
	 *//*
	public static AbstractTask<Object[]> quickThreadTask = new QueueTask<Object[]>(
			QueueTask.Type.QUICK_THREAD_POOL, Constants.QUICK_POOL_NUM,
			new ArrayBlockingQueue<Object[]>(1024*1024));
	*//**
	 * 慢队列线程
	 *//*
	public static AbstractTask<Object[]> slowThreadTask = new QueueTask<Object[]>(
			QueueTask.Type.SLOW_THREAD_POOL, Constants.SLOW_POOL_NUM,
			new ArrayBlockingQueue<Object[]>(1024*1024));
	*//**
	 * 写队列线程-单线程
	 *//*
	public static AbstractTask<Object[]> writeThreadTask = new QueueTask<Object[]>(
			QueueTask.Type.WRITE_THREAD_POOL, Constants.WRITE_POOL_NUM,
			new ArrayBlockingQueue<Object[]>(1024*1024));*/
	/**
	 * 
	 */
	public static void init(){
		
//		new Thread(httpRequestThreadTask).start();
		new Thread(httpRequestThreadTask).start();
//		new Thread(quickThreadTask).start();
//		new Thread(slowThreadTask).start();
//		new Thread(writeThreadTask).start();
	}
}
