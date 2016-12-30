package com.maijia.mq.service;

/**
 * 对外提供的消息队列服务接口（默认模式/文件模式）
 * 优点：占用内存最小，消息落体不丢失
 * 缺点：速度最慢，暂不支持分布式
 *
 * @author panjn                   
 * @date 2016/10/26
 */
public interface IFileMqService extends IMqService {
}
