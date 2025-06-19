package kr.ac.dhuniv.mypage.api_contoller;



import kr.ac.dhuniv.mypage.service.StdMypageService;
import kr.ac.dhuniv.std_info.dto.StdInfoDto; // DTO를 사용합니다.
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController // API 컨트롤러입니다.
@RequiredArgsConstructor
@RequestMapping("/api/mypage") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/mypage" 경로로 매핑됩니다.
@Slf4j
public class StdMypageApiController {

    private final StdMypageService stdMypageService;

    /**
     * 학생 정보를 조회하는 API (API 컨트롤러이므로 DTO 반환)
     * GET /api/mypage/{stdNo}
     *
     * @param stdNo 조회할 학생의 학번 (User의 userId)
     * @return 조회된 StdInfoDto 객체 (JSON 형태)
     */
    @GetMapping("/{stdNo}")
    public ResponseEntity<StdInfoDto> getStudentInfo(@PathVariable String stdNo) {
        log.info("API: 학생 정보 조회 요청: {}", stdNo);
        // 서비스의 getStdInfoDtoByUserId 메서드 호출 (stdNo가 userId로 사용됨)
        Optional<StdInfoDto> stdInfoDto = stdMypageService.getStdInfoDtoByUserId(stdNo);
        return stdInfoDto.map(ResponseEntity::ok)
                      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다: " + stdNo));
    }

    /**
     * 프로필 이미지를 업로드하고 URL을 반환하는 API
     * POST /api/mypage/uploadProfileImage
     *
     * @param studentId 이미지를 업로드할 학생의 학번 (이는 User.userId에 해당)
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 URL을 포함하는 JSON 응답
     */
    @PostMapping("/uploadProfileImage")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("studentId") String studentId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("API: 프로필 이미지 업로드 요청 - 학번: {}, 파일명: {}", studentId, file.getOriginalFilename());

        Map<String, String> response = new HashMap<>();
        try {
            // 서비스에 studentId (즉 userId) 전달
            String imageUrl = stdMypageService.uploadProfileImage(studentId, file);
            if (imageUrl != null) {
                response.put("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
                response.put("imageUrl", imageUrl);
                log.info("API: 프로필 이미지 업로드 성공. URL: {}", imageUrl);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "프로필 이미지 업로드에 실패했습니다. 학생 정보를 찾을 수 없거나 파일이 비어 있습니다.");
                log.warn("API: 프로필 이미지 업로드 실패: 서비스에서 null 반환");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IOException e) {
            response.put("message", "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
            log.error("API: 프로필 이미지 파일 저장 중 IOException 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("message", "프로필 이미지 업로드 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
            log.error("API: 프로필 이미지 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 학생 개인 정보를 업데이트하는 API
     * POST /api/mypage/updatePersonalInfo
     *
     * @param updatedInfo 업데이트할 학생 정보 DTO (JSON Body)
     * @return 업데이트 결과 메시지
     */
    @PostMapping("/updatePersonalInfo")
    public ResponseEntity<Map<String, String>> updatePersonalInfo(@RequestBody StdInfoDto updatedInfo) {
        log.info("API: 개인 정보 업데이트 요청: 학번: {}", updatedInfo.getSTD_NO());
        Map<String, String> response = new HashMap<>();
        try {
            // 서비스에 DTO의 STD_NO (즉 userId)와 DTO 객체 전달
            boolean success = stdMypageService.updatePersonalInfo(updatedInfo.getSTD_NO(), updatedInfo);
            if (success) {
                response.put("message", "개인 정보가 성공적으로 업데이트되었습니다.");
                log.info("API: 개인 정보 업데이트 성공: 학번 {}", updatedInfo.getSTD_NO());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "개인 정보 업데이트에 실패했습니다. 학생 정보를 찾을 수 없습니다.");
                log.warn("API: 개인 정보 업데이트 실패: 서비스에서 false 반환 (학번 {})", updatedInfo.getSTD_NO());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            log.error("API: 개인 정보 업데이트 실패 (IllegalArgumentException): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "개인 정보 업데이트 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
            log.error("API: 개인 정보 업데이트 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 학생 비밀번호를 변경하는 API
     * POST /api/mypage/updatePassword
     *
     * @param requestMap 비밀번호 변경 요청 데이터 (studentId, currentPassword, newPassword)
     * @return 비밀번호 변경 결과 메시지
     */
    @PostMapping("/updatePassword")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> requestMap) {
        String userId = requestMap.get("studentId"); // 클라이언트에서 studentId로 보낼 수 있음
        String currentPassword = requestMap.get("currentPassword");
        String newPassword = requestMap.get("newPassword");

        log.info("API: 비밀번호 변경 요청 - 사용자 ID: {}", userId);
        Map<String, String> response = new HashMap<>();

        if (userId == null || currentPassword == null || newPassword == null) {
            response.put("message", "필수 정보가 누락되었습니다 (사용자 ID, 현재 비밀번호, 새 비밀번호).");
            log.warn("API: 비밀번호 변경 실패: 필수 정보 누락 - 사용자 ID: {}", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // 서비스에 userId 전달
            boolean success = stdMypageService.updatePassword(userId, currentPassword, newPassword);
            if (success) {
                response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
                log.info("API: 비밀번호 변경 성공: 사용자 ID {}", userId);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인하거나 새 비밀번호가 기존 비밀번호와 달라야 합니다.");
                log.warn("API: 비밀번호 변경 실패: 서비스에서 false 반환 (사용자 ID {})", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "비밀번호 변경 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
            log.error("API: 비밀번호 변경 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
