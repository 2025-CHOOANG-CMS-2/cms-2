package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoListDTO;
import kr.ac.dhuniv.core_cpt.dto.CoreCptInfoRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.SubCompetencyDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreCptInfoService {

    private final CoreCptInfoRepository repository;



    /**
     * 최상위 역량용 cciId 생성 (CPT001, CPT002 ...)
     */
    public String generateNextRootCciId() {
        // CPT로 시작하는 항목 중 cciId가 가장 큰 것 조회
        Optional<CoreCptInfo> latest = repository.findTopByCciIdStartingWithOrderByCciIdDesc("CPT");

        // 가장 큰 cciId 가져오기, 없으면 "CPT000" 사용
        String latestId = latest.map(CoreCptInfo::getCciId).orElse("CPT000");

        // 숫자 부분 추출 (CPT003 -> 3)
        int number = Integer.parseInt(latestId.substring(3));

        // 1 증가 후 CPT 포맷으로 리턴
        return String.format("CPT%03d", number + 1);
    }

    /**
     * 하위 역량용 cciId 생성 (SCPT001, SCPT002 ...)
     */
    public String generateNextSubCciId() {
        // SCPT로 시작하는 항목 중 cciId가 가장 큰 것 조회
        Optional<CoreCptInfo> latest = repository.findTopByCciIdStartingWithOrderByCciIdDesc("SCPT");

        // 가장 큰 cciId 가져오기, 없으면 "SCPT000" 사용
        String latestId = latest.map(CoreCptInfo::getCciId).orElse("SCPT000");

        // 숫자 부분 추출 (SCPT003 -> 3)
        int number = Integer.parseInt(latestId.substring(4));

        // 1 증가 후 SCPT 포맷으로 리턴
        return String.format("SCPT%03d", number + 1);
    }
    /**
     * 최상위 역량 등록
     */
    public CoreCptInfo registerAsRoot(CoreCptInfoRequestDTO dto) {
        // 새로운 CPT 코드 생성
        String nextId = generateNextRootCciId();

        // 엔티티 빌드
        CoreCptInfo entity = CoreCptInfo.builder()
                .cciId(nextId)                  // 생성된 CPT 코드
                .parent(null)                   // 최상위 역량은 parent 없음
                .cciNm(dto.getCciNm())          // 사용자 입력명
                .cciDesc(dto.getCciDesc())      // 사용자 입력설명
                .weight(dto.getWeight())        // 사용자 입력 가중치
                .colorHex(dto.getColorHex())    // 사용자 입력 색상
                .regUserId(dto.getRegUserId())  // 등록자 ID
                .regDt(LocalDateTime.now())     // 등록 시각
                .build();

        // 저장 후 반환
        return repository.save(entity);
    }

    /**
     * 하위 역량 등록
     */
    public CoreCptInfo registerAsChild(String parentCciId, CoreCptInfoRequestDTO dto) {
        // 새로운 SCPT 코드 생성
        String nextId = generateNextSubCciId();

        // 상위 역량 엔티티 조회 (없으면 예외 발생)
        CoreCptInfo parent = repository.findByCciId(parentCciId)
                .orElseThrow(() -> new IllegalArgumentException("상위 역량 ID를 찾을 수 없습니다: " + parentCciId));

        // 엔티티 빌드
        CoreCptInfo entity = CoreCptInfo.builder()
                .cciId(nextId)                  // 생성된 SCPT 코드
                .parent(parent)                 // 상위 역량 연결
                .cciNm(dto.getCciNm())          // 사용자 입력명
                .cciDesc(dto.getCciDesc())      // 사용자 입력설명
                .weight(dto.getWeight())        // 사용자 입력 가중치
                .colorHex(dto.getColorHex())    // 사용자 입력 색상
                .regUserId(dto.getRegUserId())  // 등록자 ID
                .regDt(LocalDateTime.now())     // 등록 시각
                .build();

        // 저장 후 반환
        return repository.save(entity);
    }

    /**
     * 최상위 역량 목록 조회 (Stream API 없이)
     */
    public List<CoreCptInfoListDTO> getAllTopLevelCompetencies() {
        // parent가 null인 상위 역량 조회
        List<CoreCptInfo> topList = repository.findByParentIsNull();

        // DTO 리스트 생성
        List<CoreCptInfoListDTO> result = new ArrayList<>();

        // 엔티티 -> DTO 수동 변환
        for (CoreCptInfo entity : topList) {
            CoreCptInfoListDTO dto = CoreCptInfoListDTO.builder()
                    .cciId(entity.getCciId())       // 코드
                    .cciNm(entity.getCciNm())       // 이름
                    .cciDesc(entity.getCciDesc())   // 설명
                    .questionCount(0)               // 질문 수 (나중에 로직 연결)
                    .build();

            result.add(dto); // 리스트에 추가
        }

        return result; // 반환
    }

    /**
     * 상위 역량 + 하위역량을 포함한 상세 DTO 반환
     * 스트림이 아닌 for-loop로 변환 처리
     */
    /**
     * 상세 조회 (상위 + 하위 역량 포함)
     */
    public CoreCptInfoDetailDTO getDetailByCciId(String cciId) {
        // 상위 역량 조회 (없으면 예외)
        CoreCptInfo entity = repository.findByCciId(cciId)
                .orElseThrow(() -> new IllegalArgumentException("해당 cciId를 찾을 수 없습니다: " + cciId));

        // 하위 역량 DTO 리스트 생성
        List<SubCompetencyDTO> subs = new ArrayList<>();

        // 하위 엔티티 -> DTO 변환 (for-loop)
        for (CoreCptInfo child : entity.getChildren()) {
            SubCompetencyDTO subDto = SubCompetencyDTO.builder()
                    .cciId(child.getCciId())         // 코드
                    .cciNm(child.getCciNm())         // 이름
                    .cciDesc(child.getCciDesc())     // 설명
                    .weight(child.getWeight())       // 가중치
                    .build();

            subs.add(subDto); // 리스트에 추가
        }

        // 최종 DTO 조립
        CoreCptInfoDetailDTO detail = CoreCptInfoDetailDTO.builder()
                .cciId(entity.getCciId())               // 코드
                .cciNm(entity.getCciNm())               // 이름
                .cciDesc(entity.getCciDesc())           // 설명
                .weight(entity.getWeight())             // 가중치
                .colorHex(entity.getColorHex())         // 색상
                .questionCount(entity.getQuestions().size()) // 질문 수
                .children(subs)                         // 하위 목록
                .build();

        return detail; // 반환
    }
}