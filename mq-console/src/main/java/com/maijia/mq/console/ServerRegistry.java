package com.maijia.mq.console;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * ServerRegistry
 *
 * @author panjn
 * @date 2019/2/3
 */
@Component
public class ServerRegistry {

    // ZooKeeper 服务地址, 单机格式为:(127.0.0.1:2181), 集群格式为:(127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183)
    private String connectString;
    // Curator 客户端重试策略
    private RetryPolicy retry;
    // Curator 客户端对象
    private CuratorFramework client;

    @PostConstruct
    public void init () {
        // 设置 ZooKeeper 服务地址为本机的 2181 端口
        //connectString = "127.0.0.1:2181";
        connectString = MqServerConfig.ZK_CONNECT;
        // 重试策略
        // 初始休眠时间为 1000ms, 最大重试次数为 3
        retry = new ExponentialBackoffRetry(1000, 3);

        //为了测试代码, 我们需要下载、安装、启动一个 ZooKeeper 服务, 然后将该服务地址配置为 connectString.
        //但是如果更换环境的话又需要重新安装, 未免麻烦了点.
        // Curator 为我们提供一个专门用于开发、测试的便捷方法, 让我们更加专注于编写与 ZooKeeper 相关的程序.
        //首先需要导入 curator-test 测试包
        //==========curator-test=========
//        //会获取一个空闲端口, 同时在 java.io.tmpdir 创建一个临时目录当作本次的 dataDir 目录
//        TestingServer server= new TestingServer();
//        // server.getConnectString() 方法会返回可用的服务链接地址, 如: 127.0.0.1:2181
//        client = CuratorFrameworkFactory.newClient(server.getConnectString(), retry);
//        client2 = CuratorFrameworkFactory.newClient(server.getConnectString(), retry);
        //==========curator-test=========

        //==========正常测试=========
        // 创建一个客户端, 60000(ms)为 session 超时时间, 15000(ms)为链接超时时间
        client = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retry);
        client.start();
    }

    public CuratorFramework getClient() {
        return client;
    }
}
