package kr.ac.dhuniv.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import kr.ac.dhuniv.admin.repository.AdminStdRepository; // AdminStdRepository 임포트 유지
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import kr.ac.dhuniv.user.User; // User 관련 import 유지
import kr.ac.dhuniv.user.repository.UserRepository; // UserRepository import 유지 (User 객체 조회를 위해 필요)

@Service
@RequiredArgsConstructor
public class AdminStdService {

    private final AdminStdRepository stdInfoRepository;
    private final UserRepository userRepository; // User 엔티티를 조회하기 위해 필요

    @Transactional
    public StdInfoDto insertStudent(StdInfoDto dto) {
        try {
            // 1. 이메일 중복 체크
            if (stdInfoRepository.findByEmail(dto.getSTD_EML_ADDR()).isPresent()) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 등록된 학생의 이메일입니다.");
            }

            String newStdNo = generateStudentNo();

            String userReferenceId = dto.getUSER_ID2(); // 프론트에서 받은 "user01" 같은 문자열

            // User 엔티티의 userId (DB 컬럼명 user_id) 필드로 User를 찾습니다.
            User associatedUser = userRepository.findByUserId(userReferenceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 관리자 사용자 (ID: " + userReferenceId + ")를 찾을 수 없습니다. User 테이블의 user_id 필드를 확인하세요."));

            StdInfo entity = StdInfo.builder()
                    .stdNo(newStdNo)
                    .stdNm(dto.getSTD_NM())
                    .scsbjtCd(dto.getSCSBJT_CD())
                    .schoolYear(dto.getSCH_YR())
                    .entranceDate(dto.getENTR_DT())
                    .statusCode(dto.getSTD_STAT_CD())
                    .zip(dto.getSTD_ZIP())
                    .address(dto.getSTD_ADDR())
                    .detailAddress(dto.getSTD_DADDR())
                    .tel(dto.getSTD_TELNO())
                    .email(dto.getSTD_EML_ADDR())
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y")
                    .user(associatedUser) // <<<<<< 조회된 User 엔티티 객체를 설정 >>>>>>
                    .build();

            StdInfo savedEntity = stdInfoRepository.save(entity);

            // 응답 DTO 구성
            StdInfoDto responseDto = new StdInfoDto();
            responseDto.setSTD_NO(savedEntity.getStdNo());
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
            
            if (savedEntity.getUser() != null) {
                responseDto.setUSER_ID2(savedEntity.getUser().getUserId());
            } else {
                responseDto.setUSER_ID2(dto.getUSER_ID2());
            }

            return responseDto;
        } catch (IllegalArgumentException e) {
            System.err.println("학생 등록 실패 (데이터 유효성/사용자 오류): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("학생 등록 실패 (서비스 내부 오류): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("학생 등록 처리 중 예상치 못한 오류 발생", e);
        }
    }

    public String generateStudentNo() {
        String prefix = "STD";
        Integer maxNo = stdInfoRepository.findMaxStdNoNumber(prefix);
        int nextNo = (maxNo != null) ? maxNo + 1 : 1;
        return String.format(prefix + "%03d", nextNo);
    }
    
    @Transactional(readOnly = true)
    public Page<StdInfoDto> getAllStudents(Pageable pageable) {
        Page<StdInfo> studentPage = stdInfoRepository.findAll(pageable);

        return studentPage.map(stdInfo -> {
            StdInfoDto dto = new StdInfoDto();
            
            dto.setSTD_NO(stdInfo.getStdNo());
            dto.setSTD_NM(stdInfo.getStdNm());
            dto.setSCSBJT_CD(stdInfo.getScsbjtCd());
            dto.setSCH_YR(stdInfo.getSchoolYear());
            dto.setENTR_DT(stdInfo.getEntranceDate());
            dto.setSTD_STAT_CD(stdInfo.getStatusCode());
            dto.setSTD_ZIP(stdInfo.getZip());
            dto.setSTD_ADDR(stdInfo.getAddress());
            dto.setSTD_DADDR(stdInfo.getDetailAddress());
            dto.setSTD_TELNO(stdInfo.getTel());
            dto.setSTD_EML_ADDR(stdInfo.getEmail());
            dto.setUSE_YN(stdInfo.getUseYn());

            if (stdInfo.getUser() != null) {
                dto.setUSER_ID2(stdInfo.getUser().getUserId());
            } else {
                dto.setUSER_ID2(null);
            }
            return dto;
        });
    }



    // ✨ 1. 학생 정보 수정 메서드 추가
    @Transactional // 데이터 변경이 발생하므로 @Transactional 필요
    public StdInfoDto updateStudent(String stdNo, StdInfoDto dto) {
        // 학번(stdNo)으로 기존 학생 정보 조회
        StdInfo existingStudent = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        // DTO의 정보로 기존 엔티티 필드 업데이트
        // (null 체크는 필요에 따라 추가)
        existingStudent.setStdNm(dto.getSTD_NM());
        existingStudent.setScsbjtCd(dto.getSCSBJT_CD());
        existingStudent.setSchoolYear(dto.getSCH_YR());
        existingStudent.setEntranceDate(dto.getENTR_DT());
        existingStudent.setStatusCode(dto.getSTD_STAT_CD());
        existingStudent.setZip(dto.getSTD_ZIP());
        existingStudent.setAddress(dto.getSTD_ADDR());
        existingStudent.setDetailAddress(dto.getSTD_DADDR());
        existingStudent.setTel(dto.getSTD_TELNO());
        existingStudent.setEmail(dto.getSTD_EML_ADDR());
        existingStudent.setUseYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y"); // USE_YN이 DTO에 없으면 기본값 설정

        // USER_ID2가 변경될 경우 User 엔티티 업데이트
        if (dto.getUSER_ID2() != null && !dto.getUSER_ID2().equals(existingStudent.getUser() != null ? existingStudent.getUser().getUserId() : null)) {
            User newUser = userRepository.findByUserId(dto.getUSER_ID2())
                    .orElseThrow(() -> new IllegalArgumentException("새로운 관리자 사용자 (ID: " + dto.getUSER_ID2() + ")를 찾을 수 없습니다."));
            existingStudent.setUser(newUser);
        } else if (dto.getUSER_ID2() == null && existingStudent.getUser() != null) {
            // DTO에서 USER_ID2가 null인데 기존 학생에게 User가 연결되어 있었다면 연결 해제
            existingStudent.setUser(null);
        }

        // 변경된 엔티티 저장 (JPA 영속성 컨텍스트 덕분에 명시적 save는 필수는 아니지만, 명확성을 위해 호출)
        StdInfo updatedEntity = stdInfoRepository.save(existingStudent);

        // 업데이트된 엔티티를 DTO로 변환하여 반환
        StdInfoDto responseDto = new StdInfoDto();
        responseDto.setSTD_NO(updatedEntity.getStdNo());
        responseDto.setSTD_NM(updatedEntity.getStdNm());
        responseDto.setSCSBJT_CD(updatedEntity.getScsbjtCd());
        responseDto.setSCH_YR(updatedEntity.getSchoolYear());
        responseDto.setENTR_DT(updatedEntity.getEntranceDate());
        responseDto.setSTD_STAT_CD(updatedEntity.getStatusCode());
        responseDto.setSTD_ZIP(updatedEntity.getZip());
        responseDto.setSTD_ADDR(updatedEntity.getAddress());
        responseDto.setSTD_DADDR(updatedEntity.getDetailAddress());
        responseDto.setSTD_TELNO(updatedEntity.getTel());
        responseDto.setSTD_EML_ADDR(updatedEntity.getEmail());
        responseDto.setUSE_YN(updatedEntity.getUseYn());
        if (updatedEntity.getUser() != null) {
            responseDto.setUSER_ID2(updatedEntity.getUser().getUserId());
        }

        return responseDto;
    }



    // ✨ 2. 학생 삭제 메서드 추가
    @Transactional // 데이터 변경이 발생하므로 @Transactional 필요
    public void deleteStudent(String stdNo) {
        // 학번(stdNo)으로 학생 조회
        StdInfo studentToDelete = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        // 학생 삭제
        stdInfoRepository.delete(studentToDelete);
    }
}