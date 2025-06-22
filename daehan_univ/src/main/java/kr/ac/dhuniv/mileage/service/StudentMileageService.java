package kr.ac.dhuniv.mileage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.mileage.domain.StdMileageTotal;
import kr.ac.dhuniv.mileage.dto.student.CompetencyStat;
import kr.ac.dhuniv.mileage.dto.student.MileageOverviewDTO;
import kr.ac.dhuniv.mileage.dto.student.MonthlyStat;
import kr.ac.dhuniv.mileage.repository.CoreCptInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsCmpInfoRepository2;
import kr.ac.dhuniv.mileage.repository.StdInfoRepository2;
import kr.ac.dhuniv.mileage.repository.StdMileageHistRepository;
import kr.ac.dhuniv.mileage.repository.StdMileageTotalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentMileageService {

	private final StdMileageHistRepository mlgHistRepo;
	private final StdMileageTotalRepository mlgTotalRepo;
	private final NcsCmpInfoRepository2 cmpInfoRepo;
    private final CoreCptInfoRepository2 cptInfoRepo;
    private final StdInfoRepository2 stdInfoRepo;

    // 학생 마일리지 현황
    /*
		{
			"totalMileage": 20,
			"totalEarned": 20,
			"totalUsed": 0,
			"programCount": 1,
			"rank": 1,
			"rankingPercent": 9,
			"lastUpdated": "2025-06-20",
			"monthly": {
				"periodStart": "2025-06-10",
				"periodEnd": "2025-05-12",
				"earned": 0,
				"used": 0,
				"earnedDiff": 0,
				"usedDiff": 0
			},
			"competencies": [
				{
					"competencyName": "의사소통역량",
					"mileagePoint": 0,
					"percent": 0
				},
				{
					"competencyName": "창의역량",
					"mileagePoint": 0,
					"percent": 0
				},
				{
					"competencyName": "문제해결역량",
					"mileagePoint": 0,
					"percent": 0
				},
				{
					"competencyName": "리더십역량",
					"mileagePoint": 20,
					"percent": 100
				},
				{
					"competencyName": "팀워크역량",
					"mileagePoint": 0,
					"percent": 0
				},
				{
					"competencyName": "자기관리역량",
					"mileagePoint": 0,
					"percent": 0
				},
				{
					"competencyName": "정보활용역량",
					"mileagePoint": 0,
					"percent": 0
				}
			]
		}
     */
    public MileageOverviewDTO getOverview(String userId) {
        StdMileageTotal total = mlgTotalRepo.findByStudentUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("총 마일리지 없음"));

        BigDecimal earned = mlgHistRepo.sumMileageByStudentId(userId, "plus");
        BigDecimal used = mlgHistRepo.sumMileageByStudentId(userId, "minus");
        int programCount = mlgHistRepo.countDistinctPrograms(userId);
        LocalDate lastUpdated = total.getLastUpdated().toLocalDate();

        // 월별
        LocalDateTime now = LocalDate.now().atStartOfDay(); // 오늘 0시 0분 0초
        LocalDateTime elevenDaysAgo = now.minusDays(11);
        LocalDateTime fortyDaysAgo = now.minusDays(40);
        LocalDateTime fortyoneDaysAgo = now.minusDays(41);
        LocalDateTime seventyDaysAgo = now.minusDays(70);
        
        BigDecimal monthlyEarned = mlgHistRepo.sumMileageInPeriod(userId, elevenDaysAgo, fortyDaysAgo.withHour(23).withMinute(59).withSecond(59), "plus");
        BigDecimal monthlyUsed = mlgHistRepo.sumMileageInPeriod(userId,  elevenDaysAgo, fortyDaysAgo.withHour(23).withMinute(59).withSecond(59), "minus");

        BigDecimal prevMonthEarned = mlgHistRepo.sumMileageInPeriod(userId, fortyoneDaysAgo, seventyDaysAgo, "plus");
        BigDecimal prevMonthUsed = mlgHistRepo.sumMileageInPeriod(userId, fortyoneDaysAgo, seventyDaysAgo, "minus");

        // 핵심역량별 분포
        List<CoreCptInfo> allCompetencies = cptInfoRepo.findByParentIsNull();
        Map<String, BigDecimal> competencyTotals = mlgHistRepo.sumByCoreCompetency(userId);

        List<CompetencyStat> competencyStats = allCompetencies.stream().map(c -> {
            BigDecimal points = competencyTotals.getOrDefault(c.getCciNm(), BigDecimal.ZERO);
            return new CompetencyStat(c.getCciNm(), points, 0);
        }).collect(Collectors.toList());

        BigDecimal totalPoints = competencyStats.stream()
                .map(CompetencyStat::getMileagePoint)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 핵심역량별 % 계산
        competencyStats.forEach(c -> {
            double percent = totalPoints.compareTo(BigDecimal.ZERO) > 0
                ? c.getMileagePoint().multiply(BigDecimal.valueOf(100)).divide(totalPoints, 0, RoundingMode.HALF_UP).doubleValue()
                : 0;
            c.setPercent((int) percent);
        });

        int rank = calculateRank(userId);                       // 마일리지 보유 순위
        int stdCount = stdInfoRepo.countByStatusCode("ENROLL"); // 재학중인 학생수
        int percent = calculateRankingPercent(rank, stdCount);  // 마일리지 보유 순위의 백분율
        
        // 빌드
        return MileageOverviewDTO.builder()
                .totalMileage(total.getTotalMileageScore())
                .totalEarned(earned)
                .totalUsed(used)
                .programCount(programCount)
                .rank(rank)
                .rankingPercent(percent)
                .lastUpdated(lastUpdated)
                .monthly(new MonthlyStat(
                		elevenDaysAgo.toLocalDate(),
                		fortyDaysAgo.toLocalDate(), 
                		monthlyEarned, 
                		monthlyUsed, 
                		monthlyEarned.subtract(prevMonthEarned), 
                		monthlyUsed.subtract(prevMonthUsed)))
                .competencies(competencyStats)
                .build();
    }
    
    // 해당학생의 마일리지 총점 순위 반환
    public int calculateRank(String userId) {
        List<StdMileageTotal> allTotals = mlgTotalRepo.findAllByOrderByTotalMileageScoreDesc();
        for (int i = 0; i < allTotals.size(); i++) {
            if (allTotals.get(i).getStudent().getUser().getUserId().equals(userId)) {
                return i + 1; 
            }
        }
        return -1; // 해당 학생이 없을 경우
    }

    // 해당학생의 마일리지 총점 순위 백분율 반환
    public int calculateRankingPercent(int rank, int stdCount) {
        if (stdCount == 0) return 0;
        double percent = (double) rank / stdCount * 100;
        return (int) Math.round(percent);
    }

}
