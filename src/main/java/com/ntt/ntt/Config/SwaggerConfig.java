package com.ntt.ntt.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "게시판",
                description = "게시판 API 명세서",
                version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    //1개의 작업그룹 메소드명을 변경하여 여러 그룹 지정가능
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/**"}; //모든 경로 대상

        return GroupedOpenApi.builder()
                .group("게시판 API v1")
                .packagesToScan("com.ntt.ntt.Controller") // 패키지내의 모든 Controller 지정
                //.pathsToMatch(paths)
                .build();
    }

    @Bean
    //1개의 작업그룹 메소드명을 변경하여 여러 그룹 지정가능
    public GroupedOpenApi chatOpenApi2() {
        String[] paths = {"/list", "/insert"}; //특정 맵핑만 처리 모든 경로 대상(/**)

        return GroupedOpenApi.builder()
                .group("게시판 API v2")
                .pathsToMatch(paths)
                .build();
    }
}
/*
SpringBoot 3.x이하인 경우 작성

package com.example.board.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
        return new Docket(DocumentationType.OAS_30)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.board.Controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger 문서 제목")
                .description("Swagger 문서 설명")
                .version("1.0")
                .build();
    }
}
*/