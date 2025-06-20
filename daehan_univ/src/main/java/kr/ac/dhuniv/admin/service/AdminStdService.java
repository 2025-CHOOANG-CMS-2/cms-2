package kr.ac.dhuniv.admin.service;

import kr.ac.dhuniv.admin.repository.AdminStdRepository;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.Role;
import kr.ac.dhuniv.user.repository.UserRepository;
import kr.ac.dhuniv.user.repository.RoleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStdService {

    private final AdminStdRepository stdInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ⭐ 학과 코드와 이름 매핑 (3자리로 유지, 사용자님이 처음 주신 그대로) ⭐
    public static final Map<String, String> SCSBJT_MAP = new HashMap<>();
    static {
        SCSBJT_MAP.put("001", "국어국문학과");
        SCSBJT_MAP.put("002", "영어영문학과");
        SCSBJT_MAP.put("003", "철학과");
        SCSBJT_MAP.put("004", "정치외교학과");
        SCSBJT_MAP.put("005", "심리학과");
        SCSBJT_MAP.put("006", "사회복지학과");
        SCSBJT_MAP.put("007", "통계학과");
        SCSBJT_MAP.put("008", "천문학과");
        SCSBJT_MAP.put("009", "화학과");
        SCSBJT_MAP.put("010", "기계공학과");
        SCSBJT_MAP.put("011", "컴퓨터공학과");
        SCSBJT_MAP.put("012", "건축학과");
        SCSBJT_MAP.put("013", "스마트시스템과학과");
        SCSBJT_MAP.put("014", "동양화과");
        SCSBJT_MAP.put("015", "조소과");
        SCSBJT_MAP.put("016", "공예과");
        SCSBJT_MAP.put("017", "교육학과");
        SCSBJT_MAP.put("018", "식품영양학과");
        SCSBJT_MAP.put("019", "의류학과");
        SCSBJT_MAP.put("020", "성악과");
        SCSBJT_MAP.put("021", "의예과");
    }

    // 학생 상태 코드와 라벨 매핑 (변동 없음)
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("ENROLL", "재학");
        STATUS_MAP.put("LEAVE", "휴학");
        STATUS_MAP.put("GRAD", "졸업");
        STATUS_MAP.put("EXPEL", "제적");
        STATUS_MAP.put("GRAD_WAIT", "졸업유예");
        STATUS_MAP.put("ABSENT_LEAVE", "자퇴");
    }

    /**
     * 새로운 학생을 등록합니다.
     * 학번 자동 생성, 이메일/전화번호 중복 체크, 유효성 검사,
     * 그리고 user_info 테이블에 학생 계정 생성 및 비밀번호 암호화, 권한 부여를 수행합니다.
     * 비밀번호는 학번을 암호화하여 사용합니다.
     *
     * @param dto 등록할 학생 정보 DTO
     * @return 등록된 학생 정보 DTO
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws IllegalStateException 권한 정보 조회 실패 시 등
     */
    @Transactional
    public StdInfoDto insertStudent(StdInfoDto dto) {
        log.info("[Service - insertStudent] DTO 수신: {}", dto);
        log.info("[Service - insertStudent] CREATED_BY 값: '{}'", dto.getCREATED_BY());

        try {
            // 1. 새로운 학번 자동 생성 (YYYY + 학과코드(3자리) + SSS 형식)
            String currentYear = String.valueOf(LocalDate.now().getYear());
            String scsbjtCode = dto.getSCSBJT_CD(); // 예: "003", "011" 등 3자리 코드

            if (!SCSBJT_MAP.containsKey(scsbjtCode)) {
                throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + scsbjtCode);
            }

            String newStdNo = generateStudentNo(currentYear, scsbjtCode); // ⭐ 3자리 학과 코드 그대로 전달 ⭐
            dto.setSTD_NO(newStdNo); // DTO에 생성된 학번 설정 (User.userId로 사용될 값)

            // 2. 이메일 중복 체크 (std_info 테이블 기준)
            if (dto.getSTD_EML_ADDR() != null && !dto.getSTD_EML_ADDR().trim().isEmpty()) {
                if (stdInfoRepository.findByStdEmlAddr(dto.getSTD_EML_ADDR()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 등록된 학생의 이메일입니다.");
                }
            }

            // 3. 전화번호 중복 체크 (std_info 테이블 기준)
            if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
                if (stdInfoRepository.findByStdTelno(dto.getSTD_TELNO()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 등록된 학생의 전화번호입니다.");
                }
            }

            // 4. 학생 상태 코드 유효성 검사
            if (!STATUS_MAP.containsKey(dto.getSTD_STAT_CD())) {
                throw new IllegalArgumentException("유효하지 않은 학생 상태 코드입니다: " + dto.getSTD_STAT_CD());
            }

            // 5. **USER_INFO 테이블에 학생 본인의 계정 생성 및 권한 부여 (가장 먼저 수행)**
            String userIdForAccount = newStdNo; // 학생 계정의 ID는 생성된 학번으로 설정
            String defaultPassword = newStdNo; // 초기 비밀번호는 학번과 동일
            String encodedPassword = passwordEncoder.encode(defaultPassword); // 비밀번호 암호화

            if (userRepository.findByUserId(userIdForAccount).isPresent()) {
                throw new IllegalStateException("생성된 학번(" + userIdForAccount + ")에 해당하는 사용자 계정이 이미 존재합니다. 데이터 불일치 가능성. (이전 학번이 잘못 생성된 경우)");
            }

            Role studentRole = roleRepository.findByRoleName("STUDENT")
                    .orElseThrow(() -> new IllegalStateException("'STUDENT' 권한을 찾을 수 없습니다. role_info 테이블에 'STUDENT' 권한이 존재하는지 확인하세요."));
            
            User newUserAccount = User.builder()
                    .userId(userIdForAccount)
                    .userPw(encodedPassword)
                    .roles(Collections.singletonList(studentRole))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .failedLoginCnt(0)
                    .userYn("Y")
                    .build();
            userRepository.save(newUserAccount);

            // 6. StdInfo 엔티티에 연결할 관리자 ID 유효성 검사 (User 테이블에서 존재 여부만 확인)
            if (dto.getCREATED_BY() == null || dto.getCREATED_BY().trim().isEmpty()) {
                 log.error("[Service - insertStudent] CREATED_BY 값이 null이거나 비어있어 유효성 검사 실패.");
                 throw new IllegalArgumentException("등록 관리자 ID (CREATED_BY)는 필수 값입니다.");
            }
            userRepository.findByUserId(dto.getCREATED_BY())
                .orElseThrow(() -> new IllegalStateException("등록 관리자 계정 (ID: " + dto.getCREATED_BY() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

            // 7. **StdInfo 엔티티 빌드 및 저장**
            StdInfo entity = StdInfo.builder()
                    .user(newUserAccount) // 새로 생성된 User 객체를 StdInfo에 연결 (이것이 std_no 컬럼에 저장됨)
                    .stdNm(dto.getSTD_NM())
                    .scsbjtCd(dto.getSCSBJT_CD()) // 엔티티에 3자리 코드 그대로 저장
                    .schoolYear(dto.getSCH_YR())
                    .entranceDate(dto.getENTR_DT())
                    .statusCode(dto.getSTD_STAT_CD())
                    .stdZip(dto.getSTD_ZIP())
                    .stdAddr(dto.getSTD_ADDR())
                    .stdDaddr(dto.getSTD_DADDR())
                    .stdTelno(dto.getSTD_TELNO())
                    .stdEmlAddr(dto.getSTD_EML_ADDR())
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y")
                    .profileImageUrl(dto.getPROFILE_IMAGE_URL())
                    .createdBy(dto.getCREATED_BY())
                    .build();

            StdInfo savedEntity = stdInfoRepository.save(entity);

            return convertToDto(savedEntity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("학생 등록 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("학생 등록 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("학생 등록 처리 중 예상치 못한 오류 발생", e);
        }
    }

    /**
     * 학생 목록을 페이징하여 조회하고, 검색 조건에 따라 필터링합니다.
     * @param pageable 페이징 및 정렬 정보 (user.userId로 정렬)
     * @param searchName 검색할 학생 이름
     * @param searchDept 검색할 학과 코드
     * @param searchStatus 검색할 상태 코드
     */
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

    /**
     * 특정 학번의 학생 상세 정보를 조회합니다.
     * @param stdNo 조회할 학생의 학번 (이는 User.userId와 동일)
     * @return 조회된 학생 정보 DTO
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public StdInfoDto getStudentByStdNo(String stdNo) {
        StdInfo stdInfo = stdInfoRepository.findByUser_UserId(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생 정보를 찾을 수 없습니다."));
        return convertToDto(stdInfo);
    }


    /**
     * 기존 학생 정보를 수정합니다.
     * 학번을 기준으로 학생을 찾아 수정하며, 이메일, 전화번호 중복 체크를 수행합니다.
     * user_info 테이블의 계정 비밀번호는 학번으로 재설정됩니다.
     *
     * @param stdNo 수정할 학생의 학번
     * @param dto 수정할 학생 정보 DTO
     * @return 수정된 학생 정보 DTO
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없거나 유효성 검사 실패 시
     */
    @Transactional
    public StdInfoDto updateStudent(String stdNo, StdInfoDto dto) {
        log.info("[Service - updateStudent] DTO 수신 (학번: {}): {}", stdNo, dto);
        log.info("[Service - updateStudent] CREATED_BY 값: '{}'", dto.getCREATED_BY());

        // 1. 해당 학번의 학생 존재 여부 확인 (std_info 테이블에서 User.userId를 통해 조회)
        StdInfo existingStudent = stdInfoRepository.findByUser_UserId(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        // 2. user_info 테이블의 계정 비밀번호 재설정 및 업데이트
        User existingUserAccount = userRepository.findByUserId(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생 학번(" + stdNo + ")에 연결된 사용자 계정을 찾을 수 없습니다."));

        String newDefaultPassword = stdNo;
        String newEncodedPassword = passwordEncoder.encode(newDefaultPassword);
        existingUserAccount.setUserPw(newEncodedPassword);
        // existingUserAccount.setUpdatedAt(LocalDateTime.now());
        existingUserAccount.setUserYn("Y");
        
        userRepository.save(existingUserAccount);

        // 3. 이메일 중복 체크 (수정 시: 자기 자신의 이메일은 중복으로 간주하지 않음)
        if (dto.getSTD_EML_ADDR() != null && !dto.getSTD_EML_ADDR().trim().isEmpty()) {
            Optional<StdInfo> existingStudentWithEmail = stdInfoRepository.findByStdEmlAddr(dto.getSTD_EML_ADDR());
            if (existingStudentWithEmail.isPresent() && !existingStudentWithEmail.get().getUser().getUserId().equals(stdNo)) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 다른 학생에게 등록된 이메일입니다.");
            }
        }

        // 4. 전화번호 중복 체크 (수정 시: 자기 자신의 전화번호는 중복으로 간주하지 않음)
        if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
            Optional<StdInfo> existingStudentWithTel = stdInfoRepository.findByStdTelno(dto.getSTD_TELNO());
            if (existingStudentWithTel.isPresent() && !existingStudentWithTel.get().getUser().getUserId().equals(stdNo)) {
                throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 다른 학생에게 등록된 전화번호입니다.");
            }
        }

        // 5. 학과 코드 유효성 검사
        if (!SCSBJT_MAP.containsKey(dto.getSCSBJT_CD())) {
            throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + dto.getSCSBJT_CD());
        }

        // 6. 학생 상태 코드 유효성 검사
        if (!STATUS_MAP.containsKey(dto.getSTD_STAT_CD())) {
            throw new IllegalArgumentException("유효하지 않은 학생 상태 코드입니다: " + dto.getSTD_STAT_CD());
        }

        // 7. StdInfo 엔티티를 수정한 관리자 ID 유효성 검사
        if (dto.getCREATED_BY() == null || dto.getCREATED_BY().trim().isEmpty()) {
             log.error("[Service - updateStudent] CREATED_BY 값이 null이거나 비어있어 유효성 검사 실패.");
             throw new IllegalArgumentException("수정 관리자 ID (CREATED_BY)는 필수 값입니다.");
        }
        userRepository.findByUserId(dto.getCREATED_BY())
            .orElseThrow(() -> new IllegalStateException("수정 관리자 계정 (ID: " + dto.getCREATED_BY() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

        // 8. StdInfo 엔티티 필드 업데이트
        existingStudent.setStdNm(dto.getSTD_NM());
        existingStudent.setScsbjtCd(dto.getSCSBJT_CD());
        existingStudent.setSchoolYear(dto.getSCH_YR());
        existingStudent.setEntranceDate(dto.getENTR_DT());
        existingStudent.setStatusCode(dto.getSTD_STAT_CD());
        existingStudent.setStdZip(dto.getSTD_ZIP());
        existingStudent.setStdAddr(dto.getSTD_ADDR());
        existingStudent.setStdDaddr(dto.getSTD_DADDR());
        existingStudent.setStdTelno(dto.getSTD_TELNO());
        existingStudent.setStdEmlAddr(dto.getSTD_EML_ADDR());
        existingStudent.setUseYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y");
        existingStudent.setProfileImageUrl(dto.getPROFILE_IMAGE_URL());
        existingStudent.setCreatedBy(dto.getCREATED_BY());
        
        StdInfo updatedEntity = stdInfoRepository.save(existingStudent);

        return convertToDto(updatedEntity);
    }

    /**
     * 특정 학번의 학생을 삭제합니다.
     * std_info에서 삭제뿐만 아니라 user_info의 해당 계정(학번 기반)도 함께 삭제합니다.
     *
     * @param stdNo 삭제할 학생의 학번
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없을 때
     */
    @Transactional
    public void deleteStudent(String stdNo) {
        // 1. std_info에서 해당 학생 정보 조회 (User.userId를 통해)
        StdInfo studentToDelete = stdInfoRepository.findByUser_UserId(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생 정보를 찾을 수 없습니다."));

        // 2. std_info에서 삭제 (외래키 제약조건으로 인해 User 계정보다 먼저 삭제)
        stdInfoRepository.delete(studentToDelete);

        // 3. user_info에서 해당 계정 삭제 (std_info 삭제 후 진행)
        Optional<User> userAccountToDelete = userRepository.findByUserId(stdNo);
        userAccountToDelete.ifPresent(userRepository::delete);
    }

    /**
     * 새로운 학생 학번을 생성합니다. (YYYY + 학과코드(DDD) + SSS 형식)
     * 예: 2025003001 (2025년, 철학과(003), 순번 001)
     * DTO에서 받은 3자리 학과 코드를 그대로 사용하여 학번을 구성합니다.
     *
     * @param year 현재 년도 (String)
     * @param scsbjtCd 학과 코드 (String) - DTO에서 넘어온 3자리 학과 코드 (예: "003")
     * @return 생성된 새로운 학번 (YYYYDDDSSS 형식)
     */
    private String generateStudentNo(String year, String scsbjtCd) {
        if (year == null || year.isEmpty()) {
            throw new IllegalArgumentException("년도 정보가 없어 학번을 생성할 수 없습니다.");
        }
        if (scsbjtCd == null || scsbjtCd.isEmpty() || scsbjtCd.length() != 3) { // ⭐ 3자리 학과 코드 유효성 검사 추가 ⭐
            throw new IllegalArgumentException("학과 코드는 3자리여야 합니다: " + scsbjtCd);
        }

        // findMaxSequenceForStudentId 쿼리의 substring 인덱스를 5, 3으로 변경하여 학과 코드가 3자리임을 반영했습니다.
        Optional<Integer> maxSequenceOptional = stdInfoRepository.findMaxSequenceForStudentId(year, scsbjtCd);
        int nextSequence = maxSequenceOptional.orElse(0) + 1; // maxSequenceOptional이 비어있으면 0부터 시작 +1

        String sequencePart = String.format("%03d", nextSequence); // 3자리 순번 (예: 001, 002)

        return year + scsbjtCd + sequencePart; // ⭐ 3자리 학과 코드 그대로 사용 ⭐
    }


    /**
     * StdInfo 엔티티를 StdInfoDto DTO로 변환하는 헬퍼 메서드.
     * User 엔티티의 userPw는 보안상 DTO로 반환하지 않습니다.
     *
     * @param stdInfo 변환할 StdInfo 엔티티
     * @return 변환된 StdInfoDto DTO
     */
    private StdInfoDto convertToDto(StdInfo stdInfo) {
        StdInfoDto dto = new StdInfoDto();
        dto.setSTD_ID(stdInfo.getStdId());

        // ⭐ 중요: stdInfo.getUser()가 null일 수 있으므로 Null 체크 추가 ⭐
        if (stdInfo.getUser() != null) {
            dto.setSTD_NO(stdInfo.getUser().getUserId()); // User 엔티티의 userId를 통해 학번 가져오기
        } else {
            // 연결된 User 정보가 없을 경우 학번을 null 또는 빈 문자열로 설정
            dto.setSTD_NO(null); // 또는 "";
            log.warn("StdInfo (ID: {})에 연결된 User 정보가 없어 STD_NO를 설정할 수 없습니다.", stdInfo.getStdId());
        }
        
        dto.setSTD_NM(stdInfo.getStdNm());
        dto.setSCSBJT_CD(stdInfo.getScsbjtCd()); // 엔티티의 3자리 학과 코드를 DTO에 그대로 매핑
        dto.setSCH_YR(stdInfo.getSchoolYear());
        dto.setENTR_DT(stdInfo.getEntranceDate());
        dto.setSTD_STAT_CD(stdInfo.getStatusCode()); // 엔티티 필드명 'statusCode'를 DTO 'STD_STAT_CD'에 매핑
        dto.setSTD_ZIP(stdInfo.getStdZip());
        dto.setSTD_ADDR(stdInfo.getStdAddr());
        dto.setSTD_DADDR(stdInfo.getStdDaddr());
        dto.setSTD_TELNO(stdInfo.getStdTelno());
        dto.setSTD_EML_ADDR(stdInfo.getStdEmlAddr());
        dto.setUSE_YN(stdInfo.getUseYn());
        dto.setPROFILE_IMAGE_URL(stdInfo.getProfileImageUrl()); // 추가된 필드 매핑
        dto.setCREATED_BY(stdInfo.getCreatedBy());
        return dto;
    }
}
