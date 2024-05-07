package com.hy.yqdoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Configuration
@ComponentScan(basePackages = "com.hy.yqdoc")
public class DocConfig {

    @Bean(name="viewResolver")
    public ViewResolver getViewResolver(){

        FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
        viewResolver.setCache(true);
        viewResolver.setPrefix("/");
        // 重要： 没有这一项，请求页面中文会有乱码
        viewResolver.setContentType("text/html;charset=utf-8");
        viewResolver.setSuffix(".ftl");
        viewResolver.setOrder(0);

        return viewResolver;
    }

    @Bean(name="freemarkerConfig")
    public FreeMarkerConfigurer getFreeMarkerConfig() {
        FreeMarkerConfigurer config = new FreeMarkerConfigurer();

        config.setDefaultEncoding("UTF-8");
        // 包含 FreeMarker模板的文件夹
        config.setTemplateLoaderPath("classpath:/templates");
        // 必须加上这句，否则无法读到jar中的freemarker模板
        config.setPreferFileSystemAccess(false);
        return config;
    }

}
