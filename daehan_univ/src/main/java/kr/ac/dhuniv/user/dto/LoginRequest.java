package kr.ac.dhuniv.user.dto;

import lombok.Data;

/**
 * ✅ 로그인 요청용 DTO
 * - 클라이언트가 전달하는 로그인 데이터 (userId, password)
 */
@Data
public class LoginRequest {
    private String userId;   // 사용자 ID
    private String password; // 비밀번호
}