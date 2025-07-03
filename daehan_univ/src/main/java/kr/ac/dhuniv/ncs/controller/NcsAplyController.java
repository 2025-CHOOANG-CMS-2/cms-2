package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.dto.NcsApplicationDto;
import kr.ac.dhuniv.ncs.service.NcsAplyManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/employees/programs/applications")
@RequiredArgsConstructor
public class NcsAplyController {

    private final NcsAplyManageService manageService;

    // 신청 관리 목록 페이지
    @GetMapping
    public String applicationListPage(
    		@RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Map<String, Object> result = manageService.getApplicationList(page, size);
        model.addAttribute("list", result.get("list"));
        // TODO: 페이징 UI를 위한 데이터 추가
        return "employee/program/application";
    }

    // 상세보기 JSON 데이터 API
    @GetMapping("/{aplyId}/json")
    @ResponseBody
    public ResponseEntity<NcsApplicationDto> getApplicationDetail(@PathVariable("aplyId") Long aplyId) {
        NcsApplicationDto detail = manageService.getApplicationDetail(aplyId);
        return ResponseEntity.ok(detail);
    }

    // 승인 처리
    @PostMapping("/{aplyId}/approve")
    public String approveApplication(@PathVariable("aplyId") Long aplyId) {
        manageService.approveApplication(aplyId);
        return "redirect:/employees/programs/application";
    }

    // 반려 처리
    @PostMapping("/{aplyId}/reject")
    public String rejectApplication(@PathVariable("aplyId") Long aplyId) {
        manageService.rejectApplication(aplyId);
        return "redirect:/employees/programs/application";
    }
}