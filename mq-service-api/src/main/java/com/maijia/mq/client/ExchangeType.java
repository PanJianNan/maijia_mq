package com.maijia.mq.client;

/**
 * ExchangeType enum
 *
 * @author panjn
 * @date 2016/12/28
 */
public enum ExchangeType {

    DIRECT("direct", "精确匹配"), FANOUT("fanout", "广播"), TOPIC("topic", "主题");

    private String type;
    private String desc;

    ExchangeType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
