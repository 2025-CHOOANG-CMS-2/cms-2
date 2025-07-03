package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.dto.MyProgramDto;
import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.service.NcsPrgAplyService;
import kr.ac.dhuniv.ncs.service.NcsPrgInfoService;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("students/programs")
public class StdNcsController {

    private final NcsPrgInfoService ncsPrgInfoService;
    private final NcsPrgAplyService ncsPrgAplyService;
    private final UserRepository userRepository;

    public StdNcsController(NcsPrgInfoService ncsPrgInfoService, NcsPrgAplyService ncsPrgAplyService, UserRepository userRepository) {
        this.ncsPrgInfoService = ncsPrgInfoService;
        this.ncsPrgAplyService = ncsPrgAplyService;
        this.userRepository = userRepository;
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
     */
    @PostMapping("/{prgId}/apply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyProgram(
            @PathVariable("prgId") Long prgId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String username = userDetails.getUsername();
            User user = userRepository.findByUserId(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
            
            Long studentPkId = user.getUserIdx();

            ncsPrgAplyService.applyForProgram(prgId, studentPkId);
            
            response.put("success", true);
            response.put("message", "프로그램 신청이 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 내 프로그램 참여 현황 페이지
     */
    @GetMapping("/status")
    public String myProgramStatus(Model model) {
        try {
            // 1. 학번을 하드코딩합니다.
            String username = "2025005001"; 

            // 2. 하드코딩된 학번으로 DB에서 사용자의 고유 ID(PK)를 조회합니다.
            User user = userRepository.findByUserId(username)
                    .orElseThrow(() -> new UsernameNotFoundException("테스트용 학생 정보(" + username + ")를 DB에서 찾을 수 없습니다."));
            
            Long studentPkId = user.getUserIdx();

            // 3. 조회한 ID로 참여 현황을 가져옵니다.
            List<MyProgramDto> myPrograms = ncsPrgAplyService.getMyApplicationStatus(studentPkId);
            model.addAttribute("myPrograms", myPrograms);

        } catch (Exception e) {
            // 예외 발생 시 에러 메시지를 전달하고 빈 목록을 보여줍니다.
            System.err.println("/status 에러: " + e.getMessage());
            model.addAttribute("myPrograms", new ArrayList<>()); 
            model.addAttribute("error", e.getMessage());
        }

        return "student/program/status";
    }
}