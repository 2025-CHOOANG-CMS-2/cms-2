package kr.ac.dhuniv.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * ✅ 로그인 응답용 DTO
 * - 클라이언트에게 반환할 데이터 (JWT 토큰 + 권한)
 */
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;     // JWT 토큰

    private List<String> roles; // 사용자 권한 목록
    private String redirectUrl;
}
