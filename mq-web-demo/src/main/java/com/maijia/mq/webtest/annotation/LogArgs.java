package com.maijia.mq.webtest.annotation;

import java.lang.annotation.*;

/**
 * Created by panjiannan on 2018/7/30.
 */
@Inherited
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface LogArgs {
}
