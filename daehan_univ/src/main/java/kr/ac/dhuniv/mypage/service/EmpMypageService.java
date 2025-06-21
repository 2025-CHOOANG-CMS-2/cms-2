package kr.ac.dhuniv.mypage.service;

import kr.ac.dhuniv.mypage.repository.EmpMypageRepository;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmpMypageService {

    private final EmpMypageRepository empMypageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String uploadBaseDir = System.getProperty("user.dir") + "/uploads";
    private final String uploadProfileDir = uploadBaseDir + "/empl_profile/";

    private static final Map<String, String> departmentCodeNameMap;
    private static final Map<String, String> positionCodeNameMap;

    static {
        Map<String, String> deptMap = new HashMap<>();
        deptMap.put("001", "국어국문학과");
        deptMap.put("002", "영어영문학과");
        deptMap.put("003", "컴퓨터공학과");
        deptMap.put("004", "경영학과");
        deptMap.put("005", "기계공학과");
        departmentCodeNameMap = Collections.unmodifiableMap(deptMap);

        Map<String, String> posMap = new HashMap<>();
        posMap.put("PROF", "교수");
        posMap.put("ASS_PROF", "부교수");
        posMap.put("AST_PROF", "조교수");
        posMap.put("LECT", "강사");
        posMap.put("STAFF", "직원");
        positionCodeNameMap = Collections.unmodifiableMap(posMap);
    }

    @Autowired
    public EmpMypageService(EmpMypageRepository empMypageRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.empMypageRepository = empMypageRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        Path path = Paths.get(uploadProfileDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("DEBUG: Upload directory created for employee profiles: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("ERROR: Could not create upload directory for employee profiles: " + path.toAbsolutePath() + " - " + e.getMessage());
            }
        }
    }

    public Optional<EmplInfoDto> getMypageInfo(User user) {
        Optional<EmplInfo> emplInfoOptional = empMypageRepository.findByUser(user);
        return emplInfoOptional.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Optional<EmplInfoDto> getEmplInfoDtoByUserId(String userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<EmplInfo> emplInfoOptional = empMypageRepository.findByUser(user);
            return emplInfoOptional.map(this::convertToDto);
        }
        return Optional.empty();
    }

    public Map<String, String> getDepartmentCodeNameMap() {
        return departmentCodeNameMap;
    }

    public Map<String, String> getPositionCodeNameMap() {
        return positionCodeNameMap;
    }

    @Transactional
    public boolean updatePassword(String userId, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (user.getUserPw() == null || !passwordEncoder.matches(currentPassword, user.getUserPw())) {
                System.out.println("DEBUG: 기존 비밀번호 불일치 또는 DB에 비밀번호 없음. UserID: " + userId + ", 입력 CurrentPw: " + currentPassword + ", DB Hash: " + user.getUserPw());
                return false;
            }
            
            if (passwordEncoder.matches(newPassword, user.getUserPw())) {
                System.out.println("DEBUG: 새 비밀번호가 기존 비밀번호와 같습니다. UserID: " + userId);
                return false; 
            }

            user.setUserPw(passwordEncoder.encode(newPassword)); 
            userRepository.save(user);
            System.out.println("DEBUG: User " + userId + " 비밀번호 성공적으로 업데이트됨.");
            return true;
        }
        System.out.println("DEBUG: User " + userId + " 비밀번호 업데이트를 위한 사용자 정보를 찾을 수 없음.");
        return false;
    }

    @Transactional
    public boolean updatePersonalInfo(String userId, EmplInfoDto updatedInfoDto) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            System.out.println("DEBUG: 개인 정보 업데이트 실패: 교직원 번호(" + userId + ")에 해당하는 사용자(User)를 찾을 수 없습니다.");
            return false;
        }
        Optional<EmplInfo> emplInfoOptional = empMypageRepository.findByUser(userOptional.get());

        if (emplInfoOptional.isPresent()) {
            EmplInfo existingEmplInfo = emplInfoOptional.get();

            if (updatedInfoDto.getSTAFF_EML_ADDR() != null && !existingEmplInfo.getEmplEmailAddr().equals(updatedInfoDto.getSTAFF_EML_ADDR())) {
                Optional<EmplInfo> duplicateEmailInfo = empMypageRepository.findByEmplEmailAddr(updatedInfoDto.getSTAFF_EML_ADDR());
                if (duplicateEmailInfo.isPresent()) {
                    EmplInfo foundDuplicate = duplicateEmailInfo.get();
                    if (!Objects.equals(foundDuplicate.getEmplId(), existingEmplInfo.getEmplId())) {
                        System.out.println("DEBUG: 입력하신 이메일 주소(" + updatedInfoDto.getSTAFF_EML_ADDR() + ")는 이미 사용 중입니다.");
                        throw new IllegalArgumentException("입력하신 이메일 주소는 이미 사용 중입니다.");
                    }
                }
            }

            existingEmplInfo.setEmplNm(updatedInfoDto.getSTAFF_NM());
            existingEmplInfo.setDeptCd(updatedInfoDto.getDEPT_CD());
            existingEmplInfo.setPositionCd(updatedInfoDto.getPOSITION_CD());
            
            // hireDt, createdBy는 EmplInfo 엔티티에 updatable=false로 설정되어 있으므로 여기서 설정하지 않습니다.
            // if (updatedInfoDto.getHIRE_DT() != null) { existingEmplInfo.setHireDt(updatedInfoDto.getHIRE_DT()); }
            // if (updatedInfoDto.getCREATED_BY() != null) { existingEmplInfo.setCreatedBy(updatedInfoDto.getCREATED_BY()); }

            // ⭐ 중요: STATUS_CD와 USE_YN은 다른 기능에서 업데이트될 수 있으므로 엔티티 레벨에서 updatable=false를 제거했음.
            // 따라서, DTO에서 값이 넘어왔을 때만 설정하고, null이면 기존 값을 유지하도록 조건부 업데이트.
            if (updatedInfoDto.getSTATUS_CD() != null) { 
                 existingEmplInfo.setEmplStatCd(updatedInfoDto.getSTATUS_CD());
            }
            if (updatedInfoDto.getUSE_YN() != null) { 
                 existingEmplInfo.setUseYn(updatedInfoDto.getUSE_YN());
            }

            existingEmplInfo.setEmplZip(updatedInfoDto.getZIP_CD());
            existingEmplInfo.setEmplAddr(updatedInfoDto.getADDR());
            existingEmplInfo.setEmplDaddr(updatedInfoDto.getDADDR());
            existingEmplInfo.setEmplTelno(updatedInfoDto.getSTAFF_TELNO());
            existingEmplInfo.setEmplEmailAddr(updatedInfoDto.getSTAFF_EML_ADDR());
            
            // PROFILE_IMAGE_URL은 별도 업로드 API를 통해 처리되므로 여기서 직접 설정하지 않습니다.
            // if (updatedInfoDto.getPROFILE_IMAGE_URL() != null) {
            //      existingEmplInfo.setProfileImageUrl(updatedInfoDto.getPROFILE_IMAGE_URL());
            // }
            

            empMypageRepository.save(existingEmplInfo);
            System.out.println("DEBUG: Employee " + userId + " 개인 정보 성공적으로 업데이트됨.");
            return true;
        }
        System.out.println("DEBUG: Employee " + userId + " 개인 정보 업데이트를 위한 교직원 정보를 찾을 수 없음.");
        return false;
    }

    @Transactional
    public String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        Path filePath = null;

        if (file.isEmpty()) {
            System.out.println("DEBUG: 업로드할 파일이 비어 있습니다.");
            return null;
        }

        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            System.out.println("DEBUG: 프로필 이미지 업로드 실패: 교직원 번호(" + userId + ")에 해당하는 사용자(User)를 찾을 수 없습니다.");
            return null;
        }
        Optional<EmplInfo> emplInfoOptional = empMypageRepository.findByUser(userOptional.get());

        if (emplInfoOptional.isPresent()) {
            EmplInfo emplInfo = emplInfoOptional.get();

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path uploadPath = Paths.get(uploadProfileDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            filePath = Paths.get(uploadProfileDir, uniqueFileName);

            Files.copy(file.getInputStream(), filePath);
            System.out.println("DEBUG: 파일이 저장되었습니다: " + filePath.toAbsolutePath());

            String imageUrl = "/uploads/empl_profile/" + uniqueFileName;
            emplInfo.setProfileImageUrl(imageUrl);
            empMypageRepository.save(emplInfo);
            System.out.println("DEBUG: EmplInfo " + userId + " 프로필 이미지 URL 업데이트됨: " + imageUrl);
            return imageUrl;
        } else {
            System.out.println("DEBUG: EmplInfo " + userId + "를 찾을 수 없어 프로필 이미지 URL을 업데이트할 수 없습니다.");
            if (filePath != null) {
                Files.deleteIfExists(filePath);
            }
            return null;
        }
    }

    private EmplInfoDto convertToDto(EmplInfo emplInfo) {
        EmplInfoDto dto = new EmplInfoDto();
        dto.setSTAFF_NO(emplInfo.getUser() != null ? emplInfo.getUser().getUserId() : null);
        dto.setSTAFF_NM(emplInfo.getEmplNm());
        dto.setDEPT_CD(emplInfo.getDeptCd());
        dto.setPOSITION_CD(emplInfo.getPositionCd());
        dto.setSTATUS_CD(emplInfo.getEmplStatCd());
        dto.setHIRE_DT(emplInfo.getHireDt());
        dto.setZIP_CD(emplInfo.getEmplZip());
        dto.setADDR(emplInfo.getEmplAddr());
        dto.setDADDR(emplInfo.getEmplDaddr());
        dto.setSTAFF_TELNO(emplInfo.getEmplTelno());
        dto.setSTAFF_EML_ADDR(emplInfo.getEmplEmailAddr());
        dto.setUSE_YN(emplInfo.getUseYn());
        dto.setCREATED_BY(emplInfo.getCreatedBy());
        dto.setPROFILE_IMAGE_URL(emplInfo.getProfileImageUrl());
        
        return dto;
    }
}
