package kr.ac.dhuniv.admin.service;

import kr.ac.dhuniv.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import kr.ac.dhuniv.admin.repository.AdminStdRepository;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import kr.ac.dhuniv.user.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminStdService {

    private final AdminStdRepository stdInfoRepository;
    private final UserRepository userRepository;

    // 학과 코드와 이름 매핑 (백엔드에서 유효성 검증 및 학번 생성에 사용)
    private static final Map<String, String> DEPT_MAP = new HashMap<>();
    static {
        DEPT_MAP.put("001", "국어국문학과");
        DEPT_MAP.put("002", "영어영문학과");
        DEPT_MAP.put("003", "철학과");
        DEPT_MAP.put("004", "정치외교학과");
        DEPT_MAP.put("005", "심리학과");
        DEPT_MAP.put("006", "사회복지학과");
        DEPT_MAP.put("007", "통계학과");
        DEPT_MAP.put("008", "천문학과");
        DEPT_MAP.put("009", "화학과");
        DEPT_MAP.put("010", "기계공학과");
        DEPT_MAP.put("011", "컴퓨터공학과");
        DEPT_MAP.put("012", "건축학과");
        DEPT_MAP.put("013", "스마트시스템과학과");
        DEPT_MAP.put("014", "동양화과");
        DEPT_MAP.put("015", "조소과");
        DEPT_MAP.put("016", "공예과");
        DEPT_MAP.put("017", "교육학과");
        DEPT_MAP.put("018", "식품영양학과");
        DEPT_MAP.put("019", "의류학과");
        DEPT_MAP.put("020", "성악과");
        DEPT_MAP.put("021", "의예과");
    }

    // 상태 코드와 이름 매핑
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("ENROLL", "재학");
        STATUS_MAP.put("LEAVE", "휴학");
        STATUS_MAP.put("GRAD", "졸업");
    }

    @Transactional
    public StdInfoDto insertStudent(StdInfoDto dto) {
        try {
            // 1. 이메일 중복 체크
            if (stdInfoRepository.findByEmail(dto.getSTD_EML_ADDR()).isPresent()) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 등록된 학생의 이메일입니다.");
            }

            // 2. 전화번호 중복 체크 추가
            if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
                if (stdInfoRepository.findByTel(dto.getSTD_TELNO()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 등록된 학생의 전화번호입니다.");
                }
            }

            // 3. 학과 코드 유효성 검사
            if (!DEPT_MAP.containsKey(dto.getSCSBJT_CD())) {
                throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + dto.getSCSBJT_CD());
            }

            // 4. 관리자 ID (USER_ID2)로 User 엔티티 조회
            User associatedUser = userRepository.findByUserId(dto.getUSER_ID2())
                    .orElseThrow(() -> new IllegalArgumentException("해당 관리자 사용자 (ID: " + dto.getUSER_ID2() + ")를 찾을 수 없습니다. User 테이블의 user_id 필드를 확인하세요."));

            // 5. 새로운 학번 생성 (YYYY + NNN + MMM 체계)
            String newStdNo = generateStudentNo(dto.getENTR_DT(), dto.getSCSBJT_CD());
            
            // 6. 생성된 학번의 중복 체크
            if (stdInfoRepository.findByStdNo(newStdNo).isPresent()) {
                throw new IllegalStateException("생성된 학번(" + newStdNo + ")이 이미 존재합니다. 다시 시도해주세요.");
            }

            // StdInfo 엔티티 빌드 및 저장
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
                    .user(associatedUser) // `user` 필드에 관리자 User 엔티티 설정
                    .build();

            StdInfo savedEntity = stdInfoRepository.save(entity);

            return convertToDto(savedEntity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("학생 등록 실패: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("학생 등록 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("학생 등록 처리 중 예상치 못한 오류 발생", e);
        }
    }

    public String generateStudentNo(LocalDate entranceDate, String scsbjtCd) {
        if (entranceDate == null) {
            throw new IllegalArgumentException("입학일자가 없어 학번을 생성할 수 없습니다.");
        }
        if (scsbjtCd == null || scsbjtCd.isEmpty()) {
            throw new IllegalArgumentException("학과 코드가 없어 학번을 생성할 수 없습니다.");
        }

        String year = String.valueOf(entranceDate.getYear());

        if (!DEPT_MAP.containsKey(scsbjtCd)) {
             throw new IllegalArgumentException("학번 생성에 필요한 유효하지 않은 학과 코드입니다: " + scsbjtCd);
        }

        Integer maxSequence = stdInfoRepository.findMaxSequenceForStudentId(year, scsbjtCd);
        int nextSequence = (maxSequence != null) ? maxSequence + 1 : 1;

        String sequencePart = String.format("%03d", nextSequence);

        return year + scsbjtCd + sequencePart;
    }
    
    @Transactional(readOnly = true)
    public Page<StdInfoDto> getAllStudents(Pageable pageable, String searchName, String searchDept, String searchStatus) {
        Specification<StdInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchName != null && !searchName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("stdNm")), "%" + searchName.toLowerCase() + "%"));
            }
            if (searchDept != null && !searchDept.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("scsbjtCd"), searchDept));
            }
            if (searchStatus != null && !searchStatus.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("statusCode"), searchStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StdInfo> studentPage = stdInfoRepository.findAll(spec, pageable);
        return studentPage.map(this::convertToDto);
    }

    @Transactional
    public StdInfoDto updateStudent(String stdNo, StdInfoDto dto) {
        StdInfo existingStudent = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        if (!DEPT_MAP.containsKey(dto.getSCSBJT_CD())) {
            throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + dto.getSCSBJT_CD());
        }

        User updatedByUser = null;
        if (dto.getUSER_ID2() != null && !dto.getUSER_ID2().isEmpty()) {
            updatedByUser = userRepository.findByUserId(dto.getUSER_ID2())
                    .orElseThrow(() -> new IllegalArgumentException("학생 정보를 수정하는 관리자 ID(" + dto.getUSER_ID2() + ")를 찾을 수 없습니다."));
        }

        // 전화번호 중복 체크 (수정 시): 자기 자신의 전화번호는 허용
        if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
            Optional<StdInfo> existingStudentWithTel = stdInfoRepository.findByTel(dto.getSTD_TELNO());
            if (existingStudentWithTel.isPresent() && !existingStudentWithTel.get().getStdNo().equals(stdNo)) {
                throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 다른 학생에게 등록된 전화번호입니다.");
            }
        }

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
        existingStudent.setUseYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y");
        
        existingStudent.setUser(updatedByUser); // `user` 필드에 관리자 User 엔티티 설정

        StdInfo updatedEntity = stdInfoRepository.save(existingStudent);

        return convertToDto(updatedEntity);
    }

    @Transactional
    public void deleteStudent(String stdNo) {
        StdInfo studentToDelete = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        stdInfoRepository.delete(studentToDelete);
    }

    private StdInfoDto convertToDto(StdInfo stdInfo) {
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
    }
}