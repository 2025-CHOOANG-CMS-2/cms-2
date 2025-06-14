package kr.ac.dhuniv.std_info.controller;

import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import kr.ac.dhuniv.std_info.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/student")
@RequiredArgsConstructor
@RestController
public class StudentController {

    private final StudentService stdSevice;

    @PostMapping
    public ResponseEntity<?> insertStudent(@RequestBody StdInfoDto dto) {
        try {
            // 이 부분을 수정합니다.
            // 서비스에서 StdInfoDto를 반환하므로, boolean 대신 StdInfoDto로 받습니다.
            StdInfoDto savedDto = stdSevice.insertStudent(dto);

            // 서비스가 null을 반환할 경우(실패), Bad Request 또는 Internal Server Error를 반환합니다.
            if (savedDto != null) {
                // 성공 시, 저장된 학생 정보 (StdInfoDto)를 포함하여 201 Created 응답 반환
                return new ResponseEntity<>(savedDto, HttpStatus.CREATED); // savedDto를 직접 반환
            } else {
                // 서비스에서 null이 반환된 경우 (학생 등록 비즈니스 로직 실패)
                return new ResponseEntity<>("학생 등록에 실패했습니다. 유효하지 않은 데이터이거나 서비스 내부 오류.", HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            // 서비스에서 던진 IllegalArgumentException (예: 사용자 없음) 처리
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 예상치 못한 서버 내부 오류 발생 시
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류로 인해 학생 등록에 실패했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // TODO: 다른 API 엔드포인트들 (GET /admin/student, GET /admin/student/{id}, PUT, DELETE 등)
}