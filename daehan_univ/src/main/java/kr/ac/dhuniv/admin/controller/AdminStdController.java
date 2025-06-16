package kr.ac.dhuniv.admin.controller;

import kr.ac.dhuniv.admin.service.AdminStdService;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/student")
@RequiredArgsConstructor
@RestController
public class AdminStdController {

    private final AdminStdService stdService;

    // 학생 등록 (POST 요청)
    @PostMapping
    public ResponseEntity<?> insertStudent(@RequestBody StdInfoDto dto) {
        try {
            StdInfoDto savedDto = stdService.insertStudent(dto);
            if (savedDto != null) {
                return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("학생 등록에 실패했습니다. 유효하지 않은 데이터이거나 서비스 내부 오류.", HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 등록에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 학생 목록 페이징 및 필터링 조회 (GET 요청)
    @GetMapping
    public ResponseEntity<Page<StdInfoDto>> getAllStudents(
        @PageableDefault(size = 10, page = 0, sort = "stdNo") Pageable pageable, // 학번(stdNo)으로 기본 정렬
        @RequestParam(name = "searchName", required = false) String searchName,   // 이름 검색 파라미터
        @RequestParam(name = "searchDept", required = false) String searchDept,   // 학과 코드 검색 파라미터
        @RequestParam(name = "searchStatus", required = false) String searchStatus  // 상태 코드 검색 파라미터
    ) {
        // AdminStdService의 getAllStudents 메서드 호출 시 검색 파라미터 전달
        Page<StdInfoDto> studentPage = stdService.getAllStudents(pageable, searchName, searchDept, searchStatus);
        return ResponseEntity.ok(studentPage);
    }

    // 학생 정보 수정 (PUT 요청)
    @PutMapping("/{stdNo}")
    public ResponseEntity<?> updateStudent(
    	@PathVariable("stdNo") String stdNo,
        @RequestBody StdInfoDto dto
    ) {
        try {
            StdInfoDto updatedDto = stdService.updateStudent(stdNo, dto);
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 정보 수정에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 학생 삭제 (DELETE 요청)
    @DeleteMapping("/{stdNo}")
    public ResponseEntity<?> deleteStudent(
        @PathVariable("stdNo") String stdNo
    ) {
        try {
            stdService.deleteStudent(stdNo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 삭제에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}