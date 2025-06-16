package kr.ac.dhuniv.counsel.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.service.CounselorAdminService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/counselors") // 관리자용 API 경로
@RequiredArgsConstructor
public class CounselApiController {

    private final CounselorAdminService counselorAdminService;

    @GetMapping
    public ResponseEntity<List<CounselorListDto>> getCounselorList() {
        List<CounselorListDto> counselors = counselorAdminService.getCounselorList();
        return ResponseEntity.ok(counselors);
    }
}