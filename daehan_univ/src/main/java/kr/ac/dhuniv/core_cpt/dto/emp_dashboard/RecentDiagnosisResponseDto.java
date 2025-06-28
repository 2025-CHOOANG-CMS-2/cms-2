package kr.ac.dhuniv.core_cpt.dto.emp_dashboard;

public class RecentDiagnosisResponseDto {

    private String stdNo;
    private String stdNm;
    private String deptName;
    private Integer grade;
    private String diagnosisDate;
    private Double averageScore;

    public RecentDiagnosisResponseDto(String stdNo, String stdNm, String deptName,
                                      Integer grade, String diagnosisDate, Double averageScore) {
        this.stdNo = stdNo;
        this.stdNm = stdNm;
        this.deptName = deptName;
        this.grade = grade;
        this.diagnosisDate = diagnosisDate;
        this.averageScore = averageScore;
    }

    // getter만 있으면 OK
    public String getStdNo() { return stdNo; }
    public String getStdNm() { return stdNm; }
    public String getDeptName() { return deptName; }
    public Integer getGrade() { return grade; }
    public String getDiagnosisDate() { return diagnosisDate; }
    public Double getAverageScore() { return averageScore; }
}