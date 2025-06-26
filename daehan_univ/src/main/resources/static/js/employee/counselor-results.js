/**
 * counselor-results.js
 * 상담사의 상담 결과 관리(작성 및 수정) 페이지의 모든 동적 기능을 처리합니다.
 */

// 전역 변수로 현재 페이지 번호를 관리합니다.
let currentPage = 0;

// 페이지 로딩이 완료되면, 필터에 이벤트 리스너를 등록하고 첫 페이지 목록을 불러옵니다.
document.addEventListener("DOMContentLoaded", () => {
    initializeFilterListeners();
    fetchAndRenderResults(currentPage);
});

/**
 * 기간, 상담 유형 등 필터 변경 시 목록을 새로고침하는 이벤트 리스너를 등록합니다.
 */
function initializeFilterListeners() {
    document.querySelectorAll(".filter-select").forEach(select => {
        select.addEventListener("change", () => {
            currentPage = 0; // 필터 변경 시 첫 페이지로 리셋
            fetchAndRenderResults(currentPage);
        });
    });
}

/**
 * [API 호출] 상담 결과 목록(완료된 상담 + 결과 작성이 필요한 상담)을 가져와 화면에 그립니다.
 * @param {number} page - 조회할 페이지 번호 (0부터 시작)
 */
async function fetchAndRenderResults(page = 0) {
    currentPage = page;

    // 필터 값 가져오기
    const period = document.getElementById("resultsDateFilter")?.value || 'all';
    const type = document.getElementById("resultsTypeFilter")?.value || 'all';
    
    const listContainer = document.querySelector(".results-list");
    const paginationContainer = document.querySelector(".pagination-container");

    if (!listContainer || !paginationContainer) return;

    listContainer.innerHTML = '<div class="loading-state">로딩 중...</div>';
    paginationContainer.innerHTML = '';

    try {
        // 'APPROVED', 'COMPLETED' 상태의 상담을 모두 가져오도록 API에 요청합니다.
        const url = `/api/counselor/results?page=${page}&size=10&period=${period}&type=${type}&statuses=APPROVED,COMPLETED`;
        
        const response = await fetch(url);
        if (!response.ok) throw new Error("상담 목록을 불러오는 데 실패했습니다.");
        
        const pagedData = await response.json();
        const resultsList = pagedData.content;
        
        listContainer.innerHTML = '';
        if (resultsList.length === 0) {
            listContainer.innerHTML = '<div class="empty-state"><p>조회된 상담 내역이 없습니다.</p></div>';
            return;
        }

        resultsList.forEach(item => {
            const itemHtml = createResultItemHtml(item);
            listContainer.insertAdjacentHTML('beforeend', itemHtml);
        });

        renderPagination(pagedData, paginationContainer);

    } catch (error) {
        console.error("Error fetching results:", error);
        listContainer.innerHTML = `<div class="empty-state error"><p>${error.message}</p></div>`;
    }
}

/**
 * 상담 결과 목록의 각 아이템 HTML을 생성합니다.
 * 상태(status)에 따라 '결과 작성' 또는 '결과 수정' 버튼을 다르게 보여줍니다.
 */
function createResultItemHtml(item) {
    const counselDate = new Date(item.applyDateTime); // 날짜는 신청일 기준으로 통일
    const dateText = `${counselDate.getFullYear()}.${String(counselDate.getMonth() + 1).padStart(2, '0')}.${String(counselDate.getDate()).padStart(2, '0')} (${'일월화수목금토'[counselDate.getDay()]}) ${String(counselDate.getHours()).padStart(2, '0')}:${String(counselDate.getMinutes()).padStart(2, '0')}`;

    let actionButtons = '';
    // 상담 상태가 '예약확정(APPROVED)'이면 '결과 작성' 버튼 표시
    if (item.status === 'APPROVED') {
        actionButtons = `<button class="btn btn-primary btn-sm" onclick="openModalForWrite(${item.applyId})"><i class="fas fa-edit"></i> 결과 작성</button>`;
    } 
    // 상담 상태가 '완료(COMPLETED)'이면 '결과 수정' 및 'PDF' 버튼 표시
    else if (item.status === 'COMPLETED') {
        actionButtons = `<button class="btn btn-primary btn-sm" onclick="openModalForEdit(${item.resultId})"><i class="fas fa-edit"></i> 결과 수정</button>`;
    }
    
    // 평점(satisfactionScore)이 있을 경우 별 아이콘으로 표시 (없을 경우 빈 문자열)
    const ratingHtml = item.satisfactionScore ? `
        <div class="result-rating">
            <span>평점: ${item.satisfactionScore.toFixed(1)}</span>
            <div class="stars">${'★'.repeat(Math.round(item.satisfactionScore))}${'☆'.repeat(5 - Math.round(item.satisfactionScore))}</div>
        </div>
    ` : '';

    return `
        <div class="result-item">
            <div class="result-header">
                <div class="result-date">${dateText}</div>
                ${ratingHtml}
            </div>
            <div class="result-details">
                <div class="student-info">
                    <h4>${item.studentName} (${item.studentId})</h4>
                    <p>${item.studentMajor} | ${item.counselingType}</p>
                </div>
                <div class="result-summary">
                    <h5>상담 요약</h5>
                    <p>${item.resultContent || '아직 결과가 작성되지 않았습니다.'}</p>
                </div>
            </div>
            <div class="result-actions">${actionButtons}</div>
        </div>
    `;
}

/**
 * '결과 작성'을 위해 빈 모달을 엽니다.
 * @param {number} applyId - 상담 신청 ID
 */
function openModalForWrite(applyId) {
    document.getElementById('resultModalTitle').textContent = '상담 결과 작성';
    
    // 숨겨진 input 값 설정
    document.getElementById('resultApplyId').value = applyId;
    document.getElementById('resultId').value = ''; // 작성 모드이므로 resultId는 비움

    // 텍스트 영역 비우기
    document.getElementById('resultContent').value = '';
    
    const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));
    resultModal.show();
}

/**
 * '결과 수정'을 위해 기존 데이터를 채운 모달을 엽니다.
 * @param {number} resultId - 상담 결과 ID
 */
async function openModalForEdit(resultId) {
    document.getElementById('resultModalTitle').textContent = '상담 결과 수정';
    
    try {
        // [API 호출] 기존 결과 데이터 가져오기
        const response = await fetch(`/api/counselor/results/${resultId}`);
        if (!response.ok) throw new Error('기존 상담 내용을 불러오는 데 실패했습니다.');
        
        const data = await response.json(); // 여기서는 상세 데이터 DTO를 받아옴

        // 숨겨진 input 값 설정
        document.getElementById('resultApplyId').value = data.applyId;
        document.getElementById('resultId').value = data.resultId;
        
        // 텍스트 영역에 기존 내용 채우기
        document.getElementById('resultContent').value = data.resultContent;

        const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));
        resultModal.show();
        
    } catch (error) {
        console.error("Error fetching result for editing:", error);
        alert(error.message);
    }
}

/**
 * [API 호출] 모달에서 '결과 저장' 버튼 클릭 시 실행됩니다.
 * 작성/수정을 구분하여 다른 API를 호출합니다.
 */
async function saveResult() {
    const resultId = document.getElementById('resultId').value;
    const applyId = document.getElementById('resultApplyId').value;
    const content = document.getElementById('resultContent').value;
    const isEditing = !!resultId; // resultId가 있으면 true(수정 모드)

    if (!content.trim()) {
        alert('상담 내용을 입력해주세요.');
        return;
    }

    const url = isEditing ? `/api/counselor/results/${resultId}` : '/api/counselor/results';
    const method = isEditing ? 'PUT' : 'POST';

    const bodyData = {
        applyId: applyId,
        counselingContent: content
    };

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bodyData)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => null);
            throw new Error(errorData?.message || '결과 저장에 실패했습니다.');
        }

        alert('성공적으로 저장되었습니다.');
        
        // 부트스트랩 모달 인스턴스를 찾아 닫기
        const resultModalEl = document.getElementById('resultModal');
        const modalInstance = bootstrap.Modal.getInstance(resultModalEl);
        modalInstance.hide();
        
        // 목록 새로고침
        fetchAndRenderResults(currentPage);

    } catch (error) {
        console.error("Error saving result:", error);
        alert(error.message);
    }
}


// --- 페이징 관련 함수들 ---
// (counsel-history.js의 renderPagination, createPageItem 함수와 동일한 로직)

function renderPagination(pagedData, container) {
    container.innerHTML = '';
    if (!pagedData || pagedData.totalPages <= 1) return;

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
            fetchAndRenderResults(page);
        }
    });
    
    li.appendChild(a);
    return li;
}