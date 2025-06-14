package kr.ac.dhuniv.admin;

//필요한 Jakarta Persistence API import 문
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//Lombok import 문
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // AllArgsConstructor는 필요에 따라 추가

@Entity // 이 클래스가 JPA 엔티티임을 명시합니다.
@Table(name = "EMPL_INFO") // 매핑될 데이터베이스 테이블 이름을 지정합니다.
@Getter // Lombok: 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: protected 접근 레벨의 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // Lombok: 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다. (필요에 따라 사용)
@Builder // Lombok: Builder 패턴을 사용하여 객체 생성을 돕는 builder 메소드를 생성합니다.
public class EmplInfoEntity {

 @Id 
 @Column(name = "EMPL_NO", length = 20, nullable = false) 
 private String emplNo; 

 @Column(name = "EMPL_NM", length = 100)
 private String emplNm; 

 @Column(name = "DESC_NO", length = 20, nullable = false)
 private String descNo; 

 @Column(name = "EMPL_STAT_CD", length = 10)
 private String emplStatCd; 

 @Column(name = "EMPL_ZIP", length = 5)
 private String emplZip; 

 @Column(name = "EMPL_ADDR", length = 200)
 private String emplAddr; 

 @Column(name = "EMPL_DADDR", length = 200)
 private String emplDaddr; 

 @Column(name = "EMPL_TELNO", length = 11)
 private String emplTelno; 
 
 @Column(name = "EMPL_EML_ADDR", length = 320)
 private String emplEmlAddr; 

 @Column(name = "USER_ID2", length = 20, nullable = false)
 private String userId2; 

 @Column(name = "USE_YN", length = 1)
 private String useYn; 
}