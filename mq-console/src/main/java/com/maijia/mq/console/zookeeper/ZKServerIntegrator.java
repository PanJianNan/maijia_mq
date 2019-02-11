package com.maijia.mq.console.zookeeper;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.console.MqServerConfig;
import com.maijia.mq.console.ServerRegistry;
import com.maijia.mq.constant.ZKConstant;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;

/**
 * ZKServerIntegrator
 *
 * @author panjn
 * @date 2019/2/2
 */
@Component
public class ZKServerIntegrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKServerIntegrator.class);

    @Resource
    private ServerRegistry serverRegistry;

    @PostConstruct
    public void init() throws Exception {
        if (MqServerConfig.ZK_INTEGRATION) {
            LOGGER.info("start init ZKServerIntegrator");

            URL url = ZKServerIntegrator.class.getClassLoader().getResource("zookeeper" + File.separator + "zoo.cfg");
            String zkCfgPath = url.getPath();

            new Thread(() -> {
                try {
                    start(zkCfgPath);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }, "zk-intergrator-thread").start();

            LOGGER.info("end init ZKServerIntegrator");
        }

        // 参与master选举
        String leaderSelectorPath = ZKConstant.MASTER_SELECT_PATH;
        final LeaderLatch leaderLatch = new LeaderLatch(serverRegistry.getClient(), leaderSelectorPath, "server#" + 1);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                System.out.println(leaderLatch.getId() + ":I am leader. I am doing jobs!");
                try {
                    System.out.println(JSONObject.toJSONString(leaderLatch.getParticipants()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址
                    serverRegistry.getClient().setData().forPath(leaderSelectorPath, address.getHostAddress().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void notLeader() {
                System.out.println(leaderLatch.getId() + ":I am not leader. I will do nothing!");
            }
        });
        leaderLatch.start();

    }


    /**
     * 启动集成的zookeeper服务
     *
     * @param zkCfgPath zoo.cfg文件的物理路径
     * @throws Exception
     */
    public void start(String zkCfgPath) throws Exception {
        //加载zoocfg配置文件
        Properties prop = loadProperties(zkCfgPath);
        //提取本机服务的server编号，这个my.id是默认的zoo.cfg里没有的，需要我们后加上，
        //它的值就是当前节点的serverNum
        String serverNum = prop.getProperty("my.id");
        //提取zookeeper的客户端IP和端口，把IP和端口提取出来，方便我们的客户端API使用
//        Global.zkClientIp = prop.getProperty("server." + serverNum).split(":")[0];
//        Global.zkClientPort = Integer.parseInt(prop.getProperty("clientPort"));
        //myid文件的路径
        String dataDir = prop.getProperty("dataDir");
        //写入myid文件
        writeMyid(dataDir, serverNum);
        prop.setProperty("dataDir", dataDir);
        //将dataDir保存到zoo.cfg
        saveConfig(prop, zkCfgPath);
        String[] config = {zkCfgPath};
        Class<?> clazz = Class.forName("org.apache.zookeeper.server.quorum.QuorumPeerMain");
        Method main = clazz.getMethod("main", String[].class);
        //启动zookeeper
        main.invoke(null, (Object) config);
    }

    /**
     * 保存zookeeper的配置文件
     *
     * @param prop
     * @param zkCfgPath
     * @throws IOException
     */
    private void saveConfig(Properties prop, String zkCfgPath) throws IOException {
        OutputStream out = new FileOutputStream(zkCfgPath);
        try {
            prop.store(out, null);
        } finally {
            if (out != null) out.close();
        }
    }

    /**
     * 将server的编号写入myid文件
     *
     * @param dataDir
     * @param serverNum
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void writeMyid(String dataDir, String serverNum)
            throws IOException {
        File dir = new File(dataDir);
        if (!dir.exists()) dir.mkdirs();
        File myid = new File(dataDir + "/myid");
        if (!myid.exists()) myid.createNewFile();
        OutputStream out = new FileOutputStream(myid);
        try {
            out.write(serverNum.getBytes());
        } finally {
            if (out != null) out.close();
        }
    }

    /**
     * 加载zoocfg配置
     *
     * @param zkCfgPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Properties loadProperties(String zkCfgPath) throws IOException {
        Properties prop = new Properties();
        InputStream is = new FileInputStream(zkCfgPath);
        try {
            prop.load(is);
        } finally {
            if (is != null) is.close();
        }
        return prop;
    }

}

