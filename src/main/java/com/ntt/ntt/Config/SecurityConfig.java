package com.ntt.ntt.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

//2. 보안권한 설정, 암호화, 로그인, 로그아웃, csrf
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Spring Security 로그인시 발생하는 에러들을 별도의 Exception 으로 처리하기 위해 추가
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) { //아이디가 맞지 않을 때 UserNotFoundException을 발생하게 하는 메소드
        DaoAuthenticationProvider bean = new DaoAuthenticationProvider();
        bean.setHideUserNotFoundExceptions(false);
        bean.setUserDetailsService(userDetailsService);
        bean.setPasswordEncoder(passwordEncoder());

        return bean;
    }

    //비밀번호를 암호화처리
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //맵핑에 접근제한
    //static에 있는 폴더를 모두 사용으로 지정
    //html은 작업에 따라 사용할 권한 부여
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((auth)->
        { //각 자원, html 권한
            //모두 접근 가능
            //세부페이지 또는 Controller가 완성되면 삭제
            auth.requestMatchers("/**").permitAll(); //모든 매핑 허용
            auth.requestMatchers("/h2-console/**").permitAll(); //모든 매핑 허용
            // permitAll(); 모든 사용자 사용가능
            auth.requestMatchers("/assets/**", "/css/**", "/js/**").permitAll();
            auth.requestMatchers("/").permitAll(); //메인페이지
            auth.requestMatchers("/login").permitAll(); //로그인

            //인증된 사용자만 접근 가능 - 추후 프로젝트 완성 됐을 때 주석 해제
            auth.requestMatchers("/logout").authenticated(); //로그아웃
//            auth.requestMatchers("/reservation/**").authenticated(); //호텔 예약 관련
//            auth.requestMatchers("/favorite/**").authenticated(); //호텔 즐겨찾기 관련
//            auth.requestMatchers("/user/**").authenticated(); //회원 관련(모든회원)
//            auth.requestMatchers("/admin/**").hasRole("ADMIN"); //관리자 전용
//            auth.requestMatchers("/chief/**").hasRole("CHIEF"); //호텔장 전용
//            auth.requestMatchers("/manager/**").hasRole("MANAGER"); //호텔매니저 전용
        });

        // 로그인 실패 처리 핸들러
        AuthenticationFailureHandler failureHandler = (request, response, exception) -> {
            // exception이 UsernameNotFoundException인지 확인
            if (exception instanceof UsernameNotFoundException) {
                // 비활성화된 계정일 경우
                String errorMessage = exception.getMessage();
                request.getSession().setAttribute("loginError", errorMessage);
            } else {
                // 다른 로그인 실패 오류 메시지
                request.getSession().setAttribute("loginError", "로그인에 실패했습니다.%%%이메일과 비밀번호를 확인해주세요.");
            }
            response.sendRedirect("/login?error"); // 로그인 실패 시 로그인 페이지로 리다이렉트
        };

        //로그인 정보
        http.formLogin(login->login
            .loginPage("/login") //로그인은 /login맵핑으로
            .defaultSuccessUrl("/") //로그인 성공시 / 페이지로 이동
            .usernameParameter("memberEmail") //memberEmail을 username으로 사용
            .permitAll() //모든 사용자가 로그인폼 사용
            .failureHandler(failureHandler) // 로그인 실패 시 처리할 핸들러 설정
            .successHandler(new CustomAuthenticationSuccessHandler())); //로그인 성공시처리할 클래스

        //csrf 변조방지
        http.csrf(AbstractHttpConfigurer::disable);

        //로그아웃 정보
        http.logout(logout->logout
            .logoutUrl("/logout") //로그아웃 맵핑
            .logoutSuccessUrl("/login") //로그아웃 성공시 로그인 페이지로 이동
        );

        return http.build();
    }
}
