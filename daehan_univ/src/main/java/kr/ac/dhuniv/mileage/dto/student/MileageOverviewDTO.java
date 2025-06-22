package kr.ac.dhuniv.mileage.dto.student;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MileageOverviewDTO {
    private BigDecimal totalMileage;
    private BigDecimal totalEarned;
    private BigDecimal totalUsed;
    private int programCount;
    private int rank;
    private int rankingPercent;
    private LocalDate lastUpdated;
    private MonthlyStat monthly;
    private List<CompetencyStat> competencies;
}
