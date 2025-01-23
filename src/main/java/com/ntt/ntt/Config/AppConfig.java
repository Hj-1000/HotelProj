package com.ntt.ntt.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // 비동기 메서드(Async) 를 사용하기 위해 추가 -> 비밀번호 찾기를 위한 이메일 발송 속도가 느려서 비동기 메서드 사용
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
