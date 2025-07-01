package kr.ac.dhuniv.counsel.dto; // 공용 DTO 패키지를 사용하는 것을 추천

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PageDto<T> {

    private final List<T> content;          // 현재 페이지의 데이터 목록
    private final int pageNumber;         // 현재 페이지 번호 (0부터 시작)
    private final int pageSize;           // 페이지 당 데이터 수
    private final int totalPages;         // 전체 페이지 수
    private final long totalElements;     // 전체 데이터 수
    private final boolean isFirst;          // 첫 페이지 여부
    private final boolean isLast;           // 마지막 페이지 여부

    /**
     * Spring Data JPA의 Page 객체를 이 DTO로 변환하는 생성자
     */
    public PageDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.isFirst = page.isFirst();
        this.isLast = page.isLast();
    }
    
    /**
     * Mybatis 사용 시, 직접 데이터를 받아 DTO를 생성하는 생성자
     */
    public PageDto(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (pageSize > 0) ? (int) Math.ceil((double) totalElements / pageSize) : 1;
        this.isFirst = (pageNumber == 0);
        this.isLast = (pageNumber >= this.totalPages - 1);
    }
}