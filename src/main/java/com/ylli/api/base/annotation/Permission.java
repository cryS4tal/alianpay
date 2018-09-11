package com.ylli.api.base.annotation;

/**
 * Created by ylli on 2017/2/10.
 */
public @interface Permission {
    /**
     * 权限数组，满足所有权限则鉴权通过
     */
    long[] value() default {};
}
