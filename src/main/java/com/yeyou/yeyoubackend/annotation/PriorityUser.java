package com.yeyou.yeyoubackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PriorityUser {
    /**
     * 必须是某个角色
     */
    int mustRole() default -1;

    /**
     * 必须是角色之一
     */
    int[] anyRole() default {-1};
}
