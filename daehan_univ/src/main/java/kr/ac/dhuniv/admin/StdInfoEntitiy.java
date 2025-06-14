package kr.ac.dhuniv.admin;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // AllArgsConstructor는 필요에 따라 추가


import java.time.LocalDateTime; // Java 8 이상 날짜/시간 타입 사용

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // 이 클래스가 JPA 엔티티임을 명시합니다.
@Table(name = "STD_INFO") // 매핑될 데이터베이스 테이블 이름을 지정합니다.
@Getter // Lombok: 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: protected 접근 레벨의 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다. (필요에 따라 사용)
@Builder // Lombok: Builder 패턴을 사용하여 객체 생성을 돕는 builder 메소드를 생성합니다.
public class StdInfoEntitiy {

    @Id // 이 필드가 테이블의 Primary Key임을 명시합니다.
    @Column(name = "STD_NO", length = 20, nullable = false) // 컬럼 이름과 속성을 정의합니다.
    private String stdNo; // 테이블 컬럼명 STD_NO와 동일하게 매핑합니다.

    @Column(name = "STD_NM", length = 100)
    private String stdNm; // 테이블 컬럼명 STD_NM과 동일하게 매핑합니다.

    @Column(name = "SCSBJT_CD", length = 20, nullable = false)
    private String scsbjtCd; // 테이블 컬럼명 SCSBJT_CD와 동일하게 매핑합니다.

    @Column(name = "SCH_YR")
    private Integer schYr; // 테이블 컬럼명 SCH_YR과 동일하게 매핑합니다.

    @Column(name = "ENTR_DT")
    private LocalDateTime entrDt; // 테이블 컬럼명 ENTR_DT와 동일하게 매핑합니다. (TIMESTAMP는 LocalDateTime으로 매핑 권장)

    @Column(name = "STD_STAT_CD", length = 10)
    private String stdStatCd; // 테이블 컬럼명 STD_STAT_CD와 동일하게 매핑합니다.

    @Column(name = "STD_ZIP", length = 5)
    private String stdZip; // 테이블 컬럼명 STD_ZIP과 동일하게 매핑합니다.

    @Column(name = "STD_ADDR", length = 200)
    private String stdAddr; // 테이블 컬럼명 STD_ADDR과 동일하게 매핑합니다.

    @Column(name = "STD_DADDR", length = 200)
    private String stdDaddr; // 테이블 컬럼명 STD_DADDR과 동일하게 매핑합니다.

    @Column(name = "STD_TELNO", length = 11)
    private String stdTelno; // 테이블 컬럼명 STD_TELNO와 동일하게 매핑합니다.

    @Column(name = "STD_EML_ADDR", length = 320)
    private String stdEmlAddr; // 테이블 컬럼명 STD_EML_ADDR과 동일하게 매핑합니다.

    @Column(name = "USER_ID2", length = 20, nullable = false)
    private String userId2; // 테이블 컬럼명 USER_ID2와 동일하게 매핑합니다.

    @Column(name = "USE_YN", length = 1)
    private String useYn; // 테이블 컬럼명 USE_YN와 동일하게 매핑합니다.
}