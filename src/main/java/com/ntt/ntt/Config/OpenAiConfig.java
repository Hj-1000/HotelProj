package com.ntt.ntt.Config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // 이 클래스는 Spring의 설정 클래스임을 나타냄
public class OpenAiConfig {

    @Value("${spring.ai.openai.chat.api-key}")  // application.properties 또는 application.yml 파일에서 API 키를 가져옴
    String openAiApiKey;

    // OpenAiChatModel Bean을 생성하는 메서드
    @Bean
    public OpenAiChatModel openAiChatModel() {
        // OpenAiApi 객체와 OpenAiChatOptions 객체를 사용하여 OpenAiChatModel을 생성
        return new OpenAiChatModel(
                new OpenAiApi(openAiApiKey),  // OpenAiApi 객체는 API 키를 통해 OpenAI API와 상호작용
                OpenAiChatOptions.builder()   // OpenAiChatOptions 설정 (온도, 모델 설정 등)
                        .withTemperature(0.7)  // 모델의 창의성(0-1 범위), 0.7은 창의적이면서도 일관성 있는 응답을 의미
                        .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())  // 사용할 OpenAI 모델(GPT-4)을 설정
                        .build()  // 설정 완료된 OpenAiChatOptions 객체 반환
        );
    }

    // OpenAiApi Bean을 생성하는 메서드
    @Bean
    public OpenAiApi getOpenAiApi() {
        // OpenAiApi 객체를 생성하여 API 키를 사용해 OpenAI API와 상호작용
        return new OpenAiApi(openAiApiKey);
    }
}
