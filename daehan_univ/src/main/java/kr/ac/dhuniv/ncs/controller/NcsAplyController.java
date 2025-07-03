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
	public String applicationListPage(@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, Model model) {
		Map<String, Object> result = manageService.getApplicationList(page, size);

		// 페이징 계산 로직 추가
		int totalCount = (int) result.get("totalCount");
		int totalPages = (totalCount + size - 1) / size;
		int pageNavigationSize = 5;
		int startPage = ((page - 1) / pageNavigationSize) * pageNavigationSize + 1;
		int endPage = Math.min(startPage + pageNavigationSize - 1, totalPages);

		model.addAttribute("list", result.get("list"));

		// 페이징을 위한 모델 속성 추가
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);

		return "employee/program/application";
	}

	// 상세보기 JSON 데이터 API
	@GetMapping("/{aplyId}/json")
	@ResponseBody
	public ResponseEntity<NcsApplicationDto> getApplicationDetail(@PathVariable("aplyId") Long aplyId) { // ("aplyId")
																											// 추가
		NcsApplicationDto detail = manageService.getApplicationDetail(aplyId);
		return ResponseEntity.ok(detail);
	}

	// 승인 처리
	@PostMapping("/{aplyId}/approve")
	public String approveApplication(@PathVariable("aplyId") Long aplyId) { // ("aplyId") 추가
		manageService.approveApplication(aplyId);
		return "redirect:/employees/programs/applications"; // 리다이렉트 경로 수정
	}

	// 반려 처리
	@PostMapping("/{aplyId}/reject")
	public String rejectApplication(@PathVariable("aplyId") Long aplyId) { // ("aplyId") 추가
		manageService.rejectApplication(aplyId);
		return "redirect:/employees/programs/applications"; // 리다이렉트 경로 수정
	}
}