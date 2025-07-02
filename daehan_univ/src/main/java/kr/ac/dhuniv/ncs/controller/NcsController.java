package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.service.NcsPrgInfoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employees/programs")
public class NcsController {
    private final NcsPrgInfoService service;

    public NcsController(NcsPrgInfoService service) {
        this.service = service;
    }

    /** HTML 목록 페이지 */
    @GetMapping
    public String index(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "prgNm", required = false) String prgNm,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "cciId", required = false) String cciId,
            Model model
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("prgNm", prgNm);
        params.put("status", status);
        params.put("cciId", cciId);
        
        // 서비스에서 목록과 전체 개수가 담긴 Map을 받음
        Map<String, Object> result = service.getList(params);
        List<ProgramDto> list = (List<ProgramDto>) result.get("list");
        int totalCount = (int) result.get("totalCount");

        // 페이징 계산
        int totalPages = (totalCount + size - 1) / size;
        int pageNavigationSize = 5; // 한 번에 보여줄 페이지 번호 개수
        int startPage = ((page - 1) / pageNavigationSize) * pageNavigationSize + 1;
        int endPage = Math.min(startPage + pageNavigationSize - 1, totalPages);
        
        model.addAttribute("list", list);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("prgNm", prgNm);
        model.addAttribute("status", status);
        model.addAttribute("cciId", cciId);
        
        return "employee/program/index"; // 뷰 경로 수정: index.html에 맞게
    }

    /** 신규 등록 폼 */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("item", new ProgramDto());
        return "employee/program/form";
    }

    /** 신규 등록 처리 (HTML form) */
    @PostMapping
    public String create(@ModelAttribute ProgramDto dto) {
        service.create(dto);
        return "redirect:/employee/programs";
    }

    /** 신규 등록 처리 (AJAX JSON) */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createProgramJson(
            @RequestBody ProgramDto dto
    ) {
        try {
            // 프론트에서 사용자 정보(manager, regUserId)를 포함해서 보내므로 그대로 사용
            service.create(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로그램이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로그램 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 수정 폼 */
    @GetMapping("/{prgId}/edit")
    public String editForm(@PathVariable("prgId") Long prgId, Model model) {
        ProgramDto item = service.getOne(prgId);
        model.addAttribute("item", item);
        return "employee/program/form";
    }

    /** 수정 처리 (HTML form) */
    @PostMapping("/{prgId}")
    public String update(
            @PathVariable("prgId") Long prgId,
            @ModelAttribute ProgramDto dto
    ) {
        dto.setPrgId(prgId);
        service.update(dto);
        return "redirect:/employee/programs";
    }

    /** 수정 처리 (AJAX JSON) - action 파라미터로 구분 */
    @PostMapping(
            value = "/{prgId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProgramJson(
            @PathVariable("prgId") Long prgId,
            @RequestBody ProgramDto dto,
            @RequestParam(value = "action", required = false) String action
    ) {
        try {
            if (!"update".equals(action)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "잘못된 요청입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 프론트에서 사용자 정보(updUserId)를 포함해서 보내므로 그대로 사용
            dto.setPrgId(prgId);
            service.update(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로그램이 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로그램 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 삭제 처리 */
    @PostMapping("/{prgId}/delete")
    public String delete(@PathVariable("prgId") Long prgId) {
        service.delete(prgId);
        return "redirect:/employee/programs";
    }

    /** JSON으로 프로그램 한 건 조회 (모달 AJAX용) */
    @GetMapping(value = "/{prgId}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProgramDto getProgramJson(@PathVariable("prgId") Long prgId) {
        return service.getOne(prgId);
    }
}