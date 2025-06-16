package kr.ac.dhuniv.user.service;



import kr.ac.dhuniv.user.Role;
import kr.ac.dhuniv.user.User;
import kr.ac.dhuniv.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ CustomUserDetailsService
 * - Spring Security가 사용자 인증 시 호출하는 서비스
 * - DB에서 사용자 정보를 조회하고 UserDetails 객체를 생성
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // ✅ 사용자 정보를 조회하기 위한 리포지토리

    /**
     * ✅ loadUserByUsername
     * - Spring Security가 사용자 인증 시 호출하는 메소드
     * - userId로 사용자 정보를 조회하고 UserDetails 반환
     *
     * @param userId 로그인 시도한 사용자 ID
     * @return UserDetails (Spring Security 인증 객체)
     * @throws UsernameNotFoundException 사용자 정보가 없을 경우 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // ✅ DB에서 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // ✅ 권한 정보를 SimpleGrantedAuthority 리스트로 변환 (스트림 사용하지 않음)
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // user.getRoles()는 List<String> 형태라고 가정
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName())); // ROLE_XXX 형식
        }

        // ✅ UserDetails 객체 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())        // 사용자 ID
                .password(user.getUserPw())        // 암호화된 비밀번호
                .authorities(authorities)          // 권한 리스트
                .build();
    }
}
