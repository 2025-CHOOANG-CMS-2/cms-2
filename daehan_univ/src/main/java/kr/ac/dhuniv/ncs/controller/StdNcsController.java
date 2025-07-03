package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.service.NcsPrgAplyService;
import kr.ac.dhuniv.ncs.service.NcsPrgInfoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("students/programs")
public class StdNcsController {

    private final NcsPrgInfoService ncsPrgInfoService;
    private final NcsPrgAplyService ncsPrgAplyService;

    public StdNcsController(NcsPrgInfoService ncsPrgInfoService, NcsPrgAplyService ncsPrgAplyService) {
        this.ncsPrgInfoService = ncsPrgInfoService;
        this.ncsPrgAplyService = ncsPrgAplyService;
    }

    /**
     * 학생용 프로그램 목록 페이지
     */
    @GetMapping
    public String programList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "9") int size,
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

        Map<String, Object> result = ncsPrgInfoService.getList(params);

        int totalCount = (int) result.get("totalCount");
        int totalPages = (totalCount + size - 1) / size;
        int pageNavigationSize = 5;
        int startPage = ((page - 1) / pageNavigationSize) * pageNavigationSize + 1;
        int endPage = Math.min(startPage + pageNavigationSize - 1, totalPages);

        model.addAttribute("list", result.get("list"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("prgNm", prgNm);
        model.addAttribute("status", status);
        model.addAttribute("cciId", cciId);

        return "student/program/index";
    }

    /**
     * 상세보기 모달을 위한 프로그램 단건 조회 API (JSON)
     */
    @GetMapping(value = "/{prgId}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProgramDto getProgramJson(@PathVariable("prgId") Long prgId) {
        return ncsPrgInfoService.getOne(prgId);
    }

    /**
     * 프로그램 신청 처리 API
     * @param prgId 신청할 프로그램 ID
     * @param payload 프론트엔드에서 전송한 학생 ID가 담긴 데이터
     * @return 처리 결과
     */
    @PostMapping("/{prgId}/apply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyProgram(
            @PathVariable("prgId") Long prgId,
            @RequestBody Map<String, Object> payload
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // ▼▼▼ [핵심] userIdx 대신 stdId를 받도록 수정 ▼▼▼
            Object stdIdObj = payload.get("stdId");
            
            if (stdIdObj == null) {
                throw new IllegalArgumentException("학생 ID(stdId)가 전송되지 않았습니다.");
            }
            
            Long studentId = Long.parseLong(stdIdObj.toString());

            ncsPrgAplyService.applyForProgram(prgId, studentId);
            
            response.put("success", true);
            response.put("message", "프로그램 신청이 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}