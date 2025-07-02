package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.service.NcsPrgInfoService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("students/programs") // 학생용 프로그램 기본 경로
public class StdNcsController {

    private final NcsPrgInfoService ncsPrgInfoService;

    public StdNcsController(NcsPrgInfoService ncsPrgInfoService) {
        this.ncsPrgInfoService = ncsPrgInfoService;
    }

    /**
     * 학생용 프로그램 목록 페이지
     * @param page 현재 페이지 번호
     * @param size 페이지 당 게시물 수
     * @param prgNm 검색할 프로그램명
     * @param status 필터링할 상태
     * @param cciId 필터링할 카테고리 ID
     * @param model 뷰에 전달할 데이터
     * @return 뷰 이름
     */
    @GetMapping // "/list"를 제거하여 /students/programs 경로와 매핑
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

        // 서비스 계층을 호출하여 데이터 조회
        Map<String, Object> result = ncsPrgInfoService.getList(params);

        // 페이징 계산
        int totalCount = (int) result.get("totalCount");
        int totalPages = (totalCount + size - 1) / size;
        int pageNavigationSize = 5;
        int startPage = ((page - 1) / pageNavigationSize) * pageNavigationSize + 1;
        int endPage = Math.min(startPage + pageNavigationSize - 1, totalPages);

        // 뷰에 데이터 전달
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
     * @param prgId 조회할 프로그램 ID
     * @return ProgramDto 객체
     */
    @GetMapping(value = "/{prgId}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProgramDto getProgramJson(@PathVariable("prgId") Long prgId) {
        return ncsPrgInfoService.getOne(prgId);
    }
}