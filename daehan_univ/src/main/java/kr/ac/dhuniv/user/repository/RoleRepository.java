package kr.ac.dhuniv.user.repository;

import kr.ac.dhuniv.user.Role; // Role 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Role 엔티티의 PK 타입이 Long (id)이므로 JpaRepository<Role, Long>으로 선언합니다.
public interface RoleRepository extends JpaRepository<Role, Long> {
    // roleName으로 Role 엔티티를 찾는 메서드 (권한 부여 시 'ROLE_EMPLOYEE' 등 이름으로 조회)
    Optional<Role> findByRoleName(String roleName);
}
