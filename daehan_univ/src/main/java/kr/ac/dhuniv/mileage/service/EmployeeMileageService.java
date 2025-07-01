package kr.ac.dhuniv.mileage.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import kr.ac.dhuniv.mileage.domain.StdMileageHist;
import kr.ac.dhuniv.mileage.domain.StdMileageTotal;
import kr.ac.dhuniv.mileage.dto.employee.CompletedProgramDTO;
import kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO;
import kr.ac.dhuniv.mileage.dto.employee.DashboardStatsDTO;
import kr.ac.dhuniv.mileage.dto.employee.PaymentHistoryDTO;
import kr.ac.dhuniv.mileage.dto.employee.ProgramTopDTO;
import kr.ac.dhuniv.mileage.dto.employee.RecentActivityDTO;
import kr.ac.dhuniv.mileage.dto.employee.MileagePaymentReqDTO;
import kr.ac.dhuniv.mileage.repository.NcsCmpInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsPrgMileageRepository2;
import kr.ac.dhuniv.mileage.repository.StdInfoRepository2;
import kr.ac.dhuniv.mileage.repository.StdMileageHistRepository;
import kr.ac.dhuniv.mileage.repository.StdMileageTotalRepository;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeMileageService {
	private final StdMileageHistRepository mileageHistRepo; //학생 마일리지 점수 이력 Repository
	private final StdMileageTotalRepository mileageTotalRepo;      //학생 마일리지 총점 Repository
    private final NcsCmpInfoRepository2 cmpInfoRepo;        //비교과 프로그램 이수 정보 Repository
    private final NcsPrgMileageRepository2 prgMileageRepo;  //비교과 프로그램 마일리지 정보 Repository
    private final StdInfoRepository2 stdInfoRepo;
    
    // *** 대시보드 ***

    // 금학기 총 지급 마일리지(지급 취소 제외), 마일리지 보유 학생수(재학생 기준, 현재 보유기준), 활성 프로그램수, 학생당 평균 마일리지(재학생 기준, 현재 보유기준)
    public DashboardStatsDTO getStats() {
        long totalMileageIssued = mileageHistRepo.sumMileageByPeriod(getSemester().get(0), LocalDateTime.now());
        long activePrograms = prgMileageRepo.countActivePrograms(getSemester().get(0), getSemester().get(1));
        long studentsWithMileage = mileageTotalRepo.countActiveStudentsWithMileage();
        double avgMileagePerStudent = mileageTotalRepo.avgMileagePerActiveStudent();
        return new DashboardStatsDTO(totalMileageIssued, activePrograms, studentsWithMileage, avgMileagePerStudent);
    }

    // 금학기 상위 5개 프로그램 조회 (지급된 마일리지합계 기준 내림차순, 총지급 마일리지에서 지급취소는 제외)
    public List<ProgramTopDTO> getTop5Programs(Pageable top5) {
        return mileageHistRepo.findTop5Programs(getSemester().get(0), LocalDateTime.now(), top5);
    }

    // 최근 마일리지 지급 활동 내역 조회 (최근 10건, 지급한 총 마일리지에서 지급취소는 제외)
    public List<RecentActivityDTO> getRecentActivities(Pageable latest10) {
        return mileageHistRepo.findRecentActivities(latest10);
    }
    
    // *** 프로그램 연동 마일리지 지급 ***
    
    // 이수완료 되었지만 마일리지가 지급되지 않은 프로그램 목록 조회
    public List<CompletedProgramDTO> findCompletedButUnpaidPrograms() {
        return cmpInfoRepo.findCompletedButUnpaidPrograms();
    }
    
    // 해당 프로그램 이수자 목록 조회
    public List<CompletedStudentDTO> findCompletedStudents(Long programId, LocalDateTime completeDate){
    	return cmpInfoRepo.findCompletedStudents(programId, completeDate);
    }
    
    // *** 프로그램 연동 마일리지 지급(화면 구성이 다름) ***
    
    // 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    public List<CompletedStudentDTO> getCompletedStudents(LocalDateTime startDate, LocalDateTime endDate, Long competencyId, Long programId) {
        return cmpInfoRepo.findCompletedStudentsByFilter(startDate, endDate, competencyId, programId);
    }
    
    // 마일리지 지급 (학생마일리지 총점 갱신, 학생마일리지 점수 이력 추가)
    @Transactional
    public void saveMileagePayments(List<MileagePaymentReqDTO> requests) {
    	// mlg_code(마일리지 코드) 생성 : 일괄지급이던 개별지급이던 마일리지 코드 1개 생성 (unique 하지 않음)
    	String mlgCode = generateCode("MLG");
    	
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
                    .mlgCode(mlgCode)                  //mlg_code(마일리지 코드)
                    .mileageScore(newScore)            //mlg_score(마일리지 점수)
                    .mileageDate(LocalDateTime.now())  //마일리지 획득일자
                    .additionCode("PLUS")              //가감코드 (획득시 'PLUS', 사용시 'MINUS')
                    .mlgStatCode("COM")                //지급상태 (지급시 'COM' 지급취시 'CAN')
                    .completion(completion)            //대상 이수정보
                    .programMileage(prgMileage)        //대상 프로그램 마일리지 정보
                    .studentTotal(total)               //대상 학생마일리지 총점
                    .build();

            mileageHistRepo.save(hist);
        }
    }
    
    // *** 마일리지 지급내역 조회 및 지급 취소 ***
    
    // 전체 마일리지 지급 내역 (검색 및 페이징 적용)
    public Page<PaymentHistoryDTO> getHistory(
    		LocalDateTime startDate,
    		LocalDateTime endDate,
            Long competencyId,
            Long programId,
            String mlgStatCode,
            Pageable pageable) {
    	return mileageHistRepo.findHistory(startDate, endDate, competencyId, programId, mlgStatCode, pageable);
    }
    
    // 마일리지 단건 취소
    @Transactional
    public void cancelPayment(Long id) {
        // 지급 이력 조회 (학생, 점수 포함해서 fetch)
        StdMileageHist hist = mileageHistRepo.findWithStudentTotalById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 마일리지 이력을 찾을 수 없습니다."));

        if (!"COM".equals(hist.getMlgStatCode())) {
            throw new IllegalStateException("지급 완료된 이력만 취소할 수 있습니다.");
        }

        // 상태 변경
        hist.setMlgStatCode("CAN");

        // StdMileageTotal 마일리지 차감
        StdMileageTotal total = hist.getStudentTotal();
        total.setTotalMileageScore(total.getTotalMileageScore().subtract(hist.getMileageScore()));
    }

    // 마일리지 복수 취소
    @Transactional
    public void bulkCancel(List<Long> ids) {
        List<StdMileageHist> hists = mileageHistRepo.findAllWithStudentTotalByIds(ids);
        
        

        for (StdMileageHist hist : hists) {
            if (!"COM".equals(hist.getMlgStatCode())) continue;

            hist.setMlgStatCode("CAN");

            StdMileageTotal total = hist.getStudentTotal();
            total.setTotalMileageScore(total.getTotalMileageScore().subtract(hist.getMileageScore()));

        }
    }
    
//    // 마일리지 단건 취소
//    @Transactional
//    public void cancelPayment(Long id) {
//    	mileageHistRepo.cancelById(id);
//    }
//
//    // 마일리지 복수 취소
//    @Transactional
//    public void bulkCancel(List<Long> ids) {
//    	mileageHistRepo.cancelByIds(ids);
//    }
    
    // *** 공통 코드 ***
    
    // 각 테이블 고유코드 생성기
    private String generateCode(String prefix) {
    	Integer maxCode = mileageHistRepo.findMaxMlgCodeNumber();
    	int nextCode = (maxCode != null) ? maxCode + 1 : 1;
    	return String.format(prefix+"%03d", nextCode);   // 예: MLG001, MLG002 ...
    }
    
    // 학기 설정 (1학기 : 3월 ~ 8월, 2학기 : 9월 ~ 다음년 2월)
    private List<LocalDateTime> getSemester() {
    	List<LocalDateTime> semister = new ArrayList<LocalDateTime>();
    	LocalDateTime start = null;
    	LocalDateTime end = null;
    	LocalDate today = LocalDate.now();
    	int month = today.getMonthValue();
    	if (month >= 3 && month <= 8) {
    		// 1학기: 해당 년도의 3월 1일 ~ 8월 31
    		start = LocalDate.of(today.getYear(), 3, 1).atStartOfDay();
    		end = LocalDate.of(today.getYear(), 8, 31).atTime(LocalTime.MAX);
    	} else if (month >= 9) {
    		// 2학기: 해당 년도의 9월 1일 ~ 다음년도 2월 28
    		start = LocalDate.of(today.getYear(), 9, 1).atStartOfDay();
    		end = LocalDate.of(today.getYear() + 1, 2, 28).atTime(LocalTime.MAX);       	
    	} else {
    		// 1월 ~ 2월은 전년도 2학기이므로 → 전년도 9월 1일 ~ 해당 년도 2월 28
    		start = LocalDate.of(today.getYear() - 1, 9, 1).atStartOfDay();
    		end = LocalDate.of(today.getYear(), 2, 28).atTime(LocalTime.MAX);
    	}
    	semister.add(start);
    	semister.add(end);
    	return semister;
    }
    
}
