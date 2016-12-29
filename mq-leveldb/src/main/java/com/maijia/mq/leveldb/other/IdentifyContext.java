package com.maijia.mq.leveldb.other;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * MJMQ身份标识
 *
 * @author panjn
 * @date 2016/12/16
 */
public class IdentifyContext {
    public static final String FILE_NAME = "identifyId";

    public static final String PROP_KEY_ID = "id";

    public static String getId() throws IOException {
        Properties data = AppDataContext.getInstance().createData(FILE_NAME);
        if (data.containsKey(PROP_KEY_ID)) {
            return data.getProperty(PROP_KEY_ID);
        }

        String id = UUID.randomUUID().toString();
        data.put(PROP_KEY_ID, id);
        AppDataContext.getInstance().saveData(FILE_NAME, data);
        return id;
    }
}
