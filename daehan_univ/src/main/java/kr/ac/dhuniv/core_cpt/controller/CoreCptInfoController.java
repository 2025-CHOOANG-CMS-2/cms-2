package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoListDTO;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoRequestDTO;

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
    @PostMapping("/child/{parentId}")
    public ResponseEntity<CoreCptInfo> registerChild(
            @PathVariable("parentId") String parentId,
            @RequestBody CoreCptInfoRequestDTO dto) {

        CoreCptInfo saved = service.registerAsChild(parentId, dto); // 상위 ID 기준 등록
        return ResponseEntity.ok(saved);
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
    public ResponseEntity<CoreCptInfoDetailDTO> getCompetencyByCciId(
            @PathVariable("cciId") String cciId) {
        // 서비스에서 DTO로 변환된 결과 받아오기
        CoreCptInfoDetailDTO dto = service.getDetailByCciId(cciId);
        return ResponseEntity.ok(dto);
    }
}
