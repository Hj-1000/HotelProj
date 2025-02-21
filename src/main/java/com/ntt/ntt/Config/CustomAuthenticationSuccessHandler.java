package com.ntt.ntt.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

//사용자 인증 성공처리 핸들러
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 로그인 전 사용자가 보고 있는 페이지 저장
        String redirectUrl = request.getHeader("Referer");

        //로그인에 사용한 정보를 읽어와서 저장
        //UserDetails을 사용자가 오버라이딩 변경을 하면 해당정보를 전달
        UserDetails user = (UserDetails) authentication.getPrincipal();

        String role = authentication.getAuthorities().toArray()[0].toString(); // 첫 번째 권한 값 가져오기

        if (role.equals("ROLE_ADMIN")) {
            response.sendRedirect("/admin/memberList"); // ADMIN 권한 사용자가 로그인시 전체회원관리 페이지로 리다이렉트
        } else if (role.equals("ROLE_CHIEF")) {
            response.sendRedirect("/admin/executiveList"); // CHIEF 권한 사용자가 로그인시 임원관리 페이지로 리다이렉트
        } else if (role.equals("ROLE_MANAGER")) {
            response.sendRedirect("/manager/room/list"); // MANAGER 권한 사용자가 로그인시 객실관리 페이지로 리다이렉트
        } else if (role.equals("ROLE_USER")) {
            // USER 권한 사용자가 로그인시 메인페이지가 아닌 원래 보고 있던 페이지로 리다이렉트
            if (redirectUrl != null && !redirectUrl.contains("/login")) {
                response.sendRedirect(redirectUrl); // 사용자가 로그인 전에 보고 있던 페이지로 리다이렉트
            } else {
                response.sendRedirect("/"); // 로그인 후 기본 메인페이지로 리다이렉트
            }
        } else {
            response.sendRedirect("/login?error"); // 기본 로그인 페이지로 리다이렉트
        }
    }
}
