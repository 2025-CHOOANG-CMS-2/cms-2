package kr.ac.dhuniv.admin.service;

import kr.ac.dhuniv.admin.repository.AdminEmpRepository;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
import kr.ac.dhuniv.user.User; // User 엔티티 임포트 유지 (필수)
import kr.ac.dhuniv.user.Role; // Role 엔티티 임포트 유지
import kr.ac.dhuniv.user.repository.UserRepository; // UserRepository 주입 유지 (필수)
import kr.ac.dhuniv.user.repository.RoleRepository; // Role 정보 조회에 필요하므로 유지

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

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
public class AdminEmpService {

    private final AdminEmpRepository empInfoRepository;
    private final UserRepository userRepository; // 교직원 로그인 계정 생성 시 사용 및 관리자 User 조회 시 사용
    private final RoleRepository roleRepository; // 교직원 로그인 계정 권한 부여 시 사용
    private final PasswordEncoder passwordEncoder;

    // 부서 코드와 이름 매핑
    private static final Map<String, String> DEPT_MAP = new HashMap<>();
    static {
        DEPT_MAP.put("101", "교무처"); DEPT_MAP.put("102", "학생처");
        DEPT_MAP.put("103", "입학처"); DEPT_MAP.put("104", "총무처");
        DEPT_MAP.put("105", "기획처"); DEPT_MAP.put("106", "산학협력단");
        DEPT_MAP.put("107", "도서관"); DEPT_MAP.put("108", "전산정보원");
        DEPT_MAP.put("109", "국제교류처"); DEPT_MAP.put("110", "연구처");
        DEPT_MAP.put("111", "진로취창업팀");
    }

    // 재직 상태 코드와 라벨 매핑
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("Y", "재직"); STATUS_MAP.put("N", "퇴직"); STATUS_MAP.put("L", "휴직");
    }

    /**
     * 새로운 교직원(Employee)을 등록합니다.
     * 사번 자동 생성, 이메일/전화번호 중복 체크, 유효성 검사,
     * 그리고 user_info 테이블에 교직원 계정 생성 및 비밀번호 암호화, 권한 부여를 수행합니다.
     * 비밀번호는 사번을 암호화하여 사용합니다.
     *
     * @param dto 등록할 교직원 정보 DTO
     * @return 등록된 교직원 정보 DTO
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws IllegalStateException 권한 정보 조회 실패 시 등
     */
    @Transactional
    public EmplInfoDto insertEmployee(EmplInfoDto dto) {
        try {
            // 1. 새로운 사번 자동 생성 (YYYYDDDSSS 형식)
            String currentYear = String.valueOf(LocalDate.now().getYear());
            String deptCode = dto.getDEPT_CD(); 

            if (!DEPT_MAP.containsKey(deptCode)) {
                throw new IllegalArgumentException("유효하지 않은 부서 코드입니다: " + deptCode);
            }

            String newEmplNo = generateEmployeeNo(currentYear, deptCode); 
            dto.setSTAFF_NO(newEmplNo); 

            // 2. 이메일 중복 체크 (empl_info 테이블)
            if (dto.getSTAFF_EML_ADDR() != null && !dto.getSTAFF_EML_ADDR().trim().isEmpty()) {
                if (empInfoRepository.findByEmplEmailAddr(dto.getSTAFF_EML_ADDR()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTAFF_EML_ADDR() + ")은 이미 등록된 교직원의 이메일입니다.");
                }
            }

            // 3. 전화번호 중복 체크 (empl_info 테이블)
            if (dto.getSTAFF_TELNO() != null && !dto.getSTAFF_TELNO().trim().isEmpty()) {
                if (empInfoRepository.findByEmplTelno(dto.getSTAFF_TELNO()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTAFF_TELNO() + ")는 이미 등록된 교직원의 전화번호입니다.");
                }
            }

            // 4. 재직 상태 코드 유효성 검사
            if (!STATUS_MAP.containsKey(dto.getSTATUS_CD())) {
                throw new IllegalArgumentException("유효하지 않은 재직 상태 코드입니다: " + dto.getSTATUS_CD());
            }

            // 5. user_info 테이블에 교직원 본인의 계정 생성 및 권한 부여
            String userIdForAccount = newEmplNo; // 교직원 계정의 ID는 사번으로 설정
            String defaultPassword = newEmplNo; 
            String encodedPassword = passwordEncoder.encode(defaultPassword); 

            if (userRepository.findByUserId(userIdForAccount).isPresent()) {
                throw new IllegalStateException("생성된 사번(" + userIdForAccount + ")에 해당하는 사용자 계정이 이미 존재합니다. 재시도 필요.");
            }

            Role employeeRole = roleRepository.findByRoleName("EMPLOYEE")
                    .orElseThrow(() -> new IllegalStateException("'EMPLOYEE' 권한을 찾을 수 없습니다. role_info 테이블에 'EMPLOYEE' 권한이 존재하는지 확인하세요."));
            
            User newUserAccount = User.builder()
                    .userId(userIdForAccount)
                    .userPw(encodedPassword)
                    .roles(Collections.singletonList(employeeRole))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .failedLoginCnt(0)
                    .build();
            userRepository.save(newUserAccount); 
            
            // ⭐⭐⭐ 6. EmplInfo 엔티티에 연결할 관리자 User 객체 조회 ⭐⭐⭐
            // DTO에서 받은 USER_ID2 값을 사용하여 User 테이블에서 해당 관리자 ID의 User 객체를 찾습니다.
            User adminUser = userRepository.findByUserId(dto.getUSER_ID2()) // 예: "admin" 이라는 user_id로 조회
                    .orElseThrow(() -> new IllegalStateException("등록 관리자 계정 (ID: " + dto.getUSER_ID2() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

            // ⭐⭐⭐ 7. EmplInfo 엔티티 빌드 및 저장 - 'user' 필드에 관리자 User 객체 연결 ⭐⭐⭐
            // EmplInfo 엔티티에 'private User user;' 필드가 있으므로, user() 빌더 메서드 사용
            EmplInfo entity = EmplInfo.builder()
                    .emplNo(dto.getSTAFF_NO())
                    .emplNm(dto.getSTAFF_NM())
                    .deptCd(dto.getDEPT_CD())
                    .positionCd(dto.getPOSITION_CD())
                    .emplStatCd(dto.getSTATUS_CD())
                    .hireDt(dto.getHIRE_DT()) // HIRE_DT 필드 사용
                    .emplZip(dto.getZIP_CD())
                    .emplAddr(dto.getADDR())
                    .emplDaddr(dto.getDADDR())
                    .emplTelno(dto.getSTAFF_TELNO())
                    .emplEmailAddr(dto.getSTAFF_EML_ADDR())
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y")
                    .user(adminUser) // ⭐⭐ DTO에서 받은 USER_ID2에 해당하는 User 객체를 'user' 필드에 연결 ⭐⭐
                    .build();

            EmplInfo savedEntity = empInfoRepository.save(entity);

            return convertToDto(savedEntity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("교직원 등록 실패: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("교직원 등록 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("교직원 등록 처리 중 예상치 못한 오류 발생", e);
        }
    }

    /**
     * 교직원(Employee) 목록을 페이징하여 조회하고, 검색 조건에 따라 필터링합니다.
     */
    @Transactional(readOnly = true)
    public Page<EmplInfoDto> getAllEmployees(Pageable pageable, String searchName, String searchDept, String searchStatus) {
        Specification<EmplInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchName != null && !searchName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("emplNm")), "%" + searchName.toLowerCase() + "%"));
            }
            if (searchDept != null && !searchDept.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("deptCd"), searchDept));
            }
            if (searchStatus != null && !searchStatus.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("emplStatCd"), searchStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EmplInfo> employeePage = empInfoRepository.findAll(spec, pageable);
        return employeePage.map(this::convertToDto);
    }

    /**
     * 특정 사번의 교직원 상세 정보를 조회합니다.
     * @param emplNo 조회할 교직원의 사번
     * @return 조회된 교직원 정보 DTO
     * @throws IllegalArgumentException 해당 사번의 교직원을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public EmplInfoDto getEmployeeByEmplNo(String emplNo) {
        EmplInfo emplInfo = empInfoRepository.findByEmplNo(emplNo)
            .orElseThrow(() -> new IllegalArgumentException("해당 사번(" + emplNo + ")의 교직원 정보를 찾을 수 없습니다."));
        return convertToDto(emplInfo);
    }


    /**
     * 기존 교직원 정보를 수정합니다.
     * 사번을 기준으로 교직원을 찾아 수정하며, 이메일, 전화번호 중복 체크를 수행합니다.
     * user_info 테이블의 계정 비밀번호는 사번으로 재설정됩니다.
     *
     * @param emplNo 수정할 교직원의 사번
     * @param dto 수정할 교직원 정보 DTO
     * @return 수정된 교직원 정보 DTO
     * @throws IllegalArgumentException 해당 사번의 교직원을 찾을 수 없거나 유효성 검사 실패 시
     */
    @Transactional
    public EmplInfoDto updateEmployee(String emplNo, EmplInfoDto dto) {
        // 1. 해당 사번의 교직원 존재 여부 확인 (empl_info)
        EmplInfo existingEmployee = empInfoRepository.findByEmplNo(emplNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번(" + emplNo + ")의 교직원을 찾을 수 없습니다."));

        // 2. user_info 테이블의 계정 비밀번호 재설정 및 업데이트
        User existingUserAccount = userRepository.findByUserId(emplNo) // user_id는 사번과 동일
                .orElseThrow(() -> new IllegalArgumentException("해당 교직원 사번(" + emplNo + ")에 연결된 사용자 계정을 찾을 수 없습니다."));

        String newDefaultPassword = emplNo;
        String newEncodedPassword = passwordEncoder.encode(newDefaultPassword);
        existingUserAccount.setUserPw(newEncodedPassword);
        existingUserAccount.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(existingUserAccount);

        // 3. 이메일 중복 체크 (수정 시: 자기 자신의 이메일은 허용)
        if (dto.getSTAFF_EML_ADDR() != null && !dto.getSTAFF_EML_ADDR().trim().isEmpty()) {
            Optional<EmplInfo> existingEmployeeWithEmail = empInfoRepository.findByEmplEmailAddr(dto.getSTAFF_EML_ADDR());
            if (existingEmployeeWithEmail.isPresent() && !existingEmployeeWithEmail.get().getEmplNo().equals(emplNo)) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTAFF_EML_ADDR() + ")은 이미 다른 교직원에게 등록된 이메일입니다.");
            }
        }

        // 4. 전화번호 중복 체크 (수정 시: 자기 자신의 전화번호는 허용)
        if (dto.getSTAFF_TELNO() != null && !dto.getSTAFF_TELNO().trim().isEmpty()) {
            Optional<EmplInfo> existingEmployeeWithTel = empInfoRepository.findByEmplTelno(dto.getSTAFF_TELNO());
            if (existingEmployeeWithTel.isPresent() && !existingEmployeeWithTel.get().getEmplNo().equals(emplNo)) {
                throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTAFF_TELNO() + ")는 이미 다른 교직원에게 등록된 전화번호입니다.");
            }
        }

        // 5. 부서 코드 유효성 검사
        if (!DEPT_MAP.containsKey(dto.getDEPT_CD())) {
            throw new IllegalArgumentException("유효하지 않은 부서 코드입니다: " + dto.getDEPT_CD());
        }

        // 6. 재직 상태 코드 유효성 검사
        if (!STATUS_MAP.containsKey(dto.getSTATUS_CD())) {
            throw new IllegalArgumentException("유효하지 않은 재직 상태 코드입니다: " + dto.getSTATUS_CD());
        }

        // ⭐⭐⭐ 7. EmplInfo 엔티티에 연결할 관리자 User 객체 조회 ⭐⭐⭐
        // DTO에서 받은 USER_ID2 값을 사용하여 User 테이블에서 해당 관리자 ID의 User 객체를 찾습니다.
        User adminUser = userRepository.findByUserId(dto.getUSER_ID2()) // 예: "admin" 이라는 user_id로 조회
                .orElseThrow(() -> new IllegalStateException("등록 관리자 계정 (ID: " + dto.getUSER_ID2() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

        // ⭐⭐⭐ 8. EmplInfo 엔티티 필드 업데이트 - 'user' 필드에 관리자 User 객체 연결 ⭐⭐⭐
        existingEmployee.setEmplNm(dto.getSTAFF_NM());
        existingEmployee.setDeptCd(dto.getDEPT_CD());
        existingEmployee.setPositionCd(dto.getPOSITION_CD());
        existingEmployee.setEmplStatCd(dto.getSTATUS_CD());
        existingEmployee.setHireDt(dto.getHIRE_DT()); // HIRE_DT 필드 사용
        existingEmployee.setEmplZip(dto.getZIP_CD());
        existingEmployee.setEmplAddr(dto.getADDR());
        existingEmployee.setEmplDaddr(dto.getDADDR());
        existingEmployee.setEmplTelno(dto.getSTAFF_TELNO());
        existingEmployee.setEmplEmailAddr(dto.getSTAFF_EML_ADDR());
        existingEmployee.setUseYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y");
        existingEmployee.setUser(adminUser); // ⭐⭐ DTO에서 받은 USER_ID2에 해당하는 User 객체를 'user' 필드에 연결 ⭐⭐
        
        EmplInfo updatedEntity = empInfoRepository.save(existingEmployee);

        return convertToDto(updatedEntity);
    }

    /**
     * 특정 사번의 교직원을 삭제합니다.
     * empl_info에서 삭제뿐만 아니라 user_info의 해당 계정(사번 기반)도 함께 삭제합니다.
     *
     * @param emplNo 삭제할 교직원의 사번
     * @throws IllegalArgumentException 해당 사번의 교직원을 찾을 수 없을 때
     */
    @Transactional
    public void deleteEmployee(String emplNo) {
        EmplInfo employeeToDelete = empInfoRepository.findByEmplNo(emplNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번(" + emplNo + ")의 교직원을 찾을 수 없습니다."));

        // user_info에서도 해당 계정 삭제 (empl_info 삭제 전에 user_info 삭제 시 외래 키 제약 조건에 문제 없을 수 있도록)
        Optional<User> userAccountToDelete = userRepository.findByUserId(emplNo);
        userAccountToDelete.ifPresent(userRepository::delete);

        // empl_info에서 삭제
        empInfoRepository.delete(employeeToDelete);
    }

    /**
     * 새로운 교직원 사번을 생성합니다. (YYYY + DDD + SSS 형식)
     * 예: 2025101001 (2025년, 교무처(101), 순번 001)
     *
     * @param year 현재 년도 (String)
     * @param deptCd 부서 코드 (String)
     * @return 생성된 새로운 사번
     */
    private String generateEmployeeNo(String year, String deptCd) {
        if (year == null || year.isEmpty()) {
            throw new IllegalArgumentException("년도 정보가 없어 사번을 생성할 수 없습니다.");
        }
        if (deptCd == null || deptCd.isEmpty()) {
            throw new IllegalArgumentException("부서 코드가 없어 사번을 생성할 수 없습니다.");
        }

        Optional<Integer> maxSequenceOptional = empInfoRepository.findMaxSequenceForEmployeeId(year, deptCd);
        int nextSequence = maxSequenceOptional.orElse(0) + 1;

        String sequencePart = String.format("%03d", nextSequence);

        return year + deptCd + sequencePart;
    }


    /**
     * EmplInfo 엔티티를 EmplInfoDto DTO로 변환하는 헬퍼 메서드.
     * User 엔티티의 userPw는 DTO로 반환하지 않습니다 (보안상).
     *
     * @param emplInfo 변환할 EmplInfo 엔티티
     * @return 변환된 EmplInfoDto DTO
     */
    private EmplInfoDto convertToDto(EmplInfo emplInfo) {
        EmplInfoDto dto = new EmplInfoDto();
        dto.setSTAFF_NO(emplInfo.getEmplNo());
        dto.setSTAFF_NM(emplInfo.getEmplNm());
        dto.setDEPT_CD(emplInfo.getDeptCd());
        dto.setPOSITION_CD(emplInfo.getPositionCd());
        dto.setSTATUS_CD(emplInfo.getEmplStatCd());
        dto.setHIRE_DT(emplInfo.getHireDt()); // HIRE_DT 필드 사용
        dto.setZIP_CD(emplInfo.getEmplZip());
        dto.setADDR(emplInfo.getEmplAddr());
        dto.setDADDR(emplInfo.getEmplDaddr());
        dto.setSTAFF_TELNO(emplInfo.getEmplTelno());
        dto.setSTAFF_EML_ADDR(emplInfo.getEmplEmailAddr());
        dto.setUSE_YN(emplInfo.getUseYn());
        // USER_ID2는 emplInfo의 user 필드에서 가져옵니다.
        // EmplInfo.user가 null이 아닐 때만 userId를 가져옵니다.
        if (emplInfo.getUser() != null) {
            dto.setUSER_ID2(emplInfo.getUser().getUserId()); // ⭐⭐ User 객체에서 userId 가져와서 사용 ⭐⭐
        } else {
            dto.setUSER_ID2(null); // 연결된 User가 없으면 null
        }
        return dto;
    }
}
