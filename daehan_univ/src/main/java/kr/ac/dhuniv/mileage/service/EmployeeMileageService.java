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
import kr.ac.dhuniv.mileage.dto.CoreCptForMlgDTO;
import kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO;
import kr.ac.dhuniv.mileage.dto.employee.MileagePaymentReqDTO;
import kr.ac.dhuniv.mileage.dto.employee.NcsPrgForMlgDTO;
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
    public List<CoreCptForMlgDTO> getAllCoreCompetencies() {
        List<CoreCptInfo> entities = coreCptInfoRepo.findByParentIsNull();
        return entities.stream()
                .map(c -> new CoreCptForMlgDTO(c.getCciId(), c.getCciNm()))
                .collect(Collectors.toList());
    }
    
    // 비교과 프로그램 목록
    public List<NcsPrgForMlgDTO> getAllNcsPrograms() {
        List<NcsPrgInfo> entities = ncsPrgInfoRepo.findAll();
        return entities.stream()
                .map(c -> new NcsPrgForMlgDTO(c.getPrgId(), c.getPrgNm()))
                .collect(Collectors.toList());
    }
    
    // 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    public List<CompletedStudentDTO> getCompletedStudents(LocalDateTime startDate, LocalDateTime endDate, Long competencyId, Long programId) {
        return cmpInfoRepo.findCompletedStudentsByFilter(startDate, endDate, competencyId, programId);
    }
    
    // 마일리지 지급 (학생마일리지 총점 갱신, 학생마일리지 점수 이력 추가)
    @Transactional
    public void saveMileagePayments(List<MileagePaymentReqDTO> requests) {
        for (MileagePaymentReqDTO req : requests) {
            StdInfo student = stdInfoRepo.findByStdId(req.getStdId())
                .orElseThrow(() -> new RuntimeException("학생 없음"));
            NcsCmpInfo completion = cmpInfoRepo.findByStudentAndProgramId(req.getStdId(), req.getPrgId())
                .orElseThrow(() -> new RuntimeException("이수 정보 없음"));
            NcsPrgMileage prgMileage = prgMileageRepo.findByProgram_PrgId(req.getPrgId())
                .orElseThrow(() -> new RuntimeException("프로그램 마일리지 정보 없음"));

            StdMileageTotal total = mileageTotalRepo.findByStudent(student)
                .orElse(StdMileageTotal.builder()
                		.totCode(generateCode("TOT"))       //tot_code(마일리총점 고유코드)
                        .student(student)                   //대상 학생 
                        .totalMileageScore(BigDecimal.ZERO) //마일리지 총점
                        .lastUpdated(LocalDateTime.now())   //마지막 갱신일자
                        .build());

            BigDecimal newScore = req.getMileageScore();
            total.setTotalMileageScore(total.getTotalMileageScore().add(newScore));
            total.setLastUpdated(LocalDateTime.now());
            mileageTotalRepo.save(total);

            StdMileageHist hist = StdMileageHist.builder()
                    .mlgCode(generateCode("MLG"))      //mlg_code(마일리지 고유코드)
                    .mileageScore(newScore)            //mlg_score(마일리지 점수)
                    .mileageDate(LocalDateTime.now())  //마일리지 획득일자
                    .additionCode("plus")              //가감코드 (획득시 'plus', 사용시 'minus')
                    .completion(completion)            //대상 이수정보
                    .programMileage(prgMileage)        //대상 프로그램 마일리지 정보
                    .studentTotal(total)               //대상 학생마일리지 총점
                    .build();

            mileageHistRepo.save(hist);
        }
    }
    
    // 각 테이블 고유코드 생성기
    private String generateCode(String prefix) {
    	Integer maxCode = mileageHistRepo.findMaxMlgCodeNumber();
    	int nextCode = (maxCode != null) ? maxCode + 1 : 1;
    	return String.format(prefix+"%03d", nextCode);   // 예: MLG001, MLG002 ...
    }
}
