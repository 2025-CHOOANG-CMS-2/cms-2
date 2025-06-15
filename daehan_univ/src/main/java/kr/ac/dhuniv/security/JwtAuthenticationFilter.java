package kr.ac.dhuniv.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * ✅ JwtAuthenticationFilter 클래스
 * - Spring Security FilterChain에서 매 요청마다 JWT 토큰을 검사
 * - 유효한 토큰이면 SecurityContext에 인증 정보 설정
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // ✅ JwtTokenProvider 의존성 주입 (토큰 생성/검증/Authentication 제공)
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ✅ doFilterInternal 메소드
     * - Spring Security의 OncePerRequestFilter의 핵심 메소드
     * - 요청마다 실행되며 JWT를 검사하고 인증 정보를 SecurityContext에 등록
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ Authorization 헤더에서 토큰 값 추출
        String authHeader = request.getHeader("Authorization"); // 헤더에서 Authorization 값 가져오기
        String token = null; // 추출된 토큰 값 초기화

        // ✅ Authorization 헤더가 존재하고 "Bearer "로 시작하는 경우 토큰 부분만 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // "Bearer " 이후의 토큰 문자열만 추출
        }

        // ✅ 토큰이 존재하고, 유효한 경우
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // JwtTokenProvider로부터 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    (UsernamePasswordAuthenticationToken) jwtTokenProvider.getAuthentication(token);

            // 인증 객체에 상세 요청 정보를 추가 (IP, 세션 ID 등)
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // ✅ SecurityContext에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ✅ 다음 필터로 요청 전달 (필터 체인 진행)
        filterChain.doFilter(request, response);
    }
}