package com.pet.framework.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 *
 * 标注在 Controller 类或方法上，指定访问所需角色（满足其一即可）。
 *
 * 用法：
 * <pre>
 * &#64;RequireRole("ADMIN")
 * &#64;RequireRole({"ADMIN", "VOLUNTEER"})
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    String[] value();
}