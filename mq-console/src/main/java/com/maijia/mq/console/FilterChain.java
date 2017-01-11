package com.maijia.mq.console;

import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;


public abstract class FilterChain {

    final protected static Logger log = Logger.getLogger(FilterChain.class);

    private TreeMap<Integer, AbstractFilter> sortedFilter = new TreeMap<Integer, AbstractFilter>();

    private AbstractFilter firstFilter = null;

    public FilterChain() {
        createChain();
    }

    public abstract void createChain();

    public void addToChain(Integer order, AbstractFilter filter) {

        if (sortedFilter.size() == 0) {
            firstFilter = filter;
        }

        sortedFilter.put(order, filter);

        if (sortedFilter.size() > 1) {

            Object keys[] = sortedFilter.keySet().toArray();
            sortedFilter.get(keys[keys.length - 2]).setSuccessor(filter);
        }
    }

    public void filter(Object requestModel) {
        try {
            AbstractFilter.CheckResult returnType = firstFilter.filter(requestModel);
            processReturn((MessageEvent) requestModel, returnType);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public abstract void processReturn(MessageEvent e, AbstractFilter.CheckResult type);
}
