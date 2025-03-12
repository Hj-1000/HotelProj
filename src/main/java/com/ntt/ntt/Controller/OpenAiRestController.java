package com.ntt.ntt.Controller;

import com.ntt.ntt.Service.OpenAiServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController  // 이 클래스가 REST API 컨트롤러임을 나타냅니다.
@RequestMapping("/ai")  // 이 컨트롤러는 "/ai" 경로로 들어오는 요청을 처리합니다.
@Tag(name = "OpenAiRestController", description = "OpenAI 관련 컨트롤러")
public class OpenAiRestController {

    private final OpenAiServices openAiServices;  // OpenAiServices 객체를 생성자 주입 방식으로 선언합니다.

    // 생성자 주입을 통해 OpenAiServices를 의존성 주입합니다.
    @Autowired
    public OpenAiRestController(OpenAiServices services) {
        this.openAiServices = services;  // 서비스 객체를 초기화
    }

    // 이미지 설명 API - '/ai/image' 경로로 이미지 파일을 보내면 이미지에 대한 설명을 반환합니다.
    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 설명 API", description = "이미지에 대한 설명을 생성한다.")
    public @ResponseBody ChatResponse explainImage(
            @RequestParam(defaultValue = "이미지를 설명해줘", name = "prompt") String prompt,  // 사용자가 입력한 프롬프트. 기본값은 '이미지를 설명해줘'
            @RequestPart(required = true, name = "image") MultipartFile image  // 업로드된 이미지 파일
    ) {
        // OpenAiServices의 explainTheImage 메서드를 호출하여 이미지에 대한 설명을 생성합니다.
        return this.openAiServices.explainTheImage(prompt, image.getResource());
    }

    // 일반적인 텍스트 쿼리 API - '/ai/query' 경로로 텍스트 쿼리를 보내면 해당 쿼리에 대한 응답을 반환합니다.
    @PostMapping(path = "/query")
    @Operation(summary = "쿼리 API", description = "쿼리에 대한 응답을 생성한다.")
    public @ResponseBody ChatResponse query(
            @RequestParam(defaultValue = "인기있는 호텔은", name = "query") String prompt  // 쿼리 매개변수. 기본값은 '인기있는 호텔은'
    ) {
        // OpenAiServices의 answerQuery 메서드를 호출하여 주어진 텍스트 쿼리에 대한 답변을 받습니다.
        return this.openAiServices.answerQuery(prompt);
    }

    // 채팅 API - '/ai/chat' 경로로 텍스트 기반의 채팅 쿼리를 보내면 AI의 채팅 응답을 받습니다.
    @PostMapping(path = "/chat")
    @Operation(summary = "채팅 API", description = "채팅에 대한 응답을 생성한다.")
    public @ResponseBody ResponseEntity<OpenAiApi.ChatCompletion> chatQuery(
            @RequestParam(defaultValue = "인기있는 호텔은", name = "query") String query  // 채팅 쿼리. 기본값은 '인기있는 호텔은'
    ) {
        // OpenAiServices의 chatResponse 메서드를 호출하여 주어진 쿼리에 대한 채팅 응답을 반환합니다.
        return this.openAiServices.chatResponse(query);
    }
}
