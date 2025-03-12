package com.ntt.ntt.Service;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletion;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionMessage;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Service  // 이 클래스가 서비스 클래스임을 Spring에 알려주는 어노테이션
public class OpenAiServices {

    // OpenAiChatModel 객체 주입 (OpenAI의 채팅 모델 사용)
    @Autowired
    OpenAiChatModel openAiChatModel;

    // OpenAiApi 객체 주입 (OpenAI의 API와 상호작용)
    @Autowired
    OpenAiApi getOpenAiApi;

    // 이미지 설명 메서드
    public ChatResponse explainTheImage(String prompt, Resource image) {
        // 'prompt'는 사용자가 입력한 설명 요청, 'image'는 업로드된 이미지
        var userMessage = new UserMessage(prompt, List.of(new Media(MimeTypeUtils.IMAGE_PNG, image)));

        // Prompt 객체에 사용자 메시지와 이미지 데이터를 포함하여 OpenAI 모델에 요청
        return openAiChatModel.call(new Prompt(List.of(userMessage)));
    }

    // 일반적인 텍스트 쿼리 처리 메서드
    public ChatResponse answerQuery(String prompt) {
        // Prompt 객체에 쿼리 텍스트만 포함하여 OpenAI 모델에 요청
        ChatResponse response = openAiChatModel.call(new Prompt(prompt));

        // 주석 처리된 코드는 스트리밍 응답을 사용할 경우의 예시입니다.
        // Flux<ChatResponse> response = openAiChatModel.stream(new Prompt("Generate the names of 5 famous pirates."));

        // AI의 응답을 반환
        return response;
    }

    // 채팅 응답을 처리하는 메서드
    public ResponseEntity<ChatCompletion> chatResponse(String query) {
        // 시스템 메시지, 사용자 메시지 및 응답 메시지를 정의
        ChatCompletionMessage systemMsg = new ChatCompletionMessage("You're a friendly AI chatbot.", ChatCompletionMessage.Role.SYSTEM);
        ChatCompletionMessage userMsg1 = new ChatCompletionMessage("Hello Bot", ChatCompletionMessage.Role.USER);
        ChatCompletionMessage assistantMsg1 = new ChatCompletionMessage("Hello! I welcome you. Please ask your question.", ChatCompletionMessage.Role.ASSISTANT);
        ChatCompletionMessage userMsg2 = new ChatCompletionMessage(query, ChatCompletionMessage.Role.USER);

        // 요청을 작성 (대화의 맥락과 함께 요청)
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(
                List.of(systemMsg, userMsg1, assistantMsg1, userMsg2),  // 대화의 메시지들
                OpenAiApi.ChatModel.GPT_4_O.getValue(),  // GPT-4 모델 사용
                0.8,  // 생성될 응답의 창의성(0-1 범위)
                false  // 스트리밍 응답을 사용할지 여부
        );

        // API를 통해 채팅 응답을 요청
        return getOpenAiApi.chatCompletionEntity(chatCompletionRequest);
    }
}
