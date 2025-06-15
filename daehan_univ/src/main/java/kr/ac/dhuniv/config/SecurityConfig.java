package kr.ac.dhuniv.config;

import kr.ac.dhuniv.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ✅ SecurityConfig 클래스
 * - Spring Security의 보안 설정 클래스
 * - JWT 기반 인증/인가 설정 + 필터 체인 구성
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ JwtAuthenticationFilter 의존성 주입 (JWT 인증 처리를 위한 필터)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ filterChain 메소드
     * - SecurityFilterChain을 Bean으로 등록
     * - 보안 정책, 필터, URL 권한 규칙을 설정
     *
     * @param http HttpSecurity 빌더 객체
     * @return SecurityFilterChain (최종 빌드된 보안 필터 체인)
     * @throws Exception 보안 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF 보호 비활성화
                // JWT 기반 인증은 세션을 사용하지 않으므로 CSRF 보호 필요 없음
                .csrf(csrf -> csrf.disable())

                // ✅ 세션 관리 정책: STATELESS
                // 서버가 세션을 생성하거나 유지하지 않음 (JWT 기반 인증 필수 설정)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ URL 별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // /api/auth/** → 로그인, 회원가입 등은 누구나 접근 허용
                        .requestMatchers("/login","/api/auth/**").permitAll()

                        // /api/admin/** → ADMIN 권한 사용자만 접근 허용
                        .requestMatchers("/admin/**","/api/admin/**").hasRole("ADMIN")

                        // /api/employees/** → STAFF, COUNSELOR, PROFESSOR, ADMIN 권한 접근 허용
                        .requestMatchers("/employee/**","/api/employees/**").hasAnyRole("EMPLOYEE", "COUNSELOR", "PROFESSOR", "ADMIN")

                        // /api/student/** → STUDENT 권한 접근 허용
                        .requestMatchers("/student/**","/api/student/**").hasRole("STUDENT")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // ✅ JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
                // JWT를 통한 인증이 Username/Password 기반 인증 전에 수행되도록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 최종 SecurityFilterChain 빌드 및 반환
        return http.build();
    }

    /**
     * ✅ authenticationManager 메소드
     * - AuthenticationManager를 Bean으로 등록
     * - 로그인 시 사용자 인증 처리에 사용됨
     *
     * @param config Spring이 제공하는 AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 인증 매니저 생성 실패 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Spring Security가 자동으로 설정한 AuthenticationManager 반환
        return config.getAuthenticationManager();
    }
}
