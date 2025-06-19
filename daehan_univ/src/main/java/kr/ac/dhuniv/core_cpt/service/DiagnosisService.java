package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptInfoDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptOptionTemplateDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptQstDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ DiagnosisService
 * - 핵심역량 + 문항 + 선택지 데이터를 조회해 클라이언트에 제공하는 서비스
 */
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final CoreCptInfoRepository coreCptInfoRepository;
    private final CoreCptQstRepository coreCptQstRepository;
    private final CoreCptOptionTemplateRepository coreCptOptionTemplateRepository;

    /**
     * ✅ getDiagnosisQuestions
     * - 상위 핵심역량부터 시작하여 전체 역량 트리 + 문항 + 선택지 정보를 조회
     *
     * @return 핵심역량 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<CoreCptInfoDTO> getDiagnosisQuestions() {
        List<CoreCptInfo> rootCompetencies = coreCptInfoRepository.findByParentIsNullOrderByCciIdAsc();
        return rootCompetencies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * ✅ mapToDto
     * - CoreCptInfo 엔티티를 CoreCptInfoDTO로 변환
     * - 하위 역량 + 문항 + 선택지까지 트리 형태로 변환
     *
     * @param entity CoreCptInfo 엔티티 (상위 또는 하위 핵심역량)
     * @return CoreCptInfoDTO (프론트에 전달할 핵심역량 + 문항 + 선택지 정보)
     */
    private CoreCptInfoDTO mapToDto(CoreCptInfo entity) {
        // ✅ 해당 핵심역량에 속한 문항 목록 조회 및 DTO 변환
        List<CoreCptQstDTO> questions = coreCptQstRepository.findByCoreCptInfoOrderByQstOrdAsc(entity).stream()
                .map(q -> {
                    // ✅ 각 문항에 대한 선택지(option) 목록 조회 및 DTO 변환
                    List<CoreCptOptionTemplateDTO> optionDTOs = coreCptOptionTemplateRepository
                            .findByOptionId(q.getOptionTemplate().getOptionId())  // 문항의 옵션 템플릿 ID로 옵션 목록 조회
                            .stream()
                            .map(o -> CoreCptOptionTemplateDTO.builder()
                                    .optionId(o.getOptionId())          // 옵션 ID
                                    .optionText(o.getOptionText())      // 옵션 텍스트
                                    .ord(o.getOrd())                    // 옵션 출력 순서
                                    .score(o.getScore())                // 옵션 점수
                                    .build())
                            .collect(Collectors.toList());             // 옵션 DTO 리스트로 변환

                    // ✅ 문항 DTO 생성
                    return CoreCptQstDTO.builder()
                            .qstId(q.getQstId())                        // 문항 ID
                            .qstCode(q.getQstCode())                    // 문항 코드
                            .qstCont(q.getQstCont())                    // 문항 내용
                            .qstOrd(q.getQstOrd())                      // 문항 순서
                            .options(optionDTOs)                        // 문항의 선택지 목록
                            .build();
                })
                .collect(Collectors.toList());                          // 문항 DTO 리스트로 변환

        // ✅ 하위 핵심역량이 존재하면 재귀적으로 변환
        List<CoreCptInfoDTO> children = entity.getChildren() != null
                ? entity.getChildren().stream()
                .map(this::mapToDto)                                    // 하위 역량을 다시 mapToDto 로 변환
                .collect(Collectors.toList())                           // 하위 역량 DTO 리스트로 변환
                : List.of();                                            // 하위 역량이 없으면 빈 리스트

        // ✅ 최종 CoreCptInfoDTO 생성 (상위 + 문항 + 하위 역량 포함)
        return CoreCptInfoDTO.builder()
                .cciId(entity.getCciId())                               // 핵심역량 ID
                .cciCode(entity.getCciCode())                           // 핵심역량 코드
                .cciNm(entity.getCciNm())                               // 핵심역량명
                .cciDesc(entity.getCciDesc())                           // 핵심역량 설명
                .colorHex(entity.getColorHex())                         // 색상 코드
                .questions(questions)                                   // 문항 목록
                .children(children)                                     // 하위 역량 목록
                .build();
    }
}