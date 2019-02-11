package com.maijia.mq.client.zookeeper;

import com.maijia.mq.client.ClientRegistry;
import com.maijia.mq.client.Exchanges;
import com.maijia.mq.client.MqClientConfig;
import com.maijia.mq.constant.ZKConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * ZKConfigLoad
 *
 * @author panjn
 * @date 2019/2/9
 */
@Component
public class ZKConfigLoad {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKConfigLoad.class);

    @Resource
    private ClientRegistry clientRegistry;

    @PostConstruct
    public void init() {
        String path = ZKConstant.MASTER_SELECT_PATH;
        CuratorFramework client = clientRegistry.getClient();
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                client.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path);
            } else {
                byte[] masterHostBytes = client.getData().forPath(path);
                MqClientConfig.HOST = new String(masterHostBytes, "UTF-8");
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
//            client.getCuratorListenable().addListener();
            final NodeCache cache = new NodeCache(client, path);
            NodeCacheListener listener = () -> {
                ChildData data = cache.getCurrentData();
                if (data != null) {
                    byte[] masterHostBytes = data.getData();
                    String masterHost = new String(masterHostBytes, "UTF-8");
                    if (!masterHost.equals(MqClientConfig.HOST)) {
                        MqClientConfig.HOST = masterHost;
                        Exchanges.reset();
                    }
                } else {
                    LOGGER.info("节点被删除!");
                }

            };
            cache.getListenable().addListener(listener);
            try {
                cache.start();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
//            cache.close();
        }

    }
}
