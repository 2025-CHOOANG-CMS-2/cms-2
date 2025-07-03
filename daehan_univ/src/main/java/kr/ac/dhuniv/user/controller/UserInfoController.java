package kr.ac.dhuniv.user.controller;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.repository.UserRepository;
import kr.ac.dhuniv.user.repository.User_EmplInfoRepository;
import kr.ac.dhuniv.user.repository.User_StdInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {
private final UserRepository userRepository;
private final User_StdInfoRepository stdInfoRepository;
private final User_EmplInfoRepository emplInfoRepository;

/**
 * ✅ 로그인한 사용자 정보 조회 API
 * - userId (학번/사번) 기준으로 user_info + std_info 조인
 */
@GetMapping("/me/student")
public ResponseEntity<?> getLoginUser(Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
        return ResponseEntity.status(401).body("Unauthorized");
    }

    String userId = authentication.getName();  // JWT의 sub

    User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("사용자 없음"));
    // 학생 정보 조회 (nullable)
    StdInfo std = stdInfoRepository.findByUser_UserId(userId).orElse(null); // 학생이 아닐 수 있으므로 null 허용

    return ResponseEntity.ok(Map.of(
            "userIdx", user.getUserIdx(),
            "userId", user.getUserId(),
            "userYn", user.getUserYn(),
            "stdId", std.getStdId(),
            "stdNm", std != null ? std.getStdNm() : null,
            "scsbjtCd", std != null ? std.getScsbjtCd() : null,
            "stdEmlAddr", std != null ? std.getStdEmlAddr() : null));

}
    /**
     * ✅ 로그인한 교직원 정보 조회 API
     * - userId (사번) 기준으로 user_info + empl_info 조인
     */
    @GetMapping("/me/employee")
    public ResponseEntity<?> getEmployeeUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String userId = authentication.getName();  // JWT의 sub

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 교직원 정보 조회 (nullable)
        EmplInfo empl = emplInfoRepository.findByUser_UserId(userId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "userIdx", user.getUserIdx(),
                "userId", user.getUserId(),
                "userYn", user.getUserYn(),
                "emplId", empl != null ? empl.getEmplId() : null,
                "emplNm", empl != null ? empl.getEmplNm() : null,
                "deptCd", empl != null ? empl.getDeptCd() : null,
                "emplEmailAddr", empl != null ? empl.getEmplEmailAddr() : null,
                "positionCd", empl != null ? empl.getPositionCd() : null
        ));
    }

}