package kr.ac.dhuniv.mileage.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import kr.ac.dhuniv.mileage.domain.StdMileageHist;
import kr.ac.dhuniv.mileage.domain.StdMileageTotal;
import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.mileage.dto.CoreCptForMlgDto;
import kr.ac.dhuniv.mileage.dto.MileagePaymentRequest;
import kr.ac.dhuniv.mileage.dto.NcsPrgForMlgDto;
import kr.ac.dhuniv.mileage.repository.CoreCptInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsCmpInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsPrgInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsPrgMileageRepository2;
import kr.ac.dhuniv.mileage.repository.StdInfoRepository2;
import kr.ac.dhuniv.mileage.repository.StdMileageHistRepository;
import kr.ac.dhuniv.mileage.repository.StdMileageTotalRepository;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeMileageService {
	private final StdMileageHistRepository mileageHistRepo; //학생 마일리지 점수 이력 Repository
	private final StdMileageTotalRepository mileageTotalRepo;      //학생 마일리지 총점 Repository
    private final NcsCmpInfoRepository2 cmpInfoRepo;        //비교과 프로그램 이수 정보 Repository
    private final NcsPrgMileageRepository2 prgMileageRepo;  //비교과 프로그램 마일리지 정보 Repository
    private final CoreCptInfoRepository2 coreCptInfoRepo;
    private final NcsPrgInfoRepository2 ncsPrgInfoRepo;
    private final StdInfoRepository2 stdInfoRepo;

    // 핵심역량 목록
    public List<CoreCptForMlgDto> getAllCoreCompetencies() {
        List<CoreCptInfo> entities = coreCptInfoRepo.findByParentIsNull();
        return entities.stream()
                .map(c -> new CoreCptForMlgDto(c.getCciId(), c.getCciNm()))
                .collect(Collectors.toList());
    }
    
    // 비교과 프로그램 목록
    public List<NcsPrgForMlgDto> getAllNcsPrograms() {
        List<NcsPrgInfo> entities = ncsPrgInfoRepo.findAll();
        return entities.stream()
                .map(c -> new NcsPrgForMlgDto(c.getPrgId(), c.getPrgNm()))
                .collect(Collectors.toList());
    }
    
    // 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    public List<CompletedStudentDto> getCompletedStudents(LocalDateTime startDate, LocalDateTime endDate, Long coreCptId, Long programId) {
        return cmpInfoRepo.findCompletedStudentsByFilter(startDate, endDate, coreCptId, programId);
    }
    
    @Transactional
    public void saveMileagePayments(List<MileagePaymentRequest> requests) {
        for (MileagePaymentRequest req : requests) {
            StdInfo student = stdInfoRepo.findByStdId(req.getStdId())
                .orElseThrow(() -> new RuntimeException("학생 없음"));
            NcsCmpInfo completion = cmpInfoRepo.findByStudentAndProgramId(req.getStdId(), req.getPrgId())
                .orElseThrow(() -> new RuntimeException("이수 정보 없음"));
            NcsPrgMileage prgMileage = prgMileageRepo.findByProgram_PrgId(req.getPrgId())
                .orElseThrow(() -> new RuntimeException("프로그램 마일리지 정보 없음"));

            StdMileageTotal total = mileageTotalRepo.findByStudent(student)
                .orElse(StdMileageTotal.builder()
                        .student(student)
                        .totalMileageScore(BigDecimal.ZERO)
                        .lastUpdated(LocalDateTime.now())
                        .build());

            BigDecimal newScore = req.getMileageScore();
            total.setTotalMileageScore(total.getTotalMileageScore().add(newScore));
            total.setLastUpdated(LocalDateTime.now());
            mileageTotalRepo.save(total);

            StdMileageHist hist = StdMileageHist.builder()
                    .mlgCode(generateCode("MLG"))      //mlg_code(마일리지 고유코드)
                    .mileageScore(newScore)            //mlg_score(마일리지 점수)
                    .mileageDate(LocalDateTime.now())
                    .additionCode("AD01") // 필요 시 값 설정
                    .completion(completion)
                    .programMileage(prgMileage)
                    .studentTotal(total)
                    .build();

            mileageHistRepo.save(hist);
        }
    }
    
    @Transactional
    public void payMileageForProgram(Long prgId) {
    	List<NcsCmpInfo> completions = cmpInfoRepo.findByProgram_PrgId(prgId);  //해당 프로그램(prgId)을 이수 완료한 이수(학생) 리스트
    	NcsPrgMileage prgMileage = prgMileageRepo.findByProgram_PrgId(prgId)    //해당 프로그램(prgId)의 마일리지 점수
    			.orElseThrow(() -> new RuntimeException("해당 프로그램은 아직 마일리지가 할당되지 않습니다."));        //아직 마일리지가 할당되지 않은 프로그램인 경우
    	
    	for (NcsCmpInfo cmp : completions) {  //이수 완료 리스트 중에서
    		if (mileageHistRepo.existsByCompletion_CmpId(cmp.getCmpId())) continue;  //학생 마일리지 점수 이력에 해당 이수 ID가 있다면(마일리지 지급 이력이 있다면) 건너뜀
    		
    		StdInfo student = cmp.getStudent(); //이수한 학생 기본 정보
    		StdMileageTotal studentTotal = mileageTotalRepo.findByStudent_StdId(student.getStdId())  //해당 학생의 누적 마일리지 조회
    				.orElseThrow(() -> new RuntimeException("해당 학생의 마일리지는 없습니다 : " + student.getStdNo())); //누적 마일리지가 없는 경우
    		
    		
    		// 마일리지 이력 추가
    		StdMileageHist hist = StdMileageHist.builder()
    				.mlgCode(generateCode("MLG"))                   //mlg_code(마일리지 고유코드)
    				.mileageScore(prgMileage.getMileageScore())   //mlg_score(마일리지 점수)
    				.mileageDate(LocalDateTime.now())             //mlg_dt(획득일자)
    				.additionCode("AUTO")                         //mlg_add_cd(가감코드)
    				.completion(cmp)                              //비교과프로그램 이수 정보 테이블과 연계
    				.programMileage(prgMileage)                   //비교과프로그램 마일리지정보 테이블과 연계 
    				.studentTotal(studentTotal)                   //학생 마일리지 총점 테이블과 연계
    				.build();
    		mileageHistRepo.save(hist);                     //학생 마일리지 점수이력을 데이터베이스에 저장
    		
    		// 총점 갱신
    		BigDecimal updatedScore = studentTotal.getTotalMileageScore().add(prgMileage.getMileageScore()); //기존 누적 마일리지 점수 + 새로 지급한 점
    		studentTotal.setTotalMileageScore(updatedScore);      //누적 마일리지
    		studentTotal.setLastUpdated(LocalDateTime.now());     //마지막 갱신일자
    		mileageTotalRepo.save(studentTotal);                   //학생 마일리지 총점을 데이터베이스에 저장
    	}
    }
    
    private String generateCode(String prefix) {
    	Integer maxCode = mileageHistRepo.findMaxMlgCodeNumber();
    	int nextCode = (maxCode != null) ? maxCode + 1 : 1;
    	return String.format(prefix+"%03d", nextCode);   // 예: MLG001, MLG002 ...
    }
}
