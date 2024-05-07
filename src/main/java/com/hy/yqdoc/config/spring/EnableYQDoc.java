package com.hy.yqdoc.config.spring;

import com.hy.yqdoc.config.DocConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启YQDoc的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DocConfig.class)
public @interface EnableYQDoc {

}
