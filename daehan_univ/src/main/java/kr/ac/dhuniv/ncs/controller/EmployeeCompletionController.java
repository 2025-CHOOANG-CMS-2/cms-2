package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.service.NcsCompletionManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/employees/programs/completions")
@RequiredArgsConstructor
public class EmployeeCompletionController {

    private final NcsCompletionManageService completionService;

    @GetMapping
    public String completionListPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Map<String, Object> result = completionService.getCompletionList(page, size);

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
        
        return "employee/program/completion";
    }

    @PostMapping("/{cmpId}/complete")
    public String processCompletion(@PathVariable("cmpId") Long cmpId) {
        completionService.processCompletion(cmpId);
        return "redirect:/employees/programs/completions";
    }
}