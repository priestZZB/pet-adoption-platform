package com.pet.framework.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在 Controller 类上，该类的 POST/PUT/DELETE 操作会自动记录操作日志
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 模块名称，如"用户管理""宠物管理"
     */
    String value() default "";
}
