package kr.ac.dhuniv.core_cpt.dto.emp_dashboard;

public class DiagnosisDetailResponseDTO {
    private String upperCciName;
    private Double score;
    private String answeredAt;

    public DiagnosisDetailResponseDTO(String upperCciName, Double score, String answeredAt) {
        this.upperCciName = upperCciName;
        this.score = score;
        this.answeredAt = answeredAt;
    }

    public String getUpperCciName() { return upperCciName; }
    public Double getScore() { return score; }
    public String getAnsweredAt() { return answeredAt; }
}
