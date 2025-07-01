package kr.ac.dhuniv.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostStatisticsResponse {
    private long totalPosts; // 전체 공지사항 수
    private long activePosts; // 활성 공지사항 수 (isPublished = true)
    private long importantPosts; // 중요 공지사항 수 (isImportant = true)
    private double averageViewCount; // 평균 조회수
    private long recent30DaysNewPosts; // 최근 30일간 새로 등록된 공지사항 수 (선택 사항)
    private double recent30DaysViewIncreaseRate; // 최근 30일간 조회수 증가율 (선택 사항)
}
