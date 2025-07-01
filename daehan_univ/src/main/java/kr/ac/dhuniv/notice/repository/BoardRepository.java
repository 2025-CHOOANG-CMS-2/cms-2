package kr.ac.dhuniv.notice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.notice.domain.Board;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByBoardName(String boardName);
    Optional<Board> findByBoardType(String boardType);
}