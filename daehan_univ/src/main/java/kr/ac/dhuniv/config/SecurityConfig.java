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
 * - 개발 단계: 모든 요청 허용
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ JWT 인증 필터 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * ✅ SecurityFilterChain 빈 등록
     * - 개발 단계: 모든 URL 접근 허용
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔹 CSRF 보호 비활성화 (개발 단계, REST API용)
                .csrf(csrf -> csrf.disable())
                // 🔹 로그아웃 비활성화 (필요시 활성화)
                .logout(logout -> logout.disable())
                // 🔹 세션 생성 금지 (JWT 기반)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔹 모든 요청 허용
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ✅ 모든 요청 허용
                )

                // 🔹 JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ AuthenticationManager 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ PasswordEncoder 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

