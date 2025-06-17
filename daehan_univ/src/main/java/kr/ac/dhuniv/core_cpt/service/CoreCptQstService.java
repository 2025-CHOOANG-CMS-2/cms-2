package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQstOption;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstRequestDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstOptionRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ CoreCptQstService
 * - 진단 문항 및 선택지에 대한 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class CoreCptQstService {

    private final CoreCptQstRepository qstRepository;           // 진단 문항 리포지토리
    private final CoreCptQstOptionRepository optionRepository; // 선택지 리포지토리
    private final CoreCptInfoRepository cptInfoRepository;      // 역량 정보 리포지토리

    /**
     * ✅ 진단 문항 등록
     * @param dto 진단 문항 + 선택지 정보
     */
    public void addQuestion(CoreCptQstRequestDTO dto) {
        // 상위 역량 정보 조회
        CoreCptInfo cptInfo = cptInfoRepository.findById(dto.getCoreCptInfoId())
                .orElseThrow(() -> new IllegalArgumentException("역량 정보가 존재하지 않습니다. ID=" + dto.getCoreCptInfoId()));

        // 문항 저장
        CoreCptQst qst = CoreCptQst.builder()
                .qstCode(dto.getQstCode())
                .qstCont(dto.getQuestionText())
                .qstOrd(dto.getQstOrd())
                .coreCptInfo(cptInfo)
                .regUserId(dto.getRegUserId())
                .regDt(LocalDateTime.now())
                .build();

        qst = qstRepository.save(qst); // 저장 후 ID 획득

        // 선택지 저장
        for (CoreCptQstRequestDTO.OptionDTO optionDTO : dto.getOptions()) {
            CoreCptQstOption option = CoreCptQstOption.builder()
                    .coreCptQst(qst)
                    .optionText(optionDTO.getText())
                    .score(optionDTO.getScore())
                    .isCorrect(optionDTO.getIsCorrect())
                    .build();

            optionRepository.save(option);
        }
    }

    /**
     * ✅ 진단 문항 삭제
     * @param qstId 삭제할 문항 ID
     */
    public void deleteQuestion(Long qstId) {
        qstRepository.deleteById(qstId); // CASCADE 로 옵션도 같이 삭제됨
    }

    /**
     * ✅ 진단 문항 목록 조회 (간단 예시)
     * @return 문항 DTO 리스트
     */
    public List<CoreCptQstListDTO> getQuestionList() {
        List<CoreCptQst> questions = qstRepository.findAll();
        List<CoreCptQstListDTO> result = new ArrayList<>();

        for (CoreCptQst q : questions) {
            result.add(CoreCptQstListDTO.builder()
                    .qstId(q.getQstId())
                    .qstCode(q.getQstCode())
                    .questionText(q.getQstCont())
                    .competencyName(q.getCoreCptInfo().getCciNm())
                    .build());
        }

        return result;
    }

    /**
     * ✅ 하위 역량의 다음 문항 코드 생성
     * @param subCptId 하위 역량 ID
     * @return 새 문항 코드
     */
    public String generateNextQstCode(Long subCptId) {
        // 하위 역량 존재 여부 확인
        CoreCptInfo subCpt = cptInfoRepository.findById(subCptId)
                .orElseThrow(() -> new IllegalArgumentException("하위 역량이 존재하지 않습니다. ID=" + subCptId));

        // 하위 역량인지 확인
        if (subCpt.getParent() == null) {
            throw new IllegalArgumentException("상위 역량이 아닌 하위 역량 ID를 입력해야 합니다.");
        }

        // 해당 하위 역량의 최대 문항 코드 조회
        String maxCode = qstRepository.findMaxQstCodeBySubCpt(subCptId);

        // 순번 추출
        int nextSeq = 1;
        if (maxCode != null && maxCode.contains("-")) {
            String[] parts = maxCode.split("-");
            try {
                nextSeq = Integer.parseInt(parts[1]) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1; // fallback
            }
        }

        // 문항 코드 생성 (하위 역량 코드 + - + 순번 2자리)
        return subCpt.getCciCode() + "-" + String.format("%02d", nextSeq);
    }
}
