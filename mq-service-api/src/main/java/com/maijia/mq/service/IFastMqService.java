package com.maijia.mq.service;

/**
 * 对外提供的消息队列服务接口（快模式）
 * 优点：速度最快，消息不落地
 * 缺点：占用内存大；一旦宕机，消息会丢失；暂不支持分布式
 *
 * @author panjn
 * @date 2016/10/26
 */
public interface IFastMqService extends IMqService {
}
