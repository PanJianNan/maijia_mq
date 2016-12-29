package com.maijia.mq.service;

/**
 * 对外提供的消息队列服务接口（缓存模式）
 * 优点：速度中，消息由缓存中间件保存，不易丢失，支持分布式
 * 缺点：占用内存大；可能占用大量缓存中间件资源
 *
 * @author panjn
 * @date 2016/10/26
 */
public interface ICacheMqService<E> extends IMqService<E> {
}
