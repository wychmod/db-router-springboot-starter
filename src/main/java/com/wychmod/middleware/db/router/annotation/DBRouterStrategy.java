package com.wychmod.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @description: 路由策略，分表标记
 * @author: wychmod
 * @date: 2022/11/1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;
}
