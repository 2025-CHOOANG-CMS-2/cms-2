package kr.ac.dhuniv.mileage.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalMileageIssued;
    private long activePrograms;
    private long studentsWithMileage;
    private double avgMileagePerStudent;
}
