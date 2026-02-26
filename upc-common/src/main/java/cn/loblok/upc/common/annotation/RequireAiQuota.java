package cn.loblok.upc.common.annotation;

import cn.loblok.upc.common.enums.AppMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAiQuota {
    // 传入 SpEL 表达式，例如 "#req.mode"
    String mode() default "";
}