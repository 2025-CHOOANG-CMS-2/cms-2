package kr.ac.dhuniv.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ JwtAuthenticationFilter 클래스
 * - Spring Security의 OncePerRequestFilter를 상속
 * - 매 요청마다 쿠키에서 JWT를 추출 → 유효성 검사 → SecurityContextHolder에 인증정보 등록
 * - 인증된 사용자임을 Spring Security가 인식하도록 도와줌
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 발급 및 검증을 담당하는 JwtTokenProvider를 주입받음
    private final JwtTokenProvider jwtTokenProvider;




    /**
     * ✅ doFilterInternal 메서드
     * - 요청마다 실행되며, SecurityContext에 인증 정보를 채우는 핵심 메서드
     *
     * @param request  클라이언트 HTTP 요청 객체
     * @param response 클라이언트 HTTP 응답 객체
     * @param filterChain 나머지 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // JWT 필터에서 logout URL 은 skip
        if (request.getRequestURI().equals("/api/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 🔹 요청에서 쿠키 기반 JWT 토큰 추출
        String token = resolveToken(request);  // 쿠키 이름이 AUTH_TOKEN인 값을 가져옴

        // 🔹 토큰이 존재하고, 서명/만료 등 검증에 통과했을 때만 인증 수행
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // JWT의 Claims (사용자 ID, 권한 등 Payload 데이터) 추출
            var claims = jwtTokenProvider.parseToken(token).getBody();

            // 🔹 roles 클레임에 저장된 권한 목록을 SimpleGrantedAuthority 리스트로 변환
            List<SimpleGrantedAuthority> authorities = ((List<?>) claims.get("roles")).stream()
                    .map(role -> {
                        String roleStr = (String) role;
                        // ROLE_ 접두사 붙이기 (Spring Security는 ROLE_XXX 형태를 기대)
                        return new SimpleGrantedAuthority("ROLE_" + roleStr);
                    })
                    .collect(Collectors.toList());

            // 🔹 인증 객체 생성
            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),    // 사용자 ID (JWT subject 값)
                    null,                   // 비밀번호는 null (JWT 기반 인증은 비밀번호 사용하지 않음)
                    authorities             // 권한 정보
            );

            // 🔹 SecurityContextHolder에 인증 객체 저장 → Spring Security가 인증된 사용자로 인식
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 디버그용 로그 출력
            System.out.println("✅ 인증 정보 설정 완료: " + claims.getSubject());
        } else {
            // 토큰이 없거나 유효하지 않은 경우 (예: 서명 실패, 만료 등)
            System.out.println("❌ 유효하지 않은 토큰 또는 토큰 없음");
        }

        // 🔹 다음 필터로 요청 전달 (체인의 다음 필터로 넘어가야 응답이 정상 동작)
        filterChain.doFilter(request, response);
    }

    /**
     * ✅ resolveToken 메서드
     * - HTTP 요청의 쿠키 배열에서 AUTH_TOKEN 이름의 쿠키 값을 추출
     *
     * @param request 클라이언트 HTTP 요청
     * @return JWT 문자열 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        // 쿠키 배열이 null이 아닌지 확인
        if (request.getCookies() != null) {
            // 모든 쿠키 순회
            for (Cookie cookie : request.getCookies()) {
                // AUTH_TOKEN 쿠키가 있으면 해당 값 반환
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // AUTH_TOKEN 쿠키가 없으면 null 반환
        return null;
    }
}
