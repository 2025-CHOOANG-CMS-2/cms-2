package kr.ac.dhuniv.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.ac.dhuniv.security.JwtTokenProvider;
import kr.ac.dhuniv.user.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ 로그인 & 로그아웃 API 컨트롤러
 * - JWT를 쿠키 기반으로 발급 및 제거
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * ✅ 로그인 API
     * - 클라이언트가 ID/PW를 전달하면 인증을 수행
     * - 인증 성공 시 JWT를 생성하고 쿠키에 저장
     * - 클라이언트에 JSON 응답 (메시지 + 리다이렉트 URL)을 반환
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        // 🔹 사용자가 입력한 ID/PW로 Spring Security의 인증 수행
        // UsernamePasswordAuthenticationToken 생성 → AuthenticationManager가 내부적으로 UserDetailsService 통해 DB 조회 및 PW 검사
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUserId(),      // 사용자 ID
                        loginRequest.getPassword()     // 사용자 PW
                )
        );

        // 🔹 인증 성공 시 사용자 권한 목록 추출 (예: ROLE_ADMIN, ROLE_STUDENT 등)
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // GrantedAuthority -> 권한 문자열 추출
                .map(role -> role.replace("ROLE_", ""))  // ROLE_ 제거 → ADMIN
                .collect(Collectors.toList());       // List<String> 형태로 변환

        // 🔹 JWT 토큰 생성 (subject = 사용자 ID, payload = roles)
        String token = jwtTokenProvider.generateToken(
                authentication.getName(),  // 사용자 ID (principal 이름)
                roles               // 권한 리스트

        );
        System.out.println("JWT에 담기는 roles = " + roles);
        // 🔹 JWT를 쿠키로 생성 (보안 옵션 적용)
        ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", token)
                .httpOnly(true)      // JS에서 접근 불가 → XSS 방어
                .path("/")           // 모든 경로에 쿠키 전송
                .maxAge(60 * 60)     // 쿠키 수명: 1시간
                .build();

        // 🔹 응답 헤더에 쿠키 추가
        response.addHeader("Set-Cookie", cookie.toString());

        // 🔹 권한에 따라 클라이언트가 이동해야 할 페이지 URL 결정
        String redirectUrl;
        if (roles.contains("ADMIN")) {
            redirectUrl = "/admin/index";  // 관리자 메인 페이지
        } else if (roles.stream().anyMatch(r ->
                List.of("EMPLOYEE", "COUNSELOR", "PROFESSOR").contains(r))) {
            System.out.println("roles = " + roles.toString());
            redirectUrl = "/employees/index";  // 교직원용 메인 페이지
        } else {
            //System.out.println("roles = " + roles.toString());
            redirectUrl = "/student/index";   // 학생용 메인 페이지
        }

        // 🔹 클라이언트에 JSON 응답 반환 (메시지 + 이동 URL)
        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",      // 클라이언트에서 출력할 메시지
                "redirectUrl", redirectUrl    // JS가 리다이렉트용으로 사용할 URL
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 쿠키 삭제 (만료)
        ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("로그아웃 성공");
    }
}
