package kr.ac.dhuniv.std_info.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto; // 필드명 변경된 StdInfoDto
import kr.ac.dhuniv.std_info.repository.StdInfoRepository;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StdInfoRepository stdInfoRepository;
    private final UserRepository userRepository;

    @Transactional
    public StdInfoDto insertStudent(StdInfoDto dto) { // 입력 DTO는 변경된 필드명 가짐
        try {
            String newStdNo = generateStudentNo();

            // USER_ID2 값으로 사용자 조회
            // dto.getUSER_ID2()로 변경 (DTO 필드명에 맞춤)
            User user = userRepository.findById(dto.getUSER_ID2())
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자 없음: " + dto.getUSER_ID2()));

            // DTO → Entity 변환
            StdInfo entity = StdInfo.builder()
                    .stdNo(newStdNo)
                    .stdNm(dto.getSTD_NM()) // 변경된 DTO 필드명 사용
                    .scsbjtCd(dto.getSCSBJT_CD()) // 변경된 DTO 필드명 사용
                    .schoolYear(dto.getSCH_YR()) // 변경된 DTO 필드명 사용
                    .entranceDate(dto.getENTR_DT()) // 변경된 DTO 필드명 사용
                    .statusCode(dto.getSTD_STAT_CD()) // 변경된 DTO 필드명 사용
                    .zip(dto.getSTD_ZIP()) // 변경된 DTO 필드명 사용
                    .address(dto.getSTD_ADDR()) // 변경된 DTO 필드명 사용
                    .detailAddress(dto.getSTD_DADDR()) // 변경된 DTO 필드명 사용
                    .tel(dto.getSTD_TELNO()) // 변경된 DTO 필드명 사용
                    .email(dto.getSTD_EML_ADDR()) // 변경된 DTO 필드명 사용
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y") // USE_YN 기본값 설정
                    .user(user)
                    .build();

            StdInfo savedEntity = stdInfoRepository.save(entity);

            // 저장된 Entity의 정보를 DTO에 다시 담아서 반환
            StdInfoDto responseDto = new StdInfoDto();
            responseDto.setSTD_NO(savedEntity.getStdNo()); // DB에서 생성/할당된 학번을 DTO에 설정
            responseDto.setSTD_NM(savedEntity.getStdNm());
            responseDto.setSCSBJT_CD(savedEntity.getScsbjtCd());
            responseDto.setSCH_YR(savedEntity.getSchoolYear());
            responseDto.setENTR_DT(savedEntity.getEntranceDate());
            responseDto.setSTD_STAT_CD(savedEntity.getStatusCode());
            responseDto.setSTD_ZIP(savedEntity.getZip());
            responseDto.setSTD_ADDR(savedEntity.getAddress());
            responseDto.setSTD_DADDR(savedEntity.getDetailAddress());
            responseDto.setSTD_TELNO(savedEntity.getTel());
            responseDto.setSTD_EML_ADDR(savedEntity.getEmail());
            responseDto.setUSE_YN(savedEntity.getUseYn());
            // responseDto.setUSER_ID2(savedEntity.getUser().getUserId()); // User 객체에서 ID 가져와 설정 (필요시)

            return responseDto;
        } catch (IllegalArgumentException e) {
            System.err.println("학생 등록 실패 (데이터 유효성): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("학생 등록 실패 (서비스 내부 오류): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String generateStudentNo() {
        String prefix = "STD";
        Integer maxNo = stdInfoRepository.findMaxStdNoNumber(prefix);
        int nextNo = (maxNo != null) ? maxNo + 1 : 1;
        return String.format(prefix + "%03d", nextNo);
    }
}