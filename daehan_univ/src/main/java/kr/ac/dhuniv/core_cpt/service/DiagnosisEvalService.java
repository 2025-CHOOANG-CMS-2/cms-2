package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
import kr.ac.dhuniv.core_cpt.dto.eval.CoreCptEvalRequestDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptEvalRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCpt_StdInfoRepository;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ✅ DiagnosisEvalService
 * - CoreCptEval 저장 로직 처리
 */
@Service
@RequiredArgsConstructor
public class DiagnosisEvalService {

    private final CoreCptEvalRepository coreCptEvalRepository;
    private final CoreCpt_StdInfoRepository stdInfoRepository;
    private final CoreCptQstRepository coreCptQstRepository;
    private final CoreCptOptionTemplateRepository coreCptOptionTemplateRepository;

    /**
     * ✅ saveDiagnosisResponses
     * - 클라이언트로부터 받은 진단 응답을 DB에 저장
     *
     * @param requestDTO 클라이언트 요청 데이터
     */
    @Transactional
    public void saveDiagnosisResponses(CoreCptEvalRequestDTO requestDTO) {
        // 학생 조회
        StdInfo student = stdInfoRepository.findById(requestDTO.getStdNo())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + requestDTO.getStdNo()));

        for (CoreCptEvalRequestDTO.Answer answer : requestDTO.getAnswers()) {
            // 문항 조회
            CoreCptQst question = coreCptQstRepository.findById(answer.getQstId())
                    .orElseThrow(() -> new IllegalArgumentException("문항을 찾을 수 없습니다: " + answer.getQstId()));

            // 선택지 조회
            CoreCptOptionTemplate option = coreCptOptionTemplateRepository.findById(answer.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("선택지를 찾을 수 없습니다: " + answer.getOptionId()));

            // CoreCptEval 생성 및 저장
            CoreCptEval eval = CoreCptEval.builder()
                    .evalCode(requestDTO.getEvalCode())          // 비즈니스 키
                    .answerDate(requestDTO.getAnswerDate())      // 응답 시간
                    .student(student)                            // 학생 연관
                    .question(question)                          // 문항 연관
                    .selectedOption(option)                      // 선택지 연관
                    .build();

            coreCptEvalRepository.save(eval);
        }
    }
}