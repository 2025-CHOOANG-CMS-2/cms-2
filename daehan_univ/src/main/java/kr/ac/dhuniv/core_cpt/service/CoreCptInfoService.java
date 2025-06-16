package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
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

@Service
@RequiredArgsConstructor
public class CoreCptInfoService {

    private final CoreCptInfoRepository repository;



    /**
     * 최상위 역량용 cciId 생성 (CPT001, CPT002 ...)
     */
    public String generateNextRootCciId() {
        // CPT로 시작하는 항목 중 cciId가 가장 큰 것 조회
        Optional<CoreCptInfo> latest = repository.findTopByCciCodeStartingWithOrderByCciCodeDesc("CPT");

        // 가장 큰 cciId 가져오기, 없으면 "CPT000" 사용
        String latestId = latest.map(CoreCptInfo::getCciCode).orElse("CPT000");

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
        Optional<CoreCptInfo> latest = repository.findTopByCciCodeStartingWithOrderByCciCodeDesc("SCPT");

        // 가장 큰 cciId 가져오기, 없으면 "SCPT000" 사용
        String latestId = latest.map(CoreCptInfo::getCciCode).orElse("SCPT000");

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
                .cciCode(nextId)                  // 생성된 CPT 코드
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
    public CoreCptInfo registerAsChild(Long parentCciId, CoreCptInfoRequestDTO dto) {
        // 새로운 SCPT 코드 생성
        String nextId = generateNextSubCciId();

        // 상위 역량 엔티티 조회 (없으면 예외 발생)
        CoreCptInfo parent = repository.findByCciId(parentCciId)
                .orElseThrow(() -> new IllegalArgumentException("상위 역량 ID를 찾을 수 없습니다: " + parentCciId));

        // 엔티티 빌드
        CoreCptInfo entity = CoreCptInfo.builder()
                .cciCode(nextId)                  // 생성된 SCPT 코드
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
                    .cciCode(entity.getCciCode())       // 코드
                    .cciNm(entity.getCciNm())       // 이름
                    .cciDesc(entity.getCciDesc())   // 설명
                    .questionCount(0)               // 질문 수 (나중에 로직 연결)
                    .build();

            result.add(dto); // 리스트에 추가
        }

        return result; // 반환
    }

    /**
     * 상위 cciId 로 상세 정보 조회 (하위 역량 포함)
     */
    public CoreCptInfoDetailDTO getDetailByCciId(Long cciId) {
        // (1) repository 를 통해 해당 cciId 의 CoreCptInfo 엔티티를 조회
        CoreCptInfo entity = repository.findByCciId(cciId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 cciId를 찾을 수 없습니다: " + cciId));

        // (2) 하위 역량 DTO 를 담을 빈 리스트 생성
        List<SubCompetencyDTO> subs = new ArrayList<>();

        // (3) 엔티티의 children(하위 역량) 엔티티들을 순회
        for (CoreCptInfo child : entity.getChildren()) {
            // (3-1) 각 하위 역량 엔티티를 DTO 로 변환
            SubCompetencyDTO subDto = new SubCompetencyDTO();
            subDto.setCciCode(child.getCciCode());     // 하위 역량 코드
            subDto.setCciNm(child.getCciNm());     // 하위 역량명
            subDto.setCciDesc(child.getCciDesc()); // 하위 역량 설명
            subDto.setWeight(child.getWeight());   // 하위 역량 가중치

            // (3-2) 변환한 DTO 를 리스트에 추가
            subs.add(subDto);
        }

        // (4) CoreCptInfoDetailDTO 인스턴스 생성 및 상위 정보 설정
        CoreCptInfoDetailDTO detailDto = new CoreCptInfoDetailDTO();
        detailDto.setCciCode(entity.getCciCode());               // 상위 역량 코드
        detailDto.setCciNm(entity.getCciNm());               // 상위 역량명
        detailDto.setCciDesc(entity.getCciDesc());           // 상위 역량 설명
        detailDto.setWeight(entity.getWeight());             // 상위 역량 가중치
        detailDto.setColorHex(entity.getColorHex());         // 상위 역량 표시 색상
        detailDto.setQuestionCount(entity.getQuestions().size()); // 상위 역량 문항 수
        detailDto.setChildren(subs);                         // (3)에서 만든 하위 역량 리스트

        // (5) 최종 DTO 반환
        return detailDto;
    }
    public CoreCptInfoDetailDTO toDetailDTO(CoreCptInfo entity) {
        // (이미 작성하신 for-loop 기반 SubCompetencyDTO 변환 로직과 동일)
        CoreCptInfoDetailDTO dto = new CoreCptInfoDetailDTO();
        dto.setCciCode(entity.getCciCode());
        dto.setCciNm(entity.getCciNm());
        dto.setCciDesc(entity.getCciDesc());
        dto.setWeight(entity.getWeight());
        dto.setColorHex(entity.getColorHex());
        // 질문 수 계산 시 null 체크
        List<CoreCptQst> questions = entity.getQuestions();
        int qCount = (questions != null) ? questions.size() : 0;
        dto.setQuestionCount(qCount);


        List<SubCompetencyDTO> subs = new ArrayList<>();
        for (CoreCptInfo child : entity.getChildren()) {
            SubCompetencyDTO sub = new SubCompetencyDTO();
            sub.setCciCode(child.getCciCode());
            sub.setCciNm(child.getCciNm());
            sub.setCciDesc(child.getCciDesc());
            sub.setWeight(child.getWeight());
            subs.add(sub);
        }
        dto.setChildren(subs);

        return dto;
    }
}