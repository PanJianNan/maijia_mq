package com.maijia.mq.rpc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * CustomBeanFactory
 *
 * @author panjn
 * @date 2017/1/3
 */
@Component
public class CustomBeanFactory implements BeanFactoryAware {
    /** Spring's bean factory */
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        beanFactory = factory;
    }

    public <T> T getBean(String beanName) {
        return (T) beanFactory.getBean(beanName);
    }

}
