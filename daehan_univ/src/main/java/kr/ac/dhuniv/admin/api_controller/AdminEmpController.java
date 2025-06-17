package kr.ac.dhuniv.admin.api_controller;

import kr.ac.dhuniv.admin.service.AdminEmpService;
import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/employee")
public class AdminEmpController {

    private final AdminEmpService adminEmpService;

    /**
     * 교직원 목록 조회 (검색 및 페이징 포함)
     * GET /admin/employee?page={page}&size={size}&searchName={name}&searchDept={dept}&searchStatus={status}
     * 파라미터 이름을 @RequestParam에 명시하여 'Name for argument' 에러를 해결합니다.
     */
    @GetMapping
    public ResponseEntity<Page<EmplInfoDto>> getAllEmployees(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "searchName", required = false) String searchName,
            @RequestParam(name = "searchDept", required = false) String searchDept,
            @RequestParam(name = "searchStatus", required = false) String searchStatus) {
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("emplNo").ascending());
        
        Page<EmplInfoDto> employees = adminEmpService.getAllEmployees(pageable, searchName, searchDept, searchStatus);
        return ResponseEntity.ok(employees);
    }

    /**
     * 특정 사번의 교직원 상세 정보 조회
     * GET /admin/employee/{emplNo}
     */
    @GetMapping("/{emplNo}")
    public ResponseEntity<EmplInfoDto> getEmployeeByEmplNo(@PathVariable("emplNo") String emplNo) { // <-- "emplNo" 명시
        try {
            EmplInfoDto employee = adminEmpService.getEmployeeByEmplNo(emplNo);
            return ResponseEntity.ok(employee);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * 새로운 교직원 등록
     * POST /admin/employee
     */
    @PostMapping
    public ResponseEntity<EmplInfoDto> createEmployee(@RequestBody EmplInfoDto emplInfoDto) {
        try {
            EmplInfoDto createdEmployee = adminEmpService.insertEmployee(emplInfoDto);
            return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "교직원 등록 중 시스템 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "교직원 등록 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 교직원 정보 수정
     * PUT /admin/employee/{emplNo}
     */
    @PutMapping("/{emplNo}")
    public ResponseEntity<EmplInfoDto> updateEmployee(
            @PathVariable("emplNo") String emplNo, // <-- "emplNo" 명시
            @RequestBody EmplInfoDto emplInfoDto) {
        try {
            EmplInfoDto updatedEmployee = adminEmpService.updateEmployee(emplNo, emplInfoDto);
            return ResponseEntity.ok(updatedEmployee);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "교직원 정보 수정 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 사번의 교직원 삭제
     * DELETE /admin/employee/{emplNo}
     */
    @DeleteMapping("/{emplNo}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("emplNo") String emplNo) { // <-- "emplNo" 명시
        try {
            adminEmpService.deleteEmployee(emplNo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "교직원 삭제 실패: " + e.getMessage(), e);
        }
    }
}
