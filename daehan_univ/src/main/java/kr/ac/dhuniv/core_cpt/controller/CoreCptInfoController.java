package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.coreinfo.CoreCptInfoDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.coreinfo.CoreCptInfoListDTO;
import kr.ac.dhuniv.core_cpt.dto.coreinfo.CoreCptInfoRequestDTO;

import kr.ac.dhuniv.core_cpt.service.CoreCptInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competencies")
@RequiredArgsConstructor
public class CoreCptInfoController {
    private final CoreCptInfoService service;


    // ✅ 최상위 역량 등록 API (POST /api/competencies/root)
    @PostMapping("/root")
    public ResponseEntity<CoreCptInfo> registerRoot(@RequestBody CoreCptInfoRequestDTO dto) {
        CoreCptInfo saved = service.registerAsRoot(dto); // 최상위 등록
        return ResponseEntity.ok(saved); // 등록된 객체 반환
    }

    // ✅ 하위 역량 등록 API (POST /api/competencies/child/{parentId})
    /** 하위 역량 등록 → DTO 반환 */
    @PostMapping("/child/{parentCciId}")
    public ResponseEntity<CoreCptInfoDetailDTO> registerChild(
            @PathVariable("parentCciId") Long parentCciId,
            @RequestBody CoreCptInfoRequestDTO dto) {

        CoreCptInfo saved = service.registerAsChild(parentCciId, dto);
        CoreCptInfoDetailDTO response = service.toDetailDTO(saved);
        return ResponseEntity.ok(response);
    }
    // ✅ [3] 전체 역량 목록 조회 (상위만)
    @GetMapping("/list")
    public ResponseEntity<List<CoreCptInfoListDTO>> getCompetencyList() {
        List<CoreCptInfoListDTO> list = service.getAllTopLevelCompetencies();
        return ResponseEntity.ok(list);
    }
    /*
    @GetMapping("/{cciId}")
    public ResponseEntity<CoreCptInfo> getCompetency(@PathVariable("cciId") String cciId) {
        // 서비스에서 cciId로 조회 후 반환
        CoreCptInfo result = service.getByCciId(cciId);
        return ResponseEntity.ok(result);
    }*/
    /**
     * 상위역량 상세 조회: 이제 CoreCptInfoDetailDTO 반환
     */
    @GetMapping("/{cciId}")
    public ResponseEntity<CoreCptInfoDetailDTO> getCompetencyDetail(
            @PathVariable("cciId") Long cciId) {

        // service 에서 CoreCptInfoDetailDTO 를 만들어 리턴하도록 변경
        CoreCptInfoDetailDTO detail = service.getDetailByCciId(cciId);
        return ResponseEntity.ok(detail);
    }
}
