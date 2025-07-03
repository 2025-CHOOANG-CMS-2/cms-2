// 전역 변수 선언
let currentPage = 0; // 현재 페이지 (0부터 시작)
let pageSize = 20;   // 페이지당 항목 수 (HTML의 기본 선택값과 일치)
let currentSortBy = 'createdAt'; // 현재 정렬 기준 필드
let currentDirection = 'desc';   // 현재 정렬 방향 (desc/asc)
let currentSearchTitle = '';     // 현재 검색 제목
let currentBoardId = 'all';      // 현재 카테고리 필터 (초기값 'all')
let currentIsImportant = false;  // 현재 중요 공지사항 필터 (초기값 false)


/**
 * 로그인한 학생 정보를 서버에서 가져와 HTML에 표시합니다.
 * API URL: /api/user/me
 */
async function fetchStudentInfo() {
    try {
        const response = await fetch("/api/user/me");
        if (!response.ok) {
            // 로그인되어 있지 않거나, 권한이 없는 경우 (401, 403 등)
            console.warn("학생 정보를 불러오지 못했습니다. 로그인 상태를 확인하세요.", response.status);
            // 기본값 표시 (로그인 필요 또는 알 수 없음)
            const headerUserName = document.getElementById('headerUserName');
            const headerUserDept = document.getElementById('headerUserDept');
            if (headerUserName) headerUserName.textContent = '알 수 없음';
            if (headerUserDept) headerUserDept.textContent = '로그인 필요';
            // 사이드바에도 유사하게 업데이트 (HTML에 해당 ID가 있다면)
            const sidebarUserName = document.getElementById('sidebarUserName');
            const sidebarUserDept = document.getElementById('sidebarUserDept');
            if (sidebarUserName) sidebarUserName.textContent = '알 수 없음';
            if (sidebarUserDept) sidebarUserDept.textContent = '로그인 필요';
            return;
        }

        const studentInfo = await response.json();
        console.log("로그인한 학생 정보:", studentInfo);

        // 헤더에 학생 정보 업데이트
        const headerUserName = document.getElementById('headerUserName');
        const headerUserDept = document.getElementById('headerUserDept');
        if (headerUserName) headerUserName.textContent = studentInfo.stdNm || '이름 없음';
        if (headerUserDept) {
            // 학과명 매핑 (admin-emp-service의 DEPT_LIST와 유사한 매핑이 프론트에 있다면 활용)
            // 여기서는 임시로 DTO에서 받은 SCSBJT_CD를 그대로 표시하거나, 별도의 매핑 함수를 사용해야 합니다.
            // 예시: const deptName = getDepartmentNameByCode(studentInfo.scsbjtCd);
            headerUserDept.textContent = `${studentInfo.scsbjtCd || ''} ${studentInfo.schYr || ''}학년`;
        }

        // 사이드바에 학생 정보 업데이트 (HTML에 해당 ID가 있다면)
        const sidebarUserName = document.getElementById('sidebarUserName');
        const sidebarUserDept = document.getElementById('sidebarUserDept');
        if (sidebarUserName) sidebarUserName.textContent = studentInfo.stdNm || '이름 없음';
        if (sidebarUserDept) {
            sidebarUserDept.textContent = `${studentInfo.scsbjtCd || ''} ${studentInfo.schYr || ''}학년`;
        }

    } catch (error) {
        console.error("학생 정보를 불러오는 중 오류 발생:", error);
        const headerUserName = document.getElementById('headerUserName');
        const headerUserDept = document.getElementById('headerUserDept');
        if (headerUserName) headerUserName.textContent = '오류';
        if (headerUserDept) headerUserDept.textContent = '정보 로드 실패';
        const sidebarUserName = document.getElementById('sidebarUserName');
        const sidebarUserDept = document.getElementById('sidebarUserDept');
        if (sidebarUserName) sidebarUserName.textContent = '오류';
        if (sidebarUserDept) sidebarUserDept.textContent = '정보 로드 실패';
    }
}



// ===========================================
// 유틸리티 및 헬퍼 함수
// ===========================================


/**
 * 현재 시간 표시를 업데이트합니다.
 */
function updateTime() {
    const now = new Date();
    const timeString = now.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    const currentTimeElement = document.getElementById('currentTime');
    if (currentTimeElement) {
        currentTimeElement.textContent = timeString;
    }
}

/**
 * 사이드바를 토글합니다. (모바일 뷰에서 사용)
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('show');
    }
}

/**
 * 파일 사이즈를 읽기 쉬운 형식으로 포맷팅합니다.
 * @param {number} bytes - 파일 크기 (바이트)
 * @returns {string} 포맷팅된 파일 크기 문자열
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 카테고리 ID를 이름으로 변환합니다.
 * @param {number} boardId - 게시판 ID
 * @returns {string} 게시판 이름
 */
function getCategoryNameById(boardId) {
    switch (boardId) {
        case 1: return '학사공지';
        case 2: return '장학공지';
        case 3: return '취업공지';
        case 4: return '행사공지';
        case 5: return '일반공지';
        default: return '기타'; // 중요 공지 필터는 별도로 처리되므로 여기에 포함하지 않음
    }
}

/**
 * 카테고리 ID에 따른 CSS 배지 클래스를 반환합니다.
 * @param {number} boardId - 게시판 ID
 * @returns {string} CSS 클래스 문자열
 */
function getCategoryBadgeClass(boardId) {
    switch (boardId) {
        case 1: return 'badge-academic'; // 학사공지
        case 2: return 'badge-scholarship'; // 장학공지
        case 3: return 'badge-general'; // 취업공지는 일반으로 간주
        case 4: return 'badge-event'; // 행사공지
        case 5: return 'badge-general'; // 일반공지
        default: return 'badge-general';
    }
}

/**
 * ⭐⭐⭐ 모든 모달과 백드롭을 강제로 정리하는 함수 ⭐⭐⭐
 * 이 함수는 모달이 제대로 닫히지 않아 백드롭이 남아있는 문제를 해결합니다.
 */
function cleanupModalsAndBackdrops() {
    console.log('[Cleanup] 모든 모달과 백드롭 정리 시작.');
    document.querySelectorAll('.modal.show').forEach(modalElement => {
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
            modalInstance.hide();
        }
    });

    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.remove();
        console.log('[Cleanup] modal-backdrop 제거됨.');
    });

    document.body.classList.remove('modal-open');
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';

    console.log('[Cleanup] 모든 모달과 백드롭 정리 완료.');
}

// ===========================================
// 데이터 로드 및 UI 렌더링 함수
// ===========================================

/**
 * 대시보드 통계 데이터를 로드하여 표시합니다.
 */
async function loadStatistics() {
    try {
        const response = await fetch('/api/student/posts/statistics');
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('totalNoticesCountDisplay').textContent = stats.totalPosts;
            document.getElementById('importantNoticesCountDisplay').textContent = stats.importantPosts;
            document.getElementById('activeNoticesCountDisplay').textContent = stats.activePosts; // 활성 공지
            document.getElementById('recent30DaysNewPostsDisplay').textContent = stats.recent30DaysNewPosts; // 최근 30일 신규
        } else {
            console.error('Failed to load statistics:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

/**
 * 공지사항 목록을 서버에서 불러와 화면에 표시합니다.
 */
async function loadNotices() {
    const queryParams = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        sortBy: currentSortBy.split(',')[0], // 정렬 기준 필드
        direction: currentSortBy.split(',')[1] || 'desc', // 정렬 방향
    });

    if (currentSearchTitle) {
        queryParams.append('searchTitle', currentSearchTitle);
    }
    if (currentBoardId && currentBoardId !== 'all') {
        queryParams.append('boardId', currentBoardId);
    }
    if (currentIsImportant) {
        queryParams.append('isImportant', true);
    }

    try {
        const response = await fetch(`/api/student/posts?${queryParams.toString()}`);
        if (response.ok) {
            const pageData = await response.json();
            displayNotices(pageData.content);
            renderPagination(pageData);
        } else {
            console.error('Failed to load notices:', response.status, response.statusText);
            document.getElementById('noticeList').innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-4 text-danger">
                        <i class="bi bi-exclamation-circle me-2"></i>공지사항을 불러오지 못했습니다.
                    </td>
                </tr>
            `;
        }
    } catch (error) {
        console.error('Error loading notices:', error);
        document.getElementById('noticeList').innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4 text-danger">
                    <i class="bi bi-exclamation-circle me-2"></i>네트워크 오류로 공지사항을 불러올 수 없습니다.
                </td>
            </tr>
        `;
    }
}

/**
 * 공지사항 목록을 테이블에 표시합니다.
 * @param {Array<Object>} notices - 표시할 공지사항 객체 배열
 */
function displayNotices(notices) {
    const noticeTableBody = document.getElementById('noticeList');
    if (!noticeTableBody) return;

    noticeTableBody.innerHTML = ''; // 기존 내용 지우기

    if (!notices || notices.length === 0) {
        noticeTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="bi bi-info-circle me-2"></i>표시할 공지사항이 없습니다.
                </td>
            </tr>
        `;
        return;
    }

    let noticeNumber = (currentPage * pageSize) + 1; // 페이지에 따른 번호 시작

    // 중요 공지사항을 먼저 렌더링 (isImportant가 true인 경우)
    const importantNotices = notices.filter(notice => notice.IS_IMPORTANT);
    const generalNotices = notices.filter(notice => !notice.IS_IMPORTANT);

    // 중요 공지사항 렌더링
    importantNotices.forEach(notice => {
        const createdAt = notice.CREATED_AT ? new Date(notice.CREATED_AT).toLocaleDateString('ko-KR') : 'N/A';
        const row = document.createElement('tr');
        row.className = 'important-row'; // 중요 공지사항 스타일
        row.setAttribute('data-post-id', notice.POST_ID); // 상세보기 클릭을 위한 ID
        row.innerHTML = `
            <td><i class="bi bi-pin-fill text-danger"></i></td>
            <td>
                <div class="notice-title-cell">
                    <strong>${notice.TITLE}</strong>
                    ${isNewNotice(notice.CREATED_AT) ? '<span class="badge badge-new ms-2">NEW</span>' : ''}
                </div>
            </td>
            <td><span class="category-badge ${getCategoryBadgeClass(notice.BOARD_ID)}">${getCategoryNameById(notice.BOARD_ID)}</span></td>
            <td>${notice.VIEW_COUNT || 0}</td>
            <td>${notice.MODIFIED_BY || 'N/A'}</td> <!-- 작성부서 정보 -->
            <td>${createdAt}</td>
        `;
        noticeTableBody.appendChild(row);
    });

    // 일반 공지사항 렌더링
    generalNotices.forEach(notice => {
        const createdAt = notice.CREATED_AT ? new Date(notice.CREATED_AT).toLocaleDateString('ko-KR') : 'N/A';
        const row = document.createElement('tr');
        row.setAttribute('data-post-id', notice.POST_ID); // 상세보기 클릭을 위한 ID
        row.innerHTML = `
            <td>${noticeNumber++}</td>
            <td>
                <div class="notice-title-cell">
                    ${notice.TITLE}
                    ${isNewNotice(notice.CREATED_AT) ? '<span class="badge badge-new ms-2">NEW</span>' : ''}
                    ${notice.ATTACHMENTS && notice.ATTACHMENTS.length > 0 ? '<i class="bi bi-paperclip ms-1 text-muted"></i>' : ''}
                </div>
            </td>
            <td><span class="category-badge ${getCategoryBadgeClass(notice.BOARD_ID)}">${getCategoryNameById(notice.BOARD_ID)}</span></td>
            <td>${notice.VIEW_COUNT || 0}</td>
            <td>${notice.MODIFIED_BY || 'N/A'}</td> <!-- 작성부서 정보 -->
            <td>${createdAt}</td>
        `;
        noticeTableBody.appendChild(row);
    });
}

/**
 * 게시글이 최근에 작성되었는지 확인합니다 (예: 3일 이내).
 * @param {string} createdAtDateString - 게시글 작성일 (ISO 8601 형식 문자열)
 * @returns {boolean} 최근 게시글 여부
 */
function isNewNotice(createdAtDateString) {
    if (!createdAtDateString) return false;
    const createdDate = new Date(createdAtDateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - createdDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 3; // 3일 이내면 NEW 표시
}

/**
 * 페이지네이션 UI를 렌더링합니다.
 * @param {Object} pageData - 백엔드에서 받은 Page 객체 데이터
 */
function renderPagination(pageData) {
    const paginationContainer = document.getElementById('paginationContainer');
    if (!paginationContainer) return;

    paginationContainer.innerHTML = ''; // 기존 페이지네이션 초기화

    const ul = document.createElement('ul');
    ul.className = 'pagination-custom'; // HTML에 정의된 커스텀 클래스

    // 이전 페이지 버튼
    const prevBtn = document.createElement('button');
    prevBtn.className = `page-btn ${pageData.first ? 'disabled' : ''}`;
    prevBtn.innerHTML = `<i class="bi bi-chevron-left"></i>`;
    prevBtn.addEventListener('click', () => {
        if (!pageData.first) {
            currentPage--;
            loadNotices();
        }
    });
    ul.appendChild(prevBtn);

    // 페이지 번호 버튼들 (최대 5개 표시)
    let startPage = Math.max(0, pageData.number - 2);
    let endPage = Math.min(pageData.totalPages - 1, pageData.number + 2);

    if (endPage - startPage < 4) { // 표시할 페이지가 5개 미만이면 조정
        if (startPage === 0) {
            endPage = Math.min(pageData.totalPages - 1, 4);
        } else if (endPage === pageData.totalPages - 1) {
            startPage = Math.max(0, pageData.totalPages - 5);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-btn ${i === pageData.number ? 'active' : ''}`;
        pageBtn.textContent = i + 1; // 페이지 번호는 1부터 시작
        pageBtn.addEventListener('click', () => {
            currentPage = i;
            loadNotices();
        });
        ul.appendChild(pageBtn);
    }

    // 다음 페이지 버튼
    const nextBtn = document.createElement('button');
    nextBtn.className = `page-btn ${pageData.last ? 'disabled' : ''}`;
    nextBtn.innerHTML = `<i class="bi bi-chevron-right"></i>`;
    nextBtn.addEventListener('click', () => {
        if (!pageData.last) {
            currentPage++;
            loadNotices();
        }
    });
    ul.appendChild(nextBtn);

    paginationContainer.appendChild(ul);
}

/**
 * 특정 ID의 공지사항 상세 데이터를 불러와 모달을 채웁니다.
 * @param {string} postId - 조회할 공지사항의 ID
 */
async function openNoticeDetail(postId) {
    try {
        // 백엔드 API 호출 (조회수 증가 로직 포함)
        const response = await fetch(`/api/student/posts/${postId}`);
        if (response.ok) {
            const notice = await response.json();

            // 모달 내용 설정
            document.getElementById('modalTitle').textContent = notice.TITLE || '제목 없음';
            document.getElementById('modalAuthor').textContent = notice.WRITER_EMPL_ID || 'N/A';
            document.getElementById('modalDate').textContent = notice.CREATED_AT ? new Date(notice.CREATED_AT).toLocaleDateString('ko-KR') : 'N/A';
            document.getElementById('modalViews').textContent = notice.VIEW_COUNT || 0;
            document.getElementById('modalContent').innerHTML = notice.CONTENT || '내용 없음';

            // 카테고리 배지 설정
            const categoryBadge = document.getElementById('modalCategory');
            categoryBadge.className = `category-badge ${getCategoryBadgeClass(notice.BOARD_ID)}`;
            categoryBadge.textContent = getCategoryNameById(notice.BOARD_ID);

            // 첨부파일 설정
            const attachmentSection = document.getElementById('attachmentSection');
            const attachmentList = document.getElementById('attachmentList');
            
            if (notice.ATTACHMENTS && notice.ATTACHMENTS.length > 0) {
                attachmentSection.style.display = 'block';
                attachmentList.innerHTML = '';
                
                notice.ATTACHMENTS.forEach(file => {
                    const attachmentItem = document.createElement('div');
                    attachmentItem.className = 'attachment-item';
                    attachmentItem.innerHTML = `
                        <i class="bi bi-file-earmark me-2"></i>
                        <div class="flex-grow-1">
                            <div class="fw-medium">${file.ORIGINAL_FILE_NAME}</div>
                            <small class="text-muted">${formatFileSize(file.FILE_SIZE)}</small>
                        </div>
                        <a href="/api/attachments/download/${file.UUID}" target="_blank" class="btn btn-sm btn-outline-primary">
                            <i class="bi bi-download"></i>
                        </a>
                    `;
                    attachmentList.appendChild(attachmentItem);
                });
            } else {
                attachmentSection.style.display = 'none';
                attachmentList.innerHTML = ''; // 첨부파일 없을 시 목록 비우기
            }

            // 모달 표시
            const modal = new bootstrap.Modal(document.getElementById('noticeDetailModal'));
            modal.show();

            // 모달이 닫힐 때 목록 새로고침 (조회수 갱신)
            const noticeDetailModalElement = document.getElementById('noticeDetailModal');
            if (noticeDetailModalElement) {
                noticeDetailModalElement.addEventListener('hidden.bs.modal', loadNotices, { once: true }); // 한 번만 실행
            }

        } else {
            console.error('Failed to load notice detail:', response.status, response.statusText);
            alert('공지사항 상세 정보를 불러오지 못했습니다.');
        }
    } catch (error) {
        console.error('Error loading notice detail:', error);
        alert('네트워크 오류로 공지사항 상세 정보를 불러올 수 없습니다.');
    }
}

/**
 * 로그아웃 처리 함수
 */
function logout() {
    // 실제 로그아웃 API 호출 로직 (예: 세션 무효화)
    // fetch('/api/logout', { method: 'POST' })
    // .then(response => {
    //     if (response.ok) {
    //         window.location.href = 'login.html'; // 로그인 페이지로 리다이렉트
    //     } else {
    //         alert('로그아웃 실패');
    //     }
    // }).catch(error => console.error('Logout error:', error));
    
    // 현재는 임시로 페이지 이동
    if (confirm('로그아웃 하시겠습니까?')) {
        window.location.href = 'login.html';
    }
}

// ===========================================
// 이벤트 리스너 및 초기화
// ===========================================

document.addEventListener('DOMContentLoaded', () => {
    // 현재 시간 업데이트 시작
    setInterval(updateTime, 1000);
    updateTime();

	
	fetchStudentInfo(); // ⭐ 이 줄을 추가합니다. ⭐
	
    // 사이드바 토글 버튼 이벤트
    const toggleSidebarBtn = document.getElementById('toggleSidebarBtn');
    if (toggleSidebarBtn) {
        toggleSidebarBtn.addEventListener('click', toggleSidebar);
    }

    // 로그아웃 버튼 이벤트
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // 검색 입력 필드 (Enter 키)
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                currentSearchTitle = searchInput.value.trim();
                currentPage = 0; // 검색 시 첫 페이지로
                loadNotices();
            }
        });
    }

    // 정렬 선택 (select box)
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', () => {
            currentSortBy = sortSelect.value;
            currentPage = 0; // 정렬 변경 시 첫 페이지로
            loadNotices();
        });
    }

    // 페이지당 항목 수 선택 (select box)
    const itemsPerPageSelect = document.getElementById('itemsPerPage');
    if (itemsPerPageSelect) {
        itemsPerPageSelect.addEventListener('change', () => {
            pageSize = parseInt(itemsPerPageSelect.value);
            currentPage = 0; // 페이지 크기 변경 시 첫 페이지로
            loadNotices();
        });
    }

    // 카테고리 필터 버튼들
    document.querySelectorAll('.filter-buttons .filter-btn').forEach(button => {
        button.addEventListener('click', function() {
            // 모든 필터 버튼의 'active' 클래스 제거
            document.querySelectorAll('.filter-buttons .filter-btn').forEach(btn => btn.classList.remove('active'));
            // 클릭된 버튼에 'active' 클래스 추가
            this.classList.add('active');
            // 선택된 카테고리 ID 업데이트
            currentBoardId = this.dataset.categoryId;
            // 중요 공지 필터 상태 업데이트 (isImportant 필터 버튼이 클릭된 경우)
            currentIsImportant = (currentBoardId === 'important');
            // 만약 'important' 버튼이 클릭되었다면, boardId는 'all'로 리셋
            if (currentIsImportant) {
                currentBoardId = 'all';
            }
        });
    });

    // 필터 적용 버튼
    const applyFilterBtn = document.getElementById('applyFilterBtn');
    if (applyFilterBtn) {
        applyFilterBtn.addEventListener('click', () => {
            currentSearchTitle = searchInput.value.trim(); // 검색어 최종 적용
            currentPage = 0; // 필터 적용 시 첫 페이지로
            loadNotices();
        });
    }

    // 필터 초기화 버튼
    const resetFilterBtn = document.getElementById('resetFilterBtn');
    if (resetFilterBtn) {
        resetFilterBtn.addEventListener('click', () => {
            searchInput.value = '';
            currentSearchTitle = '';
            sortSelect.value = 'createdAt,desc'; // 기본 정렬로 초기화
            itemsPerPageSelect.value = '20'; // 기본 표시 개수로 초기화
            
            // 카테고리 필터 초기화 (전체 버튼 활성화)
            document.querySelectorAll('.filter-buttons .filter-btn').forEach(btn => btn.classList.remove('active'));
            const allCategoryBtn = document.querySelector('.filter-buttons .filter-btn[data-category-id="all"]');
            if (allCategoryBtn) {
                allCategoryBtn.classList.add('active');
            }

            currentBoardId = 'all';
            currentIsImportant = false;
            currentPage = 0; // 필터 초기화 시 첫 페이지로
            loadNotices();
        });
    }

    // 공지사항 테이블 행 클릭 이벤트 (상세보기 모달 열기) - 이벤트 위임
    const noticeListTableBody = document.getElementById('noticeList');
    if (noticeListTableBody) {
        noticeListTableBody.addEventListener('click', (event) => {
            const row = event.target.closest('tr');
            if (row && row.dataset.postId) { // data-post-id 속성이 있는 행만 처리
                openNoticeDetail(row.dataset.postId);
            }
        });
    }

    // 페이지 로드 시 공지사항 목록 및 통계 로드
    loadNotices();
    loadStatistics();

    // 사이드바 외부 클릭 시 닫기 (모바일)
    document.addEventListener('click', function(event) {
        const sidebar = document.getElementById('sidebar');
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isToggleButton = event.target.closest('#toggleSidebarBtn'); // 토글 버튼 ID로 변경
        
        if (sidebar && !isClickInsideSidebar && !isToggleButton && window.innerWidth <= 768) {
            sidebar.classList.remove('show');
        }
    });
});
