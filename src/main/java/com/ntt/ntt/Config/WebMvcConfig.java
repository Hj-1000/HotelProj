package com.ntt.ntt.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}") // application.properites 에 선언된 uploadPath
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){

        // 만약에 /upload/** 호출이 오면 file://c:/data 로 연결
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(uploadPath);

    }

}
