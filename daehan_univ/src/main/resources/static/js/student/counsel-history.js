/**
 * counsel-history.js
 * 학생의 상담 조회/결과 페이지의 모든 동적 기능을 처리합니다.
 */
let currentPage = 0; // 현재 페이지 번호 (0부터 시작)
let currentResultId = null; // 만족도 평가를 위해 현재 선택된 상담 결과 ID
let currentRating = 0; // 현재 선택된 별점
let socket = null;
let currentRoomId = null;

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
	setupChatEventListeners(); 
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
    // '예약 현황' 탭일 때만 버튼 로직 처리
    if (activeTab === 'appointments') {
        const now = new Date();
        const counselTime = new Date(item.applyDateTime);

        if (item.status === 'APPROVED') {
            // [수정] '예약됨' 상태일 때, 시간에 따라 활성화/비활성화되는 채팅 버튼 추가
            //const isCounselTime = now >= counselTime;
			const isCounselTime = true;
            const disabledAttr = isCounselTime ? '' : 'disabled';
            const buttonClass = isCounselTime ? 'btn-primary' : 'btn-secondary';
            const buttonTitle = isCounselTime ? '클릭하여 상담 시작' : '아직 상담 시작 시간이 아닙니다.';
            
            actionButtons = `<button class="btn ${buttonClass} btn-sm" onclick="startChatSession('${item.applyId}')" ${disabledAttr} title="${buttonTitle}">
                                <i class="fas fa-comments"></i> 채팅 시작
                             </button>`;

        } else if (item.status === 'PENDING') {
            // [수정] '승인대기' 상태일 때만 예약 변경/취소 버튼 표시
            actionButtons = `<button class="btn btn-outline btn-sm" onclick="modifyBooking('${item.applyId}')"><i class="fas fa-edit"></i> 예약 변경</button>
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

async function cancelBooking(applyId) { // async 키워드는 그대로 사용
    if (!confirm("정말로 예약을 취소하시겠습니까?")) {
        return;
    }

    try {
        // [수정] 이미 존재하는 PATCH API 주소를 사용합니다.
        const response = await fetch(`/api/counseling/reservations/${applyId}/status`, {
            method: 'PATCH', // [수정] HTTP 메소드를 'PATCH'로 변경
            headers: {
                'Content-Type': 'application/json' // [추가] JSON 데이터를 보낸다고 명시
            },
            // [추가] 변경할 상태를 JSON 본문에 담아 전송합니다.
            body: JSON.stringify({ status: 'CANCELED' }) 
        });

        if (!response.ok) {
            // 서버에서 보낸 에러 메시지가 있다면 표시
            const errorText = await response.text();
            throw new Error(errorText || '예약 취소에 실패했습니다.');
        }

        alert('예약이 정상적으로 취소되었습니다.');
        
        // 목록을 새로고침하여 변경된 상태를 즉시 반영합니다.
        fetchAndRenderHistory(currentPage);

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

/**
 * 예약 변경 기능을 처리하는 함수
 * @param {string} applyId - 변경할 예약 ID
 */
function modifyBooking(applyId) {
    if (confirm("기존 예약을 취소하고 예약을 변경하시겠습니까?")) {
        // [수정] URL 파라미터 이름을 'mode=modify'와 'applyId'로 변경
        window.location.href = `/counsel_book?mode=modify&applyId=${applyId}`;
    }
}

/**
 * '채팅 시작' 버튼 클릭 시 호출되는 메인 함수
 * @param {string} applyId - 입장할 예약(채팅방) ID
 */
/**
 * '채팅 시작' 버튼 클릭 시 호출되는 메인 함수
 * @param {string} applyId - 입장할 예약(채팅방) ID
 */
function startChatSession(applyId) {
    currentRoomId = applyId;
    
    const chatModalEl = document.getElementById('chatModal');
    const chatModalInstance = new bootstrap.Modal(chatModalEl, {
        backdrop: 'static',
        keyboard: false
    });
    
    const messagesContainer = document.getElementById('chat-messages');
    messagesContainer.innerHTML = '<div class="text-center text-muted small p-2">서버에 연결 중입니다...</div>';
    
    chatModalInstance.show();

    if(socket) {
        socket.disconnect();
    }

    // Node.js 서버에 연결 (공인 IP 사용)
    socket = io('http://210.178.108.186:3001');

    // --- 소켓 이벤트 리스너 등록 ---

    // 1. 서버에 성공적으로 연결되었을 때
    socket.on('connect', () => {
        console.log('채팅 서버 연결 성공');
        messagesContainer.innerHTML = '';
        
        const currentUser = getUserInfoForChat();
        
        // 채팅방 참여 이벤트 전송
        socket.emit('joinRoom', { roomId: currentRoomId, user: currentUser });
    });

    // 2. [추가] 이전 대화 기록을 서버로부터 받았을 때
    socket.on('loadHistory', (messages) => {
        // 메시지 배열을 순회하며 화면에 추가합니다.
        messages.forEach(message => {
            addMessageToChat(message);
        });
        console.log(`이전 대화 ${messages.length}개를 화면에 표시했습니다.`);
    });

    // 3. 새로운 실시간 메시지를 받았을 때
    socket.on('receiveMessage', (message) => {
        addMessageToChat(message);
    });

    // 4. 연결이 끊겼을 때
    socket.on('disconnect', () => console.log('채팅 서버 연결 종료'));
    
    // 모달이 닫힐 때 소켓 연결을 해제하는 이벤트 리스너
    chatModalEl.addEventListener('hidden.bs.modal', () => {
        if(socket) socket.disconnect();
    }, { once: true });
}

/**
 * 메시지를 화면에 추가하는 함수 (좌/우 정렬 포함)
 * @param {object} message - { senderId, senderName, text }
 */
function addMessageToChat(message) {
    const messagesContainer = document.getElementById('chat-messages');
    const currentUserId = getUserInfoForChat().id; // 현재 사용자 ID 가져오기

    const messageRow = document.createElement('div');
    messageRow.classList.add('message-row');

    if (message.senderId === currentUserId) {
        messageRow.classList.add('self');
    } else {
        messageRow.classList.add('other');
    }

    if (message.senderId === 'system') {
        messageRow.classList.add('system');
        messageRow.innerHTML = `<div class="text-center text-muted small">${message.text}</div>`;
    } else {
        messageRow.innerHTML = `
            <div class="message-bubble">
                <strong>${message.senderName}</strong>
                <p class="mb-0" style="white-space: pre-wrap;">${message.text}</p>
            </div>
        `;
    }
    
    messagesContainer.appendChild(messageRow);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

/**
 * 메시지 전송 버튼/엔터키 이벤트에 연결될 함수
 */
function sendMessage() {
    const chatInput = document.getElementById('chat-input');
    const messageText = chatInput.value.trim();

    if (messageText && socket) {
        const currentUser = getUserInfoForChat();

        const messageData = {
            roomId: currentRoomId,
            senderId: currentUser.id,
            senderName: currentUser.name,
            text: messageText
        };
        
        socket.emit('sendMessage', messageData);
        addMessageToChat({ ...messageData, senderName: '나' }); // 내 화면에는 '나'로 표시
        chatInput.value = '';
        chatInput.focus();
    }
}

/**
 * 채팅 관련 UI 이벤트 리스너를 설정하는 함수
 */
function setupChatEventListeners() {
    document.getElementById('chat-send-btn').addEventListener('click', sendMessage);
    document.getElementById('chat-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

function getUserInfoForChat() {
    // TODO: 실제 로그인한 학생 정보로 대체해야 합니다.
    return {
        id: '2025004001',
        name: '학생'
    };
}