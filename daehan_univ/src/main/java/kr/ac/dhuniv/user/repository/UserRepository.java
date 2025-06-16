package kr.ac.dhuniv.user.repository;

import kr.ac.dhuniv.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> { // User의 PK는 id (Long)

    // User 엔티티의 userId (DB 컬럼 user_id) 필드로 User를 찾는 메서드 추가
    Optional<User> findByUserId(String userId); // User 엔티티에 `private String userId;` 필드가 있어야 함
}