/**
 * counsel-history.js
 * 학생의 상담 조회/결과 페이지의 모든 동적 기능을 처리합니다.
 */
let currentPage = 0; // 현재 페이지 번호 (0부터 시작)
let currentResultId = null; // 만족도 평가를 위해 현재 선택된 상담 결과 ID
let currentRating = 0; // 현재 선택된 별점

// =========================================================================
// 별점 평가 관련 함수
// =========================================================================

/**
 * 별점 시스템 이벤트 리스너를 등록하는 함수
 */
function initializeRatingSystem() {
    const stars = document.querySelectorAll('.star-rating .fa-star');
    const saveBtn = document.getElementById('save-rating-btn');

    stars.forEach(star => {
        star.addEventListener('click', () => {
            if (star.parentElement.classList.contains('readonly')) return;
            currentRating = parseInt(star.dataset.value);
            renderStars(currentRating);
        });
    });

    if (saveBtn) {
        saveBtn.addEventListener('click', saveSatisfactionScore);
    }
}

/**
 * 별점 UI를 렌더링하는 함수
 * @param {number} rating - 표시할 별점 (1~5)
 */
function renderStars(rating) {
    const stars = document.querySelectorAll('.star-rating .fa-star');
    stars.forEach(star => {
        star.classList.toggle('selected', parseInt(star.dataset.value) <= rating);
    });
}

/**
 * 만족도 점수를 서버에 저장하는 함수
 */
async function saveSatisfactionScore() {
    if (currentRating === 0) {
        alert('만족도 점수를 선택해주세요.');
        return;
    }

    if (!currentResultId) {
        alert('오류: 대상 상담 정보를 찾을 수 없습니다.');
        return;
    }

    try {
        const response = await fetch(`/api/counseling/results/${currentResultId}/satisfaction`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ score: currentRating })
        });

        if (!response.ok) {
            throw new Error('평점 저장에 실패했습니다.');
        }
        
        alert('소중한 의견 감사합니다!');
        
        const detailModal = bootstrap.Modal.getInstance(document.getElementById('resultDetailModal'));
        if (detailModal) {
            detailModal.hide();
        }
        
        fetchAndRenderHistory(currentPage);


    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}


// =========================================================================
// 페이지 로딩 및 기본 기능 초기화
// =========================================================================

document.addEventListener("DOMContentLoaded", () => {
    initializeEventListeners();
    fetchAndRenderHistory(currentPage);
    initializeRatingSystem(); 
});

/**
 * 탭과 필터에 이벤트 리스너를 등록하는 함수
 */
function initializeEventListeners() {
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            document.querySelectorAll(".tab-btn, .tab-content").forEach(el => el.classList.remove("active"));
            this.classList.add("active");
            document.getElementById(this.dataset.tab).classList.add("active");
            currentPage = 0; 
            fetchAndRenderHistory(currentPage);
        });
    });

    document.querySelectorAll(".filter-select").forEach(select => {
        select.addEventListener("change", () => {
            currentPage = 0; 
            fetchAndRenderHistory(currentPage);
        });
    });
}

/**
 * 필터 값을 읽어 백엔드 API를 호출하고, 목록과 페이징 UI를 그리는 메인 함수
 * @param {number} page - 조회할 페이지 번호
 */
async function fetchAndRenderHistory(page = 0) {
    currentPage = page;
    
    const activeTab = document.querySelector('.tab-btn.active').dataset.tab;
    
    let period, status, type;
    let listContainer, paginationContainer;

    if (activeTab === 'appointments') {
        period = document.getElementById("periodFilter")?.value || 'all';
        status = document.getElementById("statusFilter")?.value || 'all';
        type = document.getElementById("typeFilter")?.value || 'all';
        listContainer = document.querySelector("#appointments .history-list");
        paginationContainer = document.querySelector("#appointments .pagination-container");
    } else { // 'results' 탭
        period = document.getElementById("resultsPeriodFilter")?.value || 'all';
        type = document.getElementById("resultsTypeFilter")?.value || 'all';
        status = 'COMPLETED';
        listContainer = document.querySelector("#results .history-list");
        paginationContainer = document.querySelector("#results .pagination-container");
    }

    if (!listContainer || !paginationContainer) return;

    listContainer.innerHTML = '<div class="loading-state">로딩 중...</div>';
    paginationContainer.innerHTML = ''; 

    try {
        const url = `/api/counseling/history?page=${page}&size=10&period=${period}&status=${status}&type=${type}`;
        
        const response = await fetch(url);
        if (!response.ok) throw new Error("상담 내역을 불러오는 데 실패했습니다.");
        
        const pagedData = await response.json();
        const historyList = pagedData.content;
        
        listContainer.innerHTML = '';
        if (historyList.length === 0) {
            listContainer.innerHTML = '<div class="empty-state"><p>조회된 상담 내역이 없습니다.</p></div>';
            return;
        }

        historyList.forEach(item => {
            const itemHtml = createHistoryItemHtml(item, activeTab);
            listContainer.insertAdjacentHTML('beforeend', itemHtml);
        });

        renderPagination(pagedData, paginationContainer);

    } catch (error) {
        console.error(error);
        listContainer.innerHTML = `<div class="empty-state error"><p>${error.message}</p></div>`;
    }
}

/**
 * 페이징 컨트롤 UI를 생성하는 함수
 */
function renderPagination(pagedData, container) {
    container.innerHTML = '';
    if (pagedData.totalPages <= 1) return;

    const nav = document.createElement('nav');
    const ul = document.createElement('ul');
    ul.className = 'pagination justify-content-center';

    let li = createPageItem('‹', pagedData.pageNumber - 1, pagedData.first);
    ul.appendChild(li);

    for (let i = 0; i < pagedData.totalPages; i++) {
        li = createPageItem(i + 1, i, false, pagedData.pageNumber === i);
        ul.appendChild(li);
    }

    li = createPageItem('›', pagedData.pageNumber + 1, pagedData.last);
    ul.appendChild(li);

    nav.appendChild(ul);
    container.appendChild(nav);
}


/**
 * 페이징 UI의 각 버튼 아이템을 생성하는 헬퍼 함수
 */
function createPageItem(text, page, isDisabled, isActive = false) {
    const li = document.createElement('li');
    li.className = 'page-item';
    if (isDisabled) li.classList.add('disabled');
    if (isActive) li.classList.add('active');
    
    const a = document.createElement('a');
    a.className = 'page-link';
    a.href = '#';
    a.textContent = text;
    a.addEventListener('click', (event) => {
        event.preventDefault();
        if (!isDisabled) {
            fetchAndRenderHistory(page);
        }
    });
    
    li.appendChild(a);
    return li;
}

/**
 * 상담 내역 아이템 HTML 생성
 */
function createHistoryItemHtml(item, activeTab) {
    if (!item) return '';

    const statusMap = { 'PENDING': '승인대기', 'APPROVED': '예약됨', 'COMPLETED': '완료', 'REJECTED': '반려됨', 'CANCELED': '예약취소' };
    const statusClass = item.status ? item.status.toLowerCase() : 'unknown';
    const date = new Date(item.applyDateTime);
    const dateText = `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')} (${'일월화수목금토'[date.getDay()]}) ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;

    let actionButtons = '';
    if (activeTab === 'appointments') {
        if (item.status === 'APPROVED') {
            actionButtons = `<button class="btn btn-primary btn-sm" onclick="startCounseling('${item.applyId}')"><i class="fas fa-comments"></i> 상담 시작</button>`;
        }
        if (item.status === 'PENDING' || item.status === 'APPROVED') {
            actionButtons += `<button class="btn btn-outline btn-sm" onclick="modifyBooking('${item.applyId}')"><i class="fas fa-edit"></i> 예약 변경</button>
                              <button class="btn btn-outline btn-sm" onclick="cancelBooking('${item.applyId}')"><i class="fas fa-times"></i> 예약 취소</button>`;
        }
    } else { // 'results' 탭
        const itemJson = JSON.stringify(item).replace(/'/g, "\\'");
        actionButtons = `<button class="btn btn-primary btn-sm" onclick='showResultDetailModal(${itemJson})'><i class="fas fa-eye"></i> 상세보기</button>`;                       
    }

    return `
        <div class="history-item" data-status="${statusClass}">
            <div class="history-header">
                <div class="history-date">${dateText}</div>
                <div class="history-status status-${statusClass}">${statusMap[item.status] || '알 수 없음'}</div>
            </div>
            <div class="history-details">
                <div class="detail-item"><span><i class="fas fa-user-md"></i> 상담사: ${item.counselorName || ''}</span></div>
                <div class="detail-item"><span><i class="fas fa-tag"></i> 유형: ${item.counselingType || ''}</span></div>
                <div class="detail-item"><span><i class="fas fa-video"></i> 방식: ${item.counselingMethod || '미정'}</span></div>
            </div>
            <div class="history-summary">
                <h4>${(item.content || '내용 없음').substring(0, 20)}...</h4>
                <p>${item.content || '내용 없음'}</p>
            </div>
            <div class="history-actions">${actionButtons}</div>
        </div>
    `;
}

/**
 * 상담 결과 모달을 열고, 전달받은 item 객체로 UI를 즉시 구성하는 함수
 */
function showResultDetailModal(item) {
    const modalElement = document.getElementById('resultDetailModal');
    if (!modalElement) {
        console.error('Modal element #resultDetailModal not found.');
        return;
    }
    const detailModal = bootstrap.Modal.getOrCreateInstance(modalElement);
    
    currentResultId = item.resultId; 
    currentRating = 0; 

    const loadingEl = document.getElementById('modal-loading');
    const contentEl = document.getElementById('modal-content-area');
    const satisfactionSection = document.getElementById('satisfaction-section');
    
    // [수정] 로딩 아이콘을 숨기고 콘텐츠 영역을 보이도록 명시적으로 처리합니다.
    if(loadingEl) loadingEl.style.display = 'none';
    if(contentEl) contentEl.style.display = 'block';
    if(satisfactionSection) satisfactionSection.style.display = 'none';

    // 모달 내용 채우기
    const counselorNameEl = document.getElementById('modal-counselorName');
    const counselDateEl = document.getElementById('modal-counselDate');
    const counselingTypeEl = document.getElementById('modal-counselingType');
    const counselingContentEl = document.getElementById('modal-counselingContent');

    if(counselorNameEl) counselorNameEl.textContent = item.counselorName;
    
    // [수정] 항상 '상담 예약일'을 기준으로 표시하여 날짜 불일치 문제를 해결합니다.
    const date = new Date(item.applyDateTime);
    if(counselDateEl) counselDateEl.textContent = `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
    
    if(counselingTypeEl) counselingTypeEl.textContent = item.counselingType;
    if(counselingContentEl) counselingContentEl.textContent = item.resultContent || '등록된 결과가 없습니다.';

    // 만족도 평가 섹션 처리
    if (item.status === 'COMPLETED' && satisfactionSection) {
        satisfactionSection.style.display = 'block';
        const ratingInput = document.getElementById('rating-input-area');
        const ratingDisplay = document.getElementById('rating-display-area');
        const starContainer = document.querySelector('.star-rating');
        
        if (item.satisfactionScore && item.satisfactionScore > 0) {
            if(ratingInput) ratingInput.style.display = 'none';
            if(ratingDisplay) ratingDisplay.style.display = 'block';
            if(starContainer) starContainer.classList.add('readonly');
            renderStars(item.satisfactionScore);
        } else {
            if(ratingInput) ratingInput.style.display = 'block';
            if(ratingDisplay) ratingDisplay.style.display = 'none';
            if(starContainer) starContainer.classList.remove('readonly');
            renderStars(0);
        }
    }
    
    detailModal.show();
}
