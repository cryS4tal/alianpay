package com.ylli.api.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于用户登录鉴权和权限访问控制.
 * 标注于RestController的类或方法上
 * 如果方法有标记以方法为准，忽略类标记
 *
 * Created by ylli on 2017/2/10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Auth {

    /**
     * 权限数组，用户满足其中一个权限则鉴权通过
     */
    Permission[] value() default {};
}
