package kr.ac.dhuniv.admin.api_controller;

import kr.ac.dhuniv.admin.service.AdminStdService;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/admin/student")
@RequiredArgsConstructor
@RestController
public class AdminStdController {

    private final AdminStdService stdService;

    /**
     * 학생 등록 (POST 요청)
     * POST /admin/student
     */
    @PostMapping
    public ResponseEntity<StdInfoDto> insertStudent(@RequestBody StdInfoDto dto) {
        try {
            StdInfoDto savedDto = stdService.insertStudent(dto);
            return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "학생 등록 중 충돌 발생: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 학생 등록에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 학생 목록 페이징 및 필터링 조회 (GET 요청)
     * GET /admin/student?page={page}&size={size}&searchName={name}&searchDept={dept}&searchStatus={status}
     */
    @GetMapping
    public ResponseEntity<Page<StdInfoDto>> getAllStudents(
        // ⭐ 중요: stdNo 대신 user.userId로 정렬합니다. ⭐
        @PageableDefault(size = 10, page = 0, sort = "user.userId") Pageable pageable, // StdInfo 엔티티의 user 필드 내 userId로 정렬
        @RequestParam(name = "searchName", required = false) String searchName,
        @RequestParam(name = "searchDept", required = false) String searchDept,
        @RequestParam(name = "searchStatus", required = false) String searchStatus
    ) {
        Page<StdInfoDto> studentPage = stdService.getAllStudents(pageable, searchName, searchDept, searchStatus);
        return ResponseEntity.ok(studentPage);
    }

    /**
     * 특정 학번의 학생 상세 정보 조회
     * GET /admin/student/{stdNo}
     */
    @GetMapping("/{stdNo}")
    public ResponseEntity<StdInfoDto> getStudentByStdNo(@PathVariable("stdNo") String stdNo) {
        try {
            StdInfoDto student = stdService.getStudentByStdNo(stdNo);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "학생 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 학생 정보 수정 (PUT 요청)
     * PUT /admin/student/{stdNo}
     */
    @PutMapping("/{stdNo}")
    public ResponseEntity<StdInfoDto> updateStudent(
        @PathVariable("stdNo") String stdNo,
        @RequestBody StdInfoDto dto
    ) {
        try {
            StdInfoDto updatedDto = stdService.updateStudent(stdNo, dto);
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "학생 정보 수정 중 시스템 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "학생 정보 수정 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 학번의 학생 삭제 (DELETE 요청)
     * DELETE /admin/student/{stdNo}
     */
    @DeleteMapping("/{stdNo}")
    public ResponseEntity<Void> deleteStudent(
        @PathVariable("stdNo") String stdNo
    ) {
        try {
            stdService.deleteStudent(stdNo);
            return ResponseEntity.noContent().build(); // 204 No Content 반환
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 학생 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
