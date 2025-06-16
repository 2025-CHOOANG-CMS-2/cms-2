package kr.ac.dhuniv.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

// @Getter, @Setter: 모든 필드에 대한 getter, setter 자동 생성
// @NoArgsConstructor: 기본 생성자 자동 생성
// @AllArgsConstructor: 모든 필드를 인자로 받는 생성자 자동 생성
// @Builder: 빌더 패턴을 사용하여 객체 생성 가능 (선택 사항)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmplInfoDTO {

    // 직원 번호: 생성 시에는 자동 부여될 수 있으므로 @NotBlank는 제거 (수정 시 필요)
    // 업데이트 요청의 경우 @NotBlank 또는 @NotNull 추가
    @Size(max = 20, message = "직원 번호는 최대 20자입니다.")
    private String emplNo;

    @Size(max = 100, message = "직원 이름은 최대 100자입니다.")
    @NotBlank(message = "직원 이름은 필수 입력입니다.") // 직원 이름은 필수라고 가정
    private String emplNm;

    @NotBlank(message = "부서/설명 번호는 필수 입력입니다.")
    @Size(max = 20, message = "부서/설명 번호는 최대 20자입니다.")
    private String descNo;

    @Size(max = 10, message = "직원 상태 코드는 최대 10자입니다.")
    private String emplStatCd; // 재직, 휴직 등 (선택 사항)

    @Size(max = 5, message = "우편번호는 최대 5자입니다.")
    private String emplZip;

    @Size(max = 200, message = "주소는 최대 200자입니다.")
    private String emplAddr;

    @Size(max = 200, message = "상세 주소는 최대 200자입니다.")
    private String emplDaddr;

    @Size(max = 11, message = "전화번호는 최대 11자입니다.")
    private String emplTelno;

    @Size(max = 320, message = "이메일 주소는 최대 320자입니다.")
    private String emplEmlAddr;

    // 사용자 ID2 (참조되는 외부 ID)
    @NotBlank(message = "사용자 ID는 필수 입력입니다.")
    @Size(max = 20, message = "사용자 ID는 최대 20자입니다.")
    private String userId2;

    @Size(max = 1, message = "사용 여부 (Y/N)는 1자입니다.")
    private String useYn; // 사용 여부 (Y/N)
}
