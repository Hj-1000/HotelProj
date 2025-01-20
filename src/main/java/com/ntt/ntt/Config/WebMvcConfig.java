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
        // file://c:/data/ 경로에서 file:/// 경로로 시작해야 하고, 실제 파일이 있는 폴더를 읽어올 수 있게 설정
        String filePath = uploadPath.replace("file://", "");
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + filePath);  // file:// 로 시작하는 경로로 설정
    }

}
