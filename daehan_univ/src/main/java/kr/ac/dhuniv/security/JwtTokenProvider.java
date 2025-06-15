package kr.ac.dhuniv.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ JwtTokenProvider 클래스
 * - JWT 토큰 생성, 파싱, 검증, 인증 객체 반환을 담당
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // ✅ application.yml에 설정한 JWT 시크릿 키 문자열 주입
    @Value("${jwt.secret}")
    private String secretKeyString;

    // ✅ application.yml에 설정한 JWT 만료 시간 (밀리초 단위)
    @Value("${jwt.expiration}")
    private long expiration;

    // ✅ HMAC-SHA 서명에 사용할 비밀키 객체
    private Key secretKey;

    /**
     * ✅ 초기화 메소드
     * - secretKeyString을 HMAC-SHA 알고리즘용 Key 객체로 변환
     * - 서버 기동 시 1회 실행됨
     */
    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    /**
     * ✅ generateToken
     * - 주어진 사용자 ID와 권한 목록을 포함한 JWT 토큰 생성
     *
     * @param userId 사용자 ID (subject)
     * @param roles 권한 목록 (ROLE_XXX)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String userId, List<String> roles) {
        // JWT Payload에 담을 claims 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // roles 정보 추가

        // JWT 생성
        return Jwts.builder()
                .setSubject(userId) // subject에 사용자 ID 저장
                .addClaims(claims) // 커스텀 클레임 추가 (roles)
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256) // 서명 및 알고리즘
                .compact(); // 최종 문자열로 변환
    }

    /**
     * ✅ validateToken
     * - 주어진 토큰이 유효한지 검사 (서명 검증, 만료 확인)
     *
     * @param token 검사할 토큰
     * @return true = 유효, false = 유효하지 않음
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 및 서명 검증
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 잘못된 서명, 만료, 구조 오류 등 발생 시 false 반환
            return false;
        }
    }

    /**
     * ✅ getAuthentication
     * - 토큰에서 사용자 정보와 권한을 추출하여 Authentication 객체 생성
     *
     * @param token 유효한 토큰
     * @return 인증 객체 (UsernamePasswordAuthenticationToken)
     */
    public Authentication getAuthentication(String token) {
        // 토큰 파싱 → Claims (Payload) 추출
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // roles 클레임에서 권한 목록 추출
        List<String> roles = (List<String>) claims.get("roles");

        // SimpleGrantedAuthority 리스트로 변환
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 인증 객체 생성 (비밀번호는 null)
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
    }

    /**
     * ✅ resolveToken
     * - HttpServletRequest에서 Bearer 토큰을 추출
     *
     * @param request Http 요청 객체
     * @return Bearer 토큰 값 (없으면 null)
     */
    public String resolveToken(HttpServletRequest request) {
        // Authorization 헤더 값 추출
        String bearer = request.getHeader("Authorization");

        // Bearer 스킴인지 확인 후 토큰 부분만 반환
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
