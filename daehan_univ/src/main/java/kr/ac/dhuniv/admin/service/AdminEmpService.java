package kr.ac.dhuniv.admin.service;

import kr.ac.dhuniv.admin.repository.AdminEmpRepository;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
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
public class AdminEmpService {

    private final AdminEmpRepository empInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 부서 코드와 이름 매핑 (실제 운영 시에는 DB 관리 또는 외부 설정 파일 권장)
    private static final Map<String, String> DEPT_MAP = new HashMap<>();
    static {
        DEPT_MAP.put("101", "교무처"); DEPT_MAP.put("102", "학생처");
        DEPT_MAP.put("103", "입학처"); DEPT_MAP.put("104", "총무처");
        DEPT_MAP.put("105", "기획처"); DEPT_MAP.put("106", "산학협력단");
        DEPT_MAP.put("107", "도서관"); DEPT_MAP.put("108", "전산정보원");
        DEPT_MAP.put("109", "국제교류처"); DEPT_MAP.put("110", "연구처");
        DEPT_MAP.put("111", "진로취창업팀");
        DEPT_MAP.put("112", "입학관리팀"); DEPT_MAP.put("113", "시설관리팀");
        DEPT_MAP.put("114", "홍보팀"); DEPT_MAP.put("115", "감사팀");
    }

    // 재직 상태 코드와 라벨 매핑 (실제 운영 시에는 DB 관리 또는 외부 설정 파일 권장)
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("Y", "재직"); STATUS_MAP.put("N", "퇴직"); STATUS_MAP.put("L", "휴직");
        STATUS_MAP.put("S", "정직"); STATUS_MAP.put("R", "해고");
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
        log.info("[Service - insertEmployee] DTO 수신: {}", dto);
        log.info("[Service - insertEmployee] CREATED_BY 값: '{}'", dto.getCREATED_BY());

        try {
            // 1. 새로운 사번 자동 생성 (YYYYDDDSSS 형식)
            String currentYear = String.valueOf(LocalDate.now().getYear());
            String deptCode = dto.getDEPT_CD();

            if (!DEPT_MAP.containsKey(deptCode)) {
                throw new IllegalArgumentException("유효하지 않은 부서 코드입니다: " + deptCode);
            }

            String newEmplNo = generateEmployeeNo(currentYear, deptCode);
            dto.setSTAFF_NO(newEmplNo);

            // 2. 이메일 중복 체크 (empl_info 테이블 기준)
            if (dto.getSTAFF_EML_ADDR() != null && !dto.getSTAFF_EML_ADDR().trim().isEmpty()) {
                if (empInfoRepository.findByEmplEmailAddr(dto.getSTAFF_EML_ADDR()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTAFF_EML_ADDR() + ")은 이미 등록된 교직원의 이메일입니다.");
                }
            }

            // 3. 전화번호 중복 체크 (empl_info 테이블 기준)
            if (dto.getSTAFF_TELNO() != null && !dto.getSTAFF_TELNO().trim().isEmpty()) {
                if (empInfoRepository.findByEmplTelno(dto.getSTAFF_TELNO()).isPresent()) {
                    throw new IllegalArgumentException("입력하신 전화번호(" + dto.getSTAFF_TELNO() + ")는 이미 등록된 교직원의 전화번호입니다.");
                }
            }

            // 4. 재직 상태 코드 유효성 검사
            if (!STATUS_MAP.containsKey(dto.getSTATUS_CD())) {
                throw new IllegalArgumentException("유효하지 않은 재직 상태 코드입니다: " + dto.getSTATUS_CD());
            }

            // 5. **USER_INFO 테이블에 교직원 본인의 계정 생성 및 권한 부여 (가장 먼저 수행)**
            String userIdForAccount = newEmplNo;
            String defaultPassword = newEmplNo;
            String encodedPassword = passwordEncoder.encode(defaultPassword);

            if (userRepository.findByUserId(userIdForAccount).isPresent()) {
                throw new IllegalStateException("생성된 사번(" + userIdForAccount + ")에 해당하는 사용자 계정이 이미 존재합니다. 데이터 불일치 가능성.");
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
                    .userYn("Y")
                    .build();
            userRepository.save(newUserAccount);

            // 6. EmplInfo 엔티티에 연결할 관리자 ID 유효성 검사 (User 테이블에서 존재 여부만 확인)
            if (dto.getCREATED_BY() == null || dto.getCREATED_BY().trim().isEmpty()) {
                 log.error("[Service - insertEmployee] CREATED_BY 값이 null이거나 비어있어 유효성 검사 실패.");
                 throw new IllegalArgumentException("등록 관리자 ID (CREATED_BY)는 필수 값입니다.");
            }
            userRepository.findByUserId(dto.getCREATED_BY())
                .orElseThrow(() -> new IllegalStateException("등록 관리자 계정 (ID: " + dto.getCREATED_BY() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

            // 7. **EmplInfo 엔티티 빌드 및 저장**
            EmplInfo entity = EmplInfo.builder()
                    .user(newUserAccount)
                    .emplNm(dto.getSTAFF_NM())
                    .deptCd(dto.getDEPT_CD())
                    .positionCd(dto.getPOSITION_CD())
                    .emplStatCd(dto.getSTATUS_CD())
                    .hireDt(dto.getHIRE_DT())
                    .emplZip(dto.getZIP_CD())
                    .emplAddr(dto.getADDR())
                    .emplDaddr(dto.getDADDR())
                    .emplTelno(dto.getSTAFF_TELNO())
                    .emplEmailAddr(dto.getSTAFF_EML_ADDR())
                    .useYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y")
                    .createdBy(dto.getCREATED_BY())
                    .profileImageUrl(dto.getPROFILE_IMAGE_URL()) // --- DTO에서 URL 가져와서 엔티티에 설정 ---
                    .build();

            EmplInfo savedEntity = empInfoRepository.save(entity);

            return convertToDto(savedEntity);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("교직원 등록 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("교직원 등록 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
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
     * @param emplNo 조회할 교직원의 사번 (이는 User.userId와 동일)
     * @return 조회된 교직원 정보 DTO
     * @throws IllegalArgumentException 해당 사번의 교직원을 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public EmplInfoDto getEmployeeByEmplNo(String emplNo) {
        EmplInfo emplInfo = empInfoRepository.findByUser_UserId(emplNo)
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
        log.info("[Service - updateEmployee] DTO 수신 (사번: {}): {}", emplNo, dto);
        log.info("[Service - updateEmployee] CREATED_BY 값: '{}'", dto.getCREATED_BY());

        // 1. 해당 사번의 교직원 존재 여부 확인 (empl_info 테이블에서 User.userId를 통해 조회)
        EmplInfo existingEmployee = empInfoRepository.findByUser_UserId(emplNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번(" + emplNo + ")의 교직원을 찾을 수 없습니다."));

        // 2. user_info 테이블의 계정 비밀번호 재설정 및 업데이트
        User existingUserAccount = userRepository.findByUserId(emplNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 교직원 사번(" + emplNo + ")에 연결된 사용자 계정을 찾을 수 없습니다."));

        String newDefaultPassword = emplNo;
        String newEncodedPassword = passwordEncoder.encode(newDefaultPassword);
        existingUserAccount.setUserPw(newEncodedPassword);
        existingUserAccount.setUpdatedAt(LocalDateTime.now());
        existingUserAccount.setUserYn("Y");
        
        userRepository.save(existingUserAccount);

        // 3. 이메일 중복 체크 (수정 시: 자기 자신의 이메일은 중복으로 간주하지 않음)
        if (dto.getSTAFF_EML_ADDR() != null && !dto.getSTAFF_EML_ADDR().trim().isEmpty()) {
            Optional<EmplInfo> existingEmployeeWithEmail = empInfoRepository.findByEmplEmailAddr(dto.getSTAFF_EML_ADDR());
            if (existingEmployeeWithEmail.isPresent() && !existingEmployeeWithEmail.get().getUser().getUserId().equals(emplNo)) {
                throw new IllegalArgumentException("입력하신 이메일(" + dto.getSTAFF_EML_ADDR() + ")은 이미 다른 교직원에게 등록된 이메일입니다.");
            }
        }

        // 4. 전화번호 중복 체크 (수정 시: 자기 자신의 전화번호는 중복으로 간주하지 않음)
        if (dto.getSTAFF_TELNO() != null && !dto.getSTAFF_TELNO().trim().isEmpty()) {
            Optional<EmplInfo> existingEmployeeWithTel = empInfoRepository.findByEmplTelno(dto.getSTAFF_TELNO());
            if (existingEmployeeWithTel.isPresent() && !existingEmployeeWithTel.get().getUser().getUserId().equals(emplNo)) {
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

        // 7. EmplInfo 엔티티를 수정한 관리자 ID 유효성 검사
        if (dto.getCREATED_BY() == null || dto.getCREATED_BY().trim().isEmpty()) {
             log.error("[Service - updateEmployee] CREATED_BY 값이 null이거나 비어있어 유효성 검사 실패.");
             throw new IllegalArgumentException("수정 관리자 ID (CREATED_BY)는 필수 값입니다.");
        }
        userRepository.findByUserId(dto.getCREATED_BY())
            .orElseThrow(() -> new IllegalStateException("수정 관리자 계정 (ID: " + dto.getCREATED_BY() + ")을 찾을 수 없습니다. user_info 테이블에 해당 계정이 존재하는지 확인하세요."));

        // 8. EmplInfo 엔티티 필드 업데이트
        existingEmployee.setEmplNm(dto.getSTAFF_NM());
        existingEmployee.setDeptCd(dto.getDEPT_CD());
        existingEmployee.setPositionCd(dto.getPOSITION_CD());
        existingEmployee.setEmplStatCd(dto.getSTATUS_CD());
        existingEmployee.setHireDt(dto.getHIRE_DT());
        existingEmployee.setEmplZip(dto.getZIP_CD());
        existingEmployee.setEmplAddr(dto.getADDR());
        existingEmployee.setEmplDaddr(dto.getDADDR());
        existingEmployee.setEmplTelno(dto.getSTAFF_TELNO());
        existingEmployee.setEmplEmailAddr(dto.getSTAFF_EML_ADDR());
        existingEmployee.setUseYn(dto.getUSE_YN() != null ? dto.getUSE_YN() : "Y");
        existingEmployee.setCreatedBy(dto.getCREATED_BY());
        existingEmployee.setProfileImageUrl(dto.getPROFILE_IMAGE_URL()); // --- DTO에서 URL 가져와서 엔티티에 설정 ---
        
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
        // 1. empl_info에서 해당 교직원 정보 조회 (User.userId를 통해)
        EmplInfo employeeToDelete = empInfoRepository.findByUser_UserId(emplNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 사번(" + emplNo + ")의 교직원 정보를 찾을 수 없습니다."));

        // 2. empl_info에서 삭제 (외래키 제약조건으로 인해 User 계정보다 먼저 삭제)
        empInfoRepository.delete(employeeToDelete);

        // 3. user_info에서 해당 계정 삭제 (empl_info 삭제 후 진행)
        Optional<User> userAccountToDelete = userRepository.findByUserId(emplNo);
        userAccountToDelete.ifPresent(userRepository::delete);
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
     * User 엔티티의 userPw는 보안상 DTO로 반환하지 않습니다.
     *
     * @param emplInfo 변환할 EmplInfo 엔티티
     * @return 변환된 EmplInfoDto DTO
     */
    private EmplInfoDto convertToDto(EmplInfo emplInfo) {
        EmplInfoDto dto = new EmplInfoDto();
        dto.setSTAFF_NO(emplInfo.getUser().getUserId()); // User 엔티티의 userId를 통해 사번 가져오기
        dto.setSTAFF_NM(emplInfo.getEmplNm());
        dto.setDEPT_CD(emplInfo.getDeptCd());
        dto.setPOSITION_CD(emplInfo.getPositionCd());
        dto.setSTATUS_CD(emplInfo.getEmplStatCd()); // 엔티티 필드명 'emplStatCd'를 DTO 'STATUS_CD'에 매핑
        dto.setHIRE_DT(emplInfo.getHireDt());
        dto.setZIP_CD(emplInfo.getEmplZip());
        dto.setADDR(emplInfo.getEmplAddr());
        dto.setDADDR(emplInfo.getEmplDaddr());
        dto.setSTAFF_TELNO(emplInfo.getEmplTelno());
        dto.setSTAFF_EML_ADDR(emplInfo.getEmplEmailAddr());
        dto.setUSE_YN(emplInfo.getUseYn());
        dto.setCREATED_BY(emplInfo.getCreatedBy());
        dto.setPROFILE_IMAGE_URL(emplInfo.getProfileImageUrl()); // --- 엔티티에서 URL 가져와서 DTO에 설정 ---
        return dto;
    }
}