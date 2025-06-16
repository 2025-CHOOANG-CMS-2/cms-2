package kr.ac.dhuniv.mileage.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import kr.ac.dhuniv.mileage.domain.StdMileageHist;
import kr.ac.dhuniv.mileage.domain.StdMileageTotal;
import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.mileage.repository.NcsCmpInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsPrgMileageRepository2;
import kr.ac.dhuniv.mileage.repository.StdMileageHistRepository;
import kr.ac.dhuniv.mileage.repository.StdMileageTotalRepository;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MileageService {
	private final StdMileageHistRepository mileageHistRepository; //학생 마일리지 점수 이력 Repository
    private final NcsCmpInfoRepository2 cmpInfoRepository;        //비교과 프로그램 이수 정보 Repository
    private final StdMileageTotalRepository totalRepository;      //학생 마일리지 총점 Repository
    private final NcsPrgMileageRepository2 prgMileageRepository;  //비교과 프로그램 마일리지 정보 Repository

    @Transactional
    public void payMileageForProgram(Long prgId) {
        List<NcsCmpInfo> completions = cmpInfoRepository.findByProgram_PrgId(prgId);  //해당 프로그램(prgId)을 이수 완료한 이수(학생) 리스트
        NcsPrgMileage prgMileage = prgMileageRepository.findByProgram_PrgId(prgId)    //해당 프로그램(prgId)의 마일리지 점수
        		.orElseThrow(() -> new RuntimeException("해당 프로그램은 아직 마일리지가 할당되지 않습니다."));        //아직 마일리지가 할당되지 않은 프로그램인 경우
        
        for (NcsCmpInfo cmp : completions) {  //이수 완료 리스트 중에서
            if (mileageHistRepository.existsByCompletion_CmpId(cmp.getCmpId())) continue;  //학생 마일리지 점수 이력에 해당 이수 ID가 있다면(마일리지 지급 이력이 있다면) 건너뜀

            StdInfo student = cmp.getStudent(); //이수한 학생 기본 정보
            StdMileageTotal studentTotal = totalRepository.findByStudent_StdId(student.getStdId())  //해당 학생의 누적 마일리지 조회
                    .orElseThrow(() -> new RuntimeException("해당 학생의 마일리지는 없습니다 : " + student.getStdNo())); //누적 마일리지가 없는 경우

            // 마일리지 이력 추가
            StdMileageHist hist = StdMileageHist.builder()
                    .mlgCode(generateMlgCode())                   //mlg_code(마일리지 고유코드)
                    .mileageScore(prgMileage.getMileageScore())   //mlg_score(마일리지 점수)
                    .mileageDate(LocalDateTime.now())             //mlg_dt(획득일자)
                    .additionCode("AUTO")                         //mlg_add_cd(가감코드)
                    .completion(cmp)                              //비교과프로그램 이수 정보 테이블과 연계
                    .programMileage(prgMileage)                   //비교과프로그램 마일리지정보 테이블과 연계 
                    .studentTotal(studentTotal)                   //학생 마일리지 총점 테이블과 연계
                    .build();
            mileageHistRepository.save(hist);                     //학생 마일리지 점수이력을 데이터베이스에 저장

            // 총점 갱신
            BigDecimal updatedScore = studentTotal.getTotalMileageScore().add(prgMileage.getMileageScore()); //기존 누적 마일리지 점수 + 새로 지급한 점
            studentTotal.setTotalMileageScore(updatedScore);      //누적 마일리지
            studentTotal.setLastUpdated(LocalDateTime.now());     //마지막 갱신일자
            totalRepository.save(studentTotal);                   //학생 마일리지 총점을 데이터베이스에 저장
        }
    }

    private String generateMlgCode() {
    	Integer maxCode = mileageHistRepository.findMaxMlgCodeNumber();
        int nextCode = (maxCode != null) ? maxCode + 1 : 1;
        return String.format("MLG%03d", nextCode);   // 예: MLG001, MLG002 ...
    }
    
//    public List<CompletedStudentDto> getAllCompletedStudents() {
//        return cmpInfoRepository.findAllCompletedStudents();
//    }

//    public List<CompletedStudentDto> searchCompletedStudents(String programName, String studentName) {
//        return cmpInfoRepository.findCompletedStudentsByCondition(programName, studentName);
//    }
}
