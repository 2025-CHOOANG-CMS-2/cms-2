package kr.ac.dhuniv.user.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dhuniv.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // User의 PK가 String(user_id)일 경우
}