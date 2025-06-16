package kr.ac.dhuniv.config;

import kr.ac.dhuniv.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ✅ Spring Security 설정 클래스
 * - 인증/인가 정책 및 JWT 필터 설정
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ JWT 인증 필터 주입 (JWT를 검사하고 SecurityContext에 인증 정보를 설정)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ SecurityFilterChain 빈 등록
     * - 보안 정책 (권한, 세션, 필터)을 구성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔹 CSRF 보호 비활성화 (쿠키 SameSite로 방어, REST API용)
                .csrf(csrf -> csrf.disable())

                // 🔹 세션 생성 금지 (JWT 기반이므로 상태 저장 불필요)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔹 URL 별 접근 권한 설정

                .authorizeHttpRequests(auth -> auth
                        // /api/auth/** → 로그인, 회원가입 등은 누구나 접근 허용
                        .requestMatchers("/login","/api/auth/**","/css/**","/js/**","/admin/index").permitAll()

                        // /api/admin/** → ADMIN 권한 사용자만 접근 허용
                        .requestMatchers("/admin/**","/api/admin/**").hasRole("ADMIN")

                        // /api/employees/** → STAFF, COUNSELOR, PROFESSOR, ADMIN 권한 접근 허용
                        .requestMatchers("/employee/**","/api/employees/**").hasAnyRole("EMPLOYEE", "COUNSELOR", "PROFESSOR", "ADMIN")

                        // /api/student/** → STUDENT 권한 접근 허용
                        .requestMatchers("/student/**","/api/student/**").hasAnyRole("STUDENT","ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )


                // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 전에 실행)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ AuthenticationManager 빈 등록
     * - Spring Security에서 로그인 시 인증에 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ PasswordEncoder 빈 등록
     * - 비밀번호 암호화/검증 시 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
