package kr.ac.dhuniv.admin.service;

import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.Role;
import kr.ac.dhuniv.user.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import kr.ac.dhuniv.admin.repository.AdminStdRepository;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
import kr.ac.dhuniv.user.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminStdService {

    private final AdminStdRepository stdInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 학과 코드와 이름 매핑
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

    /**
     * 새로운 학생을 등록합니다.
     * 학번 자동 생성, 이메일/전화번호 중복 체크, 유효성 검사,
     * 그리고 user_info 테이블에 학생 계정 생성 및 비밀번호 암호화, 권한 부여를 수행합니다.
     * 비밀번호는 생성된 학번을 암호화하여 사용합니다.
     *
     * @param dto 등록할 학생 정보 DTO
     * @return 등록된 학생 정보 DTO
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws IllegalStateException 권한 정보 조회 실패 시 등
     */
    @Transactional // 단일 트랜잭션으로 User와 StdInfo 처리
    public StdInfoDto insertStudent(StdInfoDto dto) {
        try {
            // 1. 이메일 중복 체크 (std_info 테이블)
            if (dto.getSTD_EML_ADDR() != null && !dto.getSTD_EML_ADDR().trim().isEmpty()) {
                if (stdInfoRepository.findByStdEmlAddr(dto.getSTD_EML_ADDR()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 등록된 학생의 이메일입니다.");
                }
            }

            // 2. 전화번호 중복 체크 (std_info 테이블)
            if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
                if (stdInfoRepository.findByStdTelno(dto.getSTD_TELNO()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 다른 학생에게 등록된 전화번호입니다.");
                }
            }

            // 3. 학과 코드 유효성 검사
            if (!DEPT_MAP.containsKey(dto.getSCSBJT_CD())) {
                throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + dto.getSCSBJT_CD());
            }
            
            // 4. 새로운 학번 생성 (YYYYDDDSSS 체계)
            String newStdNo = generateStudentNo(dto.getENTR_DT(), dto.getSCSBJT_CD());
            dto.setSTD_NO(newStdNo); // DTO에 생성된 학번 설정 (클라이언트 반환용)

            // 5. 생성된 학번의 중복 체크 (std_info 테이블)
            if (stdInfoRepository.findByStdNo(newStdNo).isPresent()) {
                throw new IllegalStateException("생성된 학번(" + newStdNo + ")이 이미 존재합니다. 다시 시도해주세요.");
            }

            // 6. user_info 테이블에 학생 계정 생성 및 권한 부여 (동일 트랜잭션 내)
            String userIdForAccount = newStdNo; // user_id는 학번과 동일하게 사용
            String defaultPassword = newStdNo; 
            String encodedPassword = passwordEncoder.encode(defaultPassword);

            // user_info 테이블에서 userId 중복 체크 (혹시 모를 학번-ID 중복 방지)
            if (userRepository.findByUserId(userIdForAccount).isPresent()) {
                throw new IllegalStateException("생성될 사용자 ID(" + userIdForAccount + ")에 해당하는 계정이 이미 존재합니다. 재시도 필요.");
            }

            // 'STUDENT' 권한 정보 조회
            Role studentRole = roleRepository.findByRoleName("STUDENT")
                    .orElseThrow(() -> new IllegalStateException("'STUDENT' 권한을 찾을 수 없습니다. role_info 테이블에 'STUDENT' 권한이 존재하는지 확인하세요."));
            
            User newUserAccount = User.builder()
                    .userId(userIdForAccount)
                    .userPw(encodedPassword)
                    .roles(Collections.singletonList(studentRole))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .failedLoginCnt(0)
                    .build();
            
            newUserAccount = userRepository.save(newUserAccount); 
            userRepository.flush(); 

            System.out.println("DEBUG: User account created and persisted with userIdx: " + newUserAccount.getUserIdx());

            // 7. StdInfo 엔티티 빌드 및 저장
            StdInfo entity = StdInfo.builder()
                    .stdNo(dto.getSTD_NO())
                    .stdNm(dto.getSTD_NM())
                    .scsbjtCd(dto.getSCSBJT_CD())
                    .schoolYear(dto.getSCH_YR())
                    .entranceDate(dto.getENTR_DT())
                    .statusCode(dto.getSTD_STAT_CD())
                    .stdZip(dto.getSTD_ZIP())
                    .stdAddr(dto.getSTD_ADDR())
                    .stdDaddr(dto.getSTD_DADDR())
                    .stdTelno(dto.getSTD_TELNO())
                    .stdEmlAddr(dto.getSTD_EML_ADDR())
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y")
                    // ⭐⭐ 수정됨: userId2Value에 관리자 ID 직접 할당 (임시) ⭐⭐
                    // TODO: 나중에 Spring Security 세션에서 현재 로그인한 관리자의 user_id를 가져와 할당하도록 변경
                    .userId2Value("admin") // 하드코딩된 'admin' 사용 (사용자님이 언급하신 "admin" 데이터)
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

    /**
     * 학번을 생성합니다. (YYYYDDDSSS 체계)
     * 예: 2025001001 (2025년, 국어국문학과(001), 순번 001)
     *
     * @param entranceDate 입학일자 (년도 추출용)
     * @param scsbjtCd 학과 코드
     * @return 생성된 새로운 학번
     * @throws IllegalArgumentException 입학일자나 학과 코드가 유효하지 않을 경우
     */
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

        String sequencePart = String.format("%03d", nextSequence); // 순번을 3자리로 포맷팅 (예: 1 -> 001)

        return year + scsbjtCd + sequencePart;
    }
    
    /**
     * 학생 목록을 페이징하여 조회하고, 검색 조건에 따라 필터링합니다.
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
     * @param stdNo 조회할 학생의 학번
     * @return 조회된 학생 정보 DTO
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public StdInfoDto getStudentByStdNo(String stdNo) {
        StdInfo stdInfo = stdInfoRepository.findByStdNo(stdNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생 정보를 찾을 수 없습니다."));
        return convertToDto(stdInfo);
    }

    /**
     * 기존 학생 정보를 수정합니다.
     * 학번을 기준으로 학생을 찾아 수정하며, 이메일, 전화번호 중복 체크를 수행합니다.
     * user_info 테이블의 해당 학생 계정 비밀번호는 학번으로 재설정됩니다.
     *
     * @param stdNo 수정할 학생의 학번
     * @param dto 수정할 학생 정보 DTO
     * @return 수정된 학생 정보 DTO
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없거나 유효성 검사 실패 시
     * @throws IllegalStateException 연결된 사용자 계정을 찾을 수 없거나 기타 시스템 오류 시
     */
    @Transactional
    public StdInfoDto updateStudent(String stdNo, StdInfoDto dto) {
        // 1. 기존 학생 정보 조회 (std_info)
        StdInfo existingStudent = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        // 2. user_info 테이블의 해당 학생 계정 비밀번호 재설정 및 업데이트
        User existingUserAccount = userRepository.findByUserId(stdNo) // user_id는 학번과 동일
                .orElseThrow(() -> new IllegalStateException("해당 학생 학번(" + stdNo + ")에 연결된 사용자 계정을 찾을 수 없습니다."));

        String newDefaultPassword = stdNo;
        String newEncodedPassword = passwordEncoder.encode(newDefaultPassword);
        existingUserAccount.setUserPw(newEncodedPassword);
        existingUserAccount.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(existingUserAccount);


        // 3. 학과 코드 유효성 검사
        if (!DEPT_MAP.containsKey(dto.getSCSBJT_CD())) {
            throw new IllegalArgumentException("유효하지 않은 학과 코드입니다: " + dto.getSCSBJT_CD());
        }

        // 4. 이메일 중복 체크 (수정 시: 자기 자신의 이메일은 허용)
        if (dto.getSTD_EML_ADDR() != null && !dto.getSTD_EML_ADDR().trim().isEmpty()) {
            Optional<StdInfo> existingStudentWithEmail = stdInfoRepository.findByStdEmlAddr(dto.getSTD_EML_ADDR());
            if (existingStudentWithEmail.isPresent() && !existingStudentWithEmail.get().getStdNo().equals(stdNo)) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTD_EML_ADDR() + ")은 이미 다른 학생에게 등록된 이메일입니다.");
            }
        }

        // 5. 전화번호 중복 체크 (수정 시): 자기 자신의 전화번호는 허용
        if (dto.getSTD_TELNO() != null && !dto.getSTD_TELNO().trim().isEmpty()) {
            Optional<StdInfo> existingStudentWithTel = stdInfoRepository.findByStdTelno(dto.getSTD_TELNO());
            if (existingStudentWithTel.isPresent() && !existingStudentWithTel.get().getStdNo().equals(stdNo)) {
                throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTD_TELNO() + ")는 이미 다른 학생에게 등록된 전화번호입니다.");
            }
        }

        // 6. StdInfo 엔티티 필드 업데이트
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
        // ⭐⭐ 수정됨: userId2Value에 관리자 ID 직접 할당 (임시) ⭐⭐
        // TODO: 나중에 Spring Security 세션에서 현재 로그인한 관리자의 user_id를 가져와 할당하도록 변경
        existingStudent.setUserId2Value("admin"); // 하드코딩된 'admin' 사용 (사용자님이 언급하신 "admin" 데이터)
        
        StdInfo updatedEntity = stdInfoRepository.save(existingStudent);

        return convertToDto(updatedEntity);
    }

    /**
     * 특정 학번의 학생을 삭제합니다.
     * std_info에서 삭제뿐만 아니라 user_info의 해당 계정(학번/사번 기반)도 함께 삭제합니다.
     *
     * @param stdNo 삭제할 학생의 학번
     * @throws IllegalArgumentException 해당 학번의 학생을 찾을 수 없을 때
     */
    @Transactional
    public void deleteStudent(String stdNo) {
        StdInfo studentToDelete = stdInfoRepository.findByStdNo(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번(" + stdNo + ")의 학생을 찾을 수 없습니다."));

        // user_info에서도 해당 계정 삭제 (std_info 삭제 전에 user_info 삭제 시 외래 키 제약 조건에 문제 없을 수 있도록)
        Optional<User> userAccountToDelete = userRepository.findByUserId(stdNo);
        userAccountToDelete.ifPresent(userRepository::delete);

        stdInfoRepository.delete(studentToDelete);
    }

    /**
     * StdInfo 엔티티를 StdInfoDto DTO로 변환하는 헬퍼 메서드.
     * User 엔티티의 userPw는 DTO로 반환하지 않습니다 (보안상).
     *
     * @param stdInfo 변환할 StdInfo 엔티티
     * @return 변환된 StdInfoDto DTO
     */
    private StdInfoDto convertToDto(StdInfo stdInfo) {
        StdInfoDto dto = new StdInfoDto();
        dto.setSTD_ID(stdInfo.getStdId());
        dto.setSTD_NO(stdInfo.getStdNo());
        dto.setSTD_NM(stdInfo.getStdNm());
        dto.setSCSBJT_CD(stdInfo.getScsbjtCd());
        dto.setSCH_YR(stdInfo.getSchoolYear());
        dto.setENTR_DT(stdInfo.getEntranceDate());
        dto.setSTD_STAT_CD(stdInfo.getStatusCode());
        dto.setSTD_ZIP(stdInfo.getStdZip());
        dto.setSTD_ADDR(stdInfo.getStdAddr());
        dto.setSTD_DADDR(stdInfo.getStdDaddr());
        dto.setSTD_TELNO(stdInfo.getStdTelno());
        dto.setSTD_EML_ADDR(stdInfo.getStdEmlAddr());
        dto.setUSE_YN(stdInfo.getUseYn());
        
        dto.setUSER_ID2(stdInfo.getUserId2Value()); // 관리자 user_id를 직접 DTO에 설정
        return dto;
    }
}
