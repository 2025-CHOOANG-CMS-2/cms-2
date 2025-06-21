package kr.ac.dhuniv.mypage.api_contoller;

import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
import kr.ac.dhuniv.mypage.service.EmpMypageService;
import kr.ac.dhuniv.user.User; // User 임포트는 getMyPageInfo에서만 사용되므로 그대로 둠
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // @AuthenticationPrincipal은 getMyPageInfo에서만 사용
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 교직원 마이페이지 API 요청을 처리하는 컨트롤러
@RestController
@RequestMapping("/api/mypage/employee")
public class EmpMypageApiController {

    private final EmpMypageService empMypageService;

    @Autowired
    public EmpMypageApiController(EmpMypageService empMypageService) {
        this.empMypageService = empMypageService;
    }

    /**
     * 현재 로그인한 교직원의 개인 정보를 조회합니다.
     * 이 메서드는 여전히 @AuthenticationPrincipal을 사용하여 로그인 정보를 활용할 수 있습니다.
     * 비인증 사용자의 경우 401 Unauthorized를 반환합니다.
     * @param user 현재 인증된 사용자 정보 (Spring Security Principal)
     * @return 교직원 정보 DTO (EmplInfoDto)
     */
    @GetMapping("/info")
    public ResponseEntity<EmplInfoDto> getMyPageInfo(@AuthenticationPrincipal User user) {
        if (user == null) {
            System.out.println("DEBUG: 비인증 사용자 접근 시도 - 교직원 정보 조회.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        System.out.println("DEBUG: 교직원 정보 조회 요청: User ID = " + user.getUserId());

        Optional<EmplInfoDto> emplInfoDto = empMypageService.getMypageInfo(user);

        return emplInfoDto.map(ResponseEntity::ok)
                          .orElseGet(() -> {
                              System.out.println("WARN: 교직원 정보를 찾을 수 없습니다: User ID = " + user.getUserId());
                              return ResponseEntity.notFound().build(); // 404 Not Found
                          });
    }

    /**
     * 교직원 개인 정보를 업데이트합니다.
     * @param updatedInfoDto 업데이트할 정보가 담긴 DTO (STAFF_NO 포함)
     * @return 업데이트 결과 메시지
     */
    @PostMapping("/updatePersonalInfo")
    public ResponseEntity<Map<String, Object>> updatePersonalInfo(
            @RequestBody EmplInfoDto updatedInfoDto) {
        
        Map<String, Object> response = new HashMap<>();

        // ⭐ 수정: @AuthenticationPrincipal 제거, DTO에서 STAFF_NO 직접 사용 ⭐
        String employeeId = updatedInfoDto.getSTAFF_NO();
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("DEBUG: 개인 정보 업데이트 실패: 요청 DTO에 교직원 번호(STAFF_NO)가 누락되었습니다.");
            response.put("success", false);
            response.put("message", "교직원 번호가 필요합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        System.out.println("DEBUG: 교직원 개인 정보 업데이트 요청: Employee ID (from DTO) = " + employeeId + ", DTO = " + updatedInfoDto);

        try {
            boolean success = empMypageService.updatePersonalInfo(employeeId, updatedInfoDto);
            if (success) {
                response.put("success", true);
                response.put("message", "개인 정보가 성공적으로 업데이트되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "개인 정보 업데이트에 실패했습니다. (존재하지 않는 교직원 ID 또는 기타 오류)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: 개인 정보 업데이트 중 유효성 검사 오류: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            System.err.println("ERROR: 개인 정보 업데이트 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "개인 정보 업데이트 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 교직원의 비밀번호를 변경합니다.
     * @param requestBody 비밀번호 변경 요청 데이터 (userId, currentPassword, newPassword)
     * @return 비밀번호 변경 결과 메시지
     */
    @PostMapping("/updatePassword")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @RequestBody Map<String, String> requestBody) {
        
        Map<String, Object> response = new HashMap<>();

        // ⭐ 수정: @AuthenticationPrincipal 제거, requestBody에서 userId 직접 사용 ⭐
        String userId = requestBody.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("DEBUG: 비밀번호 변경 실패: 요청 본문에 사용자 ID(userId)가 누락되었습니다.");
            response.put("success", false);
            response.put("message", "사용자 ID가 필요합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String currentPassword = requestBody.get("currentPassword");
        String newPassword = requestBody.get("newPassword");

        if (currentPassword == null || newPassword == null || currentPassword.isEmpty() || newPassword.isEmpty()) {
            response.put("success", false);
            response.put("message", "현재 비밀번호와 새 비밀번호를 모두 입력해야 합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        System.out.println("DEBUG: 비밀번호 변경 요청: User ID (from RequestBody) = " + userId);

        try {
            boolean success = empMypageService.updatePassword(userId, currentPassword, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "현재 비밀번호가 올바르지 않거나 새 비밀번호가 기존 비밀번호와 같습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            System.err.println("ERROR: 비밀번호 변경 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 교직원 프로필 이미지를 업로드합니다.
     * @param file 업로드할 이미지 파일
     * @param employeeId 교직원 ID (폼 데이터에서 가져옴)
     * @return 업로드 결과 및 이미지 URL
     */
    @PostMapping("/uploadProfileImage")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("employeeId") String employeeId) { 
        
        Map<String, Object> response = new HashMap<>();

        // ⭐ 수정: @AuthenticationPrincipal 제거, employeeId를 직접 사용 ⭐
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("DEBUG: 프로필 이미지 업로드 실패: 요청에 교직원 번호(employeeId)가 누락되었습니다.");
            response.put("success", false);
            response.put("message", "교직원 번호가 필요합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 인증 로직이 우회되므로, 여기서 특정 ID에 대한 추가적인 권한 검증은 생략됩니다.
        System.out.println("DEBUG: 프로필 이미지 업로드 요청: Employee ID (from RequestParam) = " + employeeId + ", File Name = " + file.getOriginalFilename());

        try {
            String imageUrl = empMypageService.uploadProfileImage(employeeId, file);
            if (imageUrl != null) {
                response.put("success", true);
                response.put("message", "프로필 사진이 성공적으로 업로드되었습니다.");
                response.put("imageUrl", imageUrl);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "프로필 사진 업로드에 실패했습니다. (존재하지 않는 교직원 ID 또는 기타 오류)");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (IOException e) {
            System.err.println("ERROR: 프로필 이미지 파일 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            System.err.println("ERROR: 프로필 이미지 업로드 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "프로필 이미지 업로드 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
