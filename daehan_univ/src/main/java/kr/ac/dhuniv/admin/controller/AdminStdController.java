package kr.ac.dhuniv.admin.controller;

import kr.ac.dhuniv.admin.service.AdminStdService; // AdminStdService 임포트 유지
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Spring Data JPA Page 임포트
import org.springframework.data.domain.Pageable; // Spring Data JPA Pageable 임포트
import org.springframework.data.web.PageableDefault; // PageableDefault 임포트
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // @GetMapping, @PutMapping, @DeleteMapping 사용을 위해 와일드카드 임포트 또는 개별 임포트

@RequestMapping("/admin/student")
@RequiredArgsConstructor
@RestController
public class AdminStdController {

    private final AdminStdService stdService; // 필드명 오타 수정: stdSevice -> stdService

    // 1. 학생 등록 (기존 코드)
    @PostMapping
    public ResponseEntity<?> insertStudent(@RequestBody StdInfoDto dto) {
        try {
            StdInfoDto savedDto = stdService.insertStudent(dto); // 필드명 수정 반영
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



    //  2. 학생 목록 페이징 조회 API 추가
    @GetMapping // GET /admin/student 요청을 처리합니다.
    public ResponseEntity<Page<StdInfoDto>> getAllStudents(
        // @PageableDefault를 사용하여 기본 페이지 크기, 시작 페이지, 정렬 기준을 설정합니다.
        // 프론트엔드의 JavaScript에서 'page'와 'size' 파라미터를 넘겨주면 이 설정이 덮어쓰여집니다.
        @PageableDefault(size = 10, page = 0, sort = "stdNo") Pageable pageable
    ) {
        // AdminStdService에서 페이징된 학생 목록을 가져옵니다.
        Page<StdInfoDto> studentPage = stdService.getAllStudents(pageable);
        // HTTP 200 OK 상태 코드와 함께 Page<StdInfoDto> 객체를 반환합니다.
        return ResponseEntity.ok(studentPage);
    }


    //  3. 학생 정보 수정 API 추가 (PUT 요청)
    @PutMapping("/{stdNo}") // PUT /admin/student/{stdNo} 요청을 처리합니다.
    public ResponseEntity<?> updateStudent(
    	@PathVariable("stdNo") String stdNo, // URL 경로에서 학번(stdNo)을 추출합니다.
        @RequestBody StdInfoDto dto // 요청 본문에서 수정할 학생 정보를 StdInfoDto 객체로 받습니다.
    ) {
        try {
            // URL의 학번과 DTO 내부의 학번이 일치하는지 확인하는 로직 (선택 사항이지만 권장)
            // if (!stdNo.equals(dto.getSTD_NO())) {
            //     return new ResponseEntity<>("요청된 학번과 DTO의 학번이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
            // }

            // AdminStdService를 호출하여 학생 정보를 업데이트합니다.
            StdInfoDto updatedDto = stdService.updateStudent(stdNo, dto);

            // 업데이트가 성공하면 HTTP 200 OK와 업데이트된 StdInfoDto를 반환합니다.
            return ResponseEntity.ok(updatedDto);
        } catch (IllegalArgumentException e) {
            // 학생을 찾을 수 없거나 유효성 검사 실패 등 비즈니스 로직 오류 시
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 또는 HttpStatus.BAD_REQUEST
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 오류 시
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 정보 수정에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   
    //  4. 학생 삭제 API 추가 (DELETE 요청)
 // 4. 학생 삭제 API 추가 (DELETE 요청)
    @DeleteMapping("/{stdNo}") // DELETE /admin/student/{stdNo} 요청을 처리합니다.
    public ResponseEntity<?> deleteStudent(
        @PathVariable("stdNo") String stdNo // <-- 여기에 "stdNo"를 명시적으로 추가!
    ) {
        try {
            // AdminStdService를 호출하여 학생을 삭제합니다.
            stdService.deleteStudent(stdNo);
            // 성공적으로 삭제되었지만, 반환할 내용이 없을 때 HTTP 204 No Content를 반환합니다.
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 학생을 찾을 수 없을 경우 등 비즈니스 로직 오류 시
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 오류 시
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 삭제에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}