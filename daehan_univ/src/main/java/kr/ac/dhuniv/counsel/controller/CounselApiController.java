package kr.ac.dhuniv.counsel.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
import kr.ac.dhuniv.counsel.dto.UpdateCounselorStatusDto;
import kr.ac.dhuniv.counsel.service.CounselorAdminService;
import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/counselors")  //관리자용 API 경로
@RequiredArgsConstructor
public class CounselApiController {

    private final CounselorAdminService counselorAdminService;

    @GetMapping
    public ResponseEntity<List<CounselorListDto>> getCounselorList() {
        List<CounselorListDto> counselors = counselorAdminService.getCounselorList();
        return ResponseEntity.ok(counselors);
    }
    
    @GetMapping("/{counselorId}")
    public ResponseEntity<CounselorListDto> getCounselorDetail(@PathVariable("counselorId") String counselorId) {
        CounselorListDto counselor = counselorAdminService.getCounselorDetail(counselorId);
        return ResponseEntity.ok(counselor);
    }
    
    @PutMapping("/{counselorId}")
    public ResponseEntity<Void> updateCounselor(
            @PathVariable("counselorId") String counselorId,
            @RequestBody UpdateCounselorRequestDto requestDto) {
        counselorAdminService.updateCounselor(counselorId, requestDto);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/unregistered")
    public ResponseEntity<List<UnregisteredEmpDto>> getUnregisteredCounselors() {
        List<UnregisteredEmpDto> employees = counselorAdminService.getUnregisteredEmployees();
        return ResponseEntity.ok(employees);
    }
    
    @PostMapping
    public ResponseEntity<Void> createCounselor(@RequestBody CreateCounselorRequestDto requestDto) {
        counselorAdminService.createCounselor(requestDto);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{counselorId}")
    public ResponseEntity<Void> deleteCounselor(@PathVariable("counselorId") String counselorId) {
        counselorAdminService.deleteCounselor(counselorId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{counselorId}/status")
    public ResponseEntity<Void> updateCounselorStatus(
            @PathVariable("counselorId") String counselorId,
            @RequestBody UpdateCounselorStatusDto requestDto) {
        counselorAdminService.updateCounselorStatus(counselorId, requestDto.getIsActive());
        return ResponseEntity.ok().build();
    }
}