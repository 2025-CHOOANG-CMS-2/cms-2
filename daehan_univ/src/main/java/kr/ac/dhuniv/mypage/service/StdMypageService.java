package kr.ac.dhuniv.mypage.service;

import kr.ac.dhuniv.mypage.repository.StdMypageRepository;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.std_info.dto.StdInfoDto;
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
public class StdMypageService {

    private final StdMypageRepository stdMypageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String uploadBaseDir = System.getProperty("user.dir") + "/uploads";
    private final String uploadProfileDir = uploadBaseDir + "/std_profile/";

    private static final Map<String, String> departmentCodeNameMap;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("001", "국어국문학과");
        map.put("002", "영어영문학과");
        map.put("003", "철학과");
        map.put("004", "정치외교학과");
        map.put("005", "심리학과");
        map.put("006", "사회복지학과");
        map.put("007", "통계학과");
        map.put("008", "천문학과");
        map.put("009", "화학과");
        map.put("010", "기계공학과");
        map.put("011", "컴퓨터공학과");
        map.put("012", "건축학과");
        map.put("013", "스마트시스템과학과");
        map.put("014", "동양화과");
        map.put("015", "조소과");
        map.put("016", "공예과");
        map.put("017", "교육학과");
        map.put("018", "식품영양학과");
        map.put("019", "의류학과");
        map.put("020", "성악과");
        map.put("021", "의예과");
        departmentCodeNameMap = Collections.unmodifiableMap(map);
    }

    @Autowired
    public StdMypageService(StdMypageRepository stdMypageRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.stdMypageRepository = stdMypageRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        Path path = Paths.get(uploadProfileDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("DEBUG: Upload directory created: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("ERROR: Could not create upload directory: " + path.toAbsolutePath() + " - " + e.getMessage());
            }
        }
    }

    public Optional<StdInfoDto> getMypageInfo(User user) {
        Optional<StdInfo> stdInfoOptional = stdMypageRepository.findByUser(user);
        return stdInfoOptional.map(this::convertToDto);
    }

    public Optional<StdInfoDto> getStdInfoDtoByUserId(String userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<StdInfo> stdInfoOptional = stdMypageRepository.findByUser(user);
            return stdInfoOptional.map(this::convertToDto);
        }
        return Optional.empty();
    }

    public Map<String, String> getDepartmentCodeNameMap() {
        return departmentCodeNameMap;
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
            // user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("DEBUG: User " + userId + " 비밀번호 성공적으로 업데이트됨.");
            return true;
        }
        System.out.println("DEBUG: User " + userId + " 비밀번호 업데이트를 위한 사용자 정보를 찾을 수 없음.");
        return false;
    }

    @Transactional
    public boolean updatePersonalInfo(String userId, StdInfoDto updatedInfoDto) {
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            System.out.println("DEBUG: 개인 정보 업데이트 실패: 학번(" + userId + ")에 해당하는 사용자(User)를 찾을 수 없습니다.");
            return false;
        }
        Optional<StdInfo> stdInfoOptional = stdMypageRepository.findByUser(userOptional.get());

        if (stdInfoOptional.isPresent()) {
            StdInfo existingStdInfo = stdInfoOptional.get();

            if (updatedInfoDto.getSTD_EML_ADDR() != null && !existingStdInfo.getStdEmlAddr().equals(updatedInfoDto.getSTD_EML_ADDR())) {
                Optional<StdInfo> duplicateEmailInfo = stdMypageRepository.findByStdEmlAddr(updatedInfoDto.getSTD_EML_ADDR());
                if (duplicateEmailInfo.isPresent()) {
                    StdInfo foundDuplicate = duplicateEmailInfo.get();
                    if (!Objects.equals(foundDuplicate.getStdId(), existingStdInfo.getStdId())) {
                        System.out.println("DEBUG: 입력하신 이메일 주소(" + updatedInfoDto.getSTD_EML_ADDR() + ")는 이미 사용 중입니다.");
                        throw new IllegalArgumentException("입력하신 이메일 주소는 이미 사용 중입니다.");
                    }
                }
            }

            existingStdInfo.setStdNm(updatedInfoDto.getSTD_NM());
            existingStdInfo.setScsbjtCd(updatedInfoDto.getSCSBJT_CD());
            existingStdInfo.setSchoolYear(updatedInfoDto.getSCH_YR());
            existingStdInfo.setEntranceDate(updatedInfoDto.getENTR_DT());
            existingStdInfo.setStatusCode(updatedInfoDto.getSTD_STAT_CD());
            existingStdInfo.setStdZip(updatedInfoDto.getSTD_ZIP());
            existingStdInfo.setStdAddr(updatedInfoDto.getSTD_ADDR());
            existingStdInfo.setStdDaddr(updatedInfoDto.getSTD_DADDR());
            existingStdInfo.setStdTelno(updatedInfoDto.getSTD_TELNO());
            existingStdInfo.setStdEmlAddr(updatedInfoDto.getSTD_EML_ADDR());
            existingStdInfo.setUseYn(updatedInfoDto.getUSE_YN());
            existingStdInfo.setCreatedBy(updatedInfoDto.getCREATED_BY());

            stdMypageRepository.save(existingStdInfo);
            System.out.println("DEBUG: Student " + userId + " 개인 정보 성공적으로 업데이트됨.");
            return true;
        }
        System.out.println("DEBUG: Student " + userId + " 개인 정보 업데이트를 위한 학생 정보를 찾을 수 없음.");
        return false;
    }

    @Transactional
    public String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        Path filePath = null; // filePath 변수 선언 및 초기화

        if (file.isEmpty()) {
            System.out.println("DEBUG: 업로드할 파일이 비어 있습니다.");
            return null;
        }

        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            System.out.println("DEBUG: 프로필 이미지 업로드 실패: 학번(" + userId + ")에 해당하는 사용자(User)를 찾을 수 없습니다.");
            return null;
        }
        Optional<StdInfo> stdInfoOptional = stdMypageRepository.findByUser(userOptional.get());

        if (stdInfoOptional.isPresent()) {
            StdInfo stdInfo = stdInfoOptional.get();

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            filePath = Paths.get(uploadProfileDir, uniqueFileName); // filePath에 값 할당

            Files.copy(file.getInputStream(), filePath);
            System.out.println("DEBUG: 파일이 저장되었습니다: " + filePath.toAbsolutePath());

            String imageUrl = "/uploads/std_profile/" + uniqueFileName; 
            stdInfo.setProfileImageUrl(imageUrl);
            stdMypageRepository.save(stdInfo);
            System.out.println("DEBUG: StdInfo " + userId + " 프로필 이미지 URL 업데이트됨: " + imageUrl);
            return imageUrl;
        } else {
            System.out.println("DEBUG: StdInfo " + userId + "를 찾을 수 없어 프로필 이미지 URL을 업데이트할 수 없습니다.");
            if (filePath != null) { // 파일이 저장된 경우에만 삭제 시도
                Files.deleteIfExists(filePath);
            }
            return null;
        }
    }

    /**
     * Helper Method: StdInfo 엔티티를 StdInfoDto DTO로 변환합니다.
     * @param stdInfo 변환할 StdInfo 엔티티
     * @return 변환된 StdInfoDto DTO
     */
    private StdInfoDto convertToDto(StdInfo stdInfo) {
        StdInfoDto dto = new StdInfoDto();
        dto.setSTD_ID(stdInfo.getStdId());
        
        if (stdInfo.getUser() != null) {
            dto.setSTD_NO(stdInfo.getUser().getUserId());
        } else {
            dto.setSTD_NO(null);
        }

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
        dto.setPROFILE_IMAGE_URL(stdInfo.getProfileImageUrl());
        dto.setCREATED_BY(stdInfo.getCreatedBy());
        
        return dto;
    }
}
