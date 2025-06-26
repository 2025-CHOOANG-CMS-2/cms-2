/**
 * counsel-book.js
 * 학생의 상담 예약 페이지의 모든 동적 기능을 처리합니다.
 */

// --- 1. 전역 변수 선언 ---

// 페이지 로딩 시 URL 파라미터를 제일 먼저 확인
const urlParams = new URLSearchParams(window.location.search);
const isModifyMode = urlParams.get('mode') === 'modify';
const originalApplyId = urlParams.get('applyId');

// 선택된 상담 정보 및 캘린더 상태 관리
let selectedInfo = { date: null, time: null, counselor: null };
const viewDate = new Date(); // 캘린더가 보여줄 현재 날짜 (월 이동 시 변경됨)
let availableDataCache = {}; // API로부터 받은 월간 데이터를 캐싱할 객체

// --- 2. 페이지 로딩 완료 후 실행 ---

document.addEventListener("DOMContentLoaded", () => {
    initializeEventListeners(); // 모든 이벤트 리스너 등록
    handleFilterChange();       // 페이지 첫 로딩 시 상담사 목록 및 캘린더 초기화
    
    // '변경 모드'일 경우, 사용자에게 안내창 표시
    if (isModifyMode && originalApplyId) {
        displayModifyModeBanner(originalApplyId);
    }
});

// --- 3. 이벤트 리스너 등록 함수 ---

/**
 * 페이지 내 모든 UI 요소에 이벤트 리스너를 한 번에 등록합니다.
 */
function initializeEventListeners() {
    document.getElementById('counselingTypeFilter').addEventListener('change', handleFilterChange);
    document.getElementById('counselorFilter').addEventListener('change', fetchAndRenderCalendar);
    document.getElementById('prevMonth').addEventListener('click', () => { viewDate.setMonth(viewDate.getMonth() - 1); fetchAndRenderCalendar(); });
    document.getElementById('nextMonth').addEventListener('click', () => { viewDate.setMonth(viewDate.getMonth() + 1); fetchAndRenderCalendar(); });
    document.getElementById('bookingForm').addEventListener('submit', handleBookingSubmit); // form 제출 이벤트
    document.querySelector('input[name="phone"]').addEventListener('input', handlePhoneInput); // 연락처 자동 하이픈
}

// --- 4. 핵심 기능 함수들 ---

/**
 * [최종] '상담 예약하기' 폼 제출 처리 함수 (신규/변경 모두 처리)
 */
async function handleBookingSubmit(event) {
    event.preventDefault(); // 폼 기본 제출(새로고침) 방지

    // 필수 정보 선택 유효성 검사
    if (!selectedInfo.date || !selectedInfo.time || !selectedInfo.counselor) {
        alert("상담 유형, 상담사, 날짜와 시간을 모두 선택해주세요.");
        return;
    }

    const form = event.target;
    // HTML 'required' 속성 유효성 검사
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    // 서버로 보낼 데이터(DTO) 생성
    const reservationData = {
        // stdNo는 서버에서 로그인 세션을 통해 처리하는 것이 더 안전합니다.
        stdNo: '2025004001', // TODO: 실제 로그인한 학생의 학번으로 교체 필요
        emplNo:           selectedInfo.counselor.emplNo,
        counselingType:   document.getElementById('counselingTypeFilter').value,
        applyDateTime:    `${selectedInfo.date}T${selectedInfo.time}:00`,
        counselingMethod: form.querySelector('[name="counselingMethod"]').value,
        content:          form.querySelector('[name="content"]').value,
        phone:            form.querySelector('[name="phone"]').value.replace(/-/g, ''),
        // [핵심] 변경 모드일 경우, 취소할 예약 ID를 DTO에 포함
        originalApplyId:  isModifyMode ? originalApplyId : null 
    };
    
    console.log('[handleBookingSubmit] API 전송 DTO:', reservationData);

    // Fetch API를 사용해 서버에 예약 생성/변경 요청
    try {
        const response = await fetch('/api/counseling/reservations', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(reservationData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error('예약 처리 실패: ' + (errorText || '서버 응답 없음'));
        }
        
        alert('예약이 성공적으로 완료되었습니다.');
        window.location.href = '/counsel_history'; // 성공 후 상담 내역 페이지로 이동

    } catch (err) {
        console.error('[handleBookingSubmit] POST 오류:', err);
        alert(err.message);
    }
}

/**
 * 상담 유형 필터 변경 시 상담사 목록을 다시 불러오는 함수
 */
async function handleFilterChange() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorSelect = document.getElementById('counselorFilter');
    
    // 상담사 목록 초기화
    counselorSelect.innerHTML = '<option value="all">전체 상담사</option>';

    if (type) { // 상담 유형이 선택되었을 경우
        try {
            const response = await fetch(`/api/counseling/counselors?type=${type}`);
            if (!response.ok) throw new Error('상담사 목록 로딩 실패');
            
            const counselors = await response.json();
            counselors.forEach(c => {
                counselorSelect.insertAdjacentHTML('beforeend', `<option value="${c.emplNo}">${c.emplNm}</option>`);
            });
        } catch (error) {
            console.error("상담사 목록 로딩 실패:", error);
        }
    }
    
    // 상담사 목록 변경 후, 캘린더를 새로고침
    fetchAndRenderCalendar();
}


/**
 * 백엔드에서 예약 가능 데이터를 가져와 캘린더 UI를 그리는 함수
 */
async function fetchAndRenderCalendar() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorId = document.getElementById('counselorFilter').value;
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1;

    try {
        const url = `/api/counseling/available-slots?year=${year}&month=${month}&type=${type}&counselorId=${counselorId}`;
        const response = await fetch(url);
        if (!response.ok) throw new Error('예약 가능 시간 로딩 실패');
        
        availableDataCache = await response.json(); // 월간 데이터를 캐시에 저장
        
        renderCalendar(year, month - 1, availableDataCache); 
        clearTimeSlotsAndSelection();

    } catch (error) {
        console.error("fetchAndRenderCalendar 에러:", error);
        document.getElementById("calendarGrid").innerHTML = '<div class="empty-state-sm" style="grid-column: span 7;">예약 정보를 불러올 수 없습니다.</div>';
    }
}

// --- 5. UI 렌더링 및 헬퍼 함수들 ---

/**
 * 예약 변경 모드 안내창을 화면에 표시하는 함수
 */
function displayModifyModeBanner(applyId) {
    const alertBox = document.createElement('div');
    alertBox.className = 'alert alert-info mt-3'; 
    alertBox.setAttribute('role', 'alert');
    
    alertBox.innerHTML = `
        <strong><i class="fas fa-info-circle"></i> 예약 변경 모드입니다.</strong>
        <p class="mb-1 mt-2">새로운 날짜와 시간을 선택하고 예약을 완료하면, 기존 예약(ID: ${applyId})은 자동으로 취소됩니다.</p>
        <a href="/counsel_history" class="alert-link">변경을 원치 않으시면 여기를 눌러 예약 내역으로 돌아가세요.</a>
    `;
    
    const filterSection = document.querySelector('.filter-section');
    if (filterSection) {
        filterSection.parentNode.insertBefore(alertBox, filterSection);
    }
}

/**
 * 캘린더 UI를 그리고 날짜를 채우는 함수
 */
function renderCalendar(year, month, availableData) {
    const calendarGrid = document.getElementById("calendarGrid");
    document.getElementById("currentMonth").textContent = `${year}년 ${month + 1}월`;
    calendarGrid.innerHTML = "";

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = 0; i < firstDay; i++) {
        calendarGrid.insertAdjacentHTML('beforeend', '<div class="calendar-day empty"></div>');
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement("div");
        dayCell.className = "calendar-day";
        dayCell.textContent = day;
        
        const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        const currentDate = new Date(dateStr);

        if (currentDate < today) {
            dayCell.classList.add("unavailable");
        } else if (availableData && availableData[dateStr] && availableData[dateStr].length > 0) {
            dayCell.classList.add("available");
            dayCell.addEventListener("click", () => handleDateClick(dateStr, dayCell));
        } else {
            dayCell.classList.add("unavailable");
        }
        calendarGrid.appendChild(dayCell);
    }
}

/**
 * 캘린더에서 특정 날짜를 클릭했을 때 시간 슬롯을 표시하는 함수
 */
function handleDateClick(dateStr, dayCell) {
    const counselorFilter = document.getElementById('counselorFilter');
    if (counselorFilter.value === 'all') {
        alert('특정 상담사를 먼저 선택해주세요.');
        return;
    }

    selectedInfo.date = dateStr;
    selectedInfo.time = null;

    document.querySelectorAll("#calendarGrid .calendar-day.selected").forEach(d => d.classList.remove("selected"));
    dayCell.classList.add("selected");

    const timeSlotsData = availableDataCache[dateStr] || [];
    const slotsContainer = document.getElementById("timeSlots");
    slotsContainer.innerHTML = ""; 

    if (timeSlotsData.length === 0) {
        slotsContainer.innerHTML = '<div class="empty-state-sm">선택 가능한 시간이 없습니다.</div>';
        updateSelectedInfo();
        return;
    }
    
    const selectedCounselorId = counselorFilter.value;
    
    // 선택된 상담사의 시간만 필터링
    const filteredSlots = timeSlotsData.filter(slot => slot.counselorEmplNo === selectedCounselorId);

    if (filteredSlots.length === 0) {
        slotsContainer.innerHTML = '<div class="empty-state-sm">해당 상담사의 예약 가능 시간이 없습니다.</div>';
        updateSelectedInfo();
        return;
    }

    filteredSlots.forEach(slotData => {
        const slotEl = document.createElement("div");
        slotEl.className = "time-slot";
        slotEl.dataset.time = slotData.time;
        slotEl.textContent = slotData.time;
        
        slotEl.addEventListener("click", () => {
            selectedInfo.time = slotData.time;
            selectedInfo.counselor = { emplNo: slotData.counselorEmplNo, name: slotData.counselorName };

            document.querySelectorAll("#timeSlots .time-slot.selected").forEach(s => s.classList.remove("selected"));
            slotEl.classList.add("selected");
            updateSelectedInfo();
        });
        slotsContainer.appendChild(slotEl);
    });
    
    updateSelectedInfo();
}

/**
 * 오른쪽 폼의 '선택 정보' 텍스트를 업데이트하는 함수
 */
function updateSelectedInfo() {
    const infoText = document.getElementById("selectedInfoText");
    if (selectedInfo.date && selectedInfo.time && selectedInfo.counselor) {
        infoText.textContent = `선택된 예약: ${selectedInfo.counselor.name} 상담사, ${selectedInfo.date} ${selectedInfo.time}`;
        infoText.style.color = '#333';
    } else if(selectedInfo.date) {
        infoText.textContent = '시간을 선택해주세요.';
        infoText.style.color = '#999';
    } else {
        infoText.textContent = '상담 날짜와 시간을 선택해주세요.';
        infoText.style.color = '#999';
    }
}

/**
 * 시간 선택 및 선택 정보를 초기화하는 함수
 */
function clearTimeSlotsAndSelection() {
    document.getElementById("timeSlots").innerHTML = '<div class="empty-state-sm">날짜를 먼저 선택해주세요.</div>';
    selectedInfo = { date: null, time: null, counselor: null };
    updateSelectedInfo();
}

/**
 * 연락처 입력 시 자동으로 하이픈(-)을 추가하는 함수
 */
function handlePhoneInput(event) {
    const phone = event.target.value.replace(/\D/g, ''); // 숫자가 아닌 문자 모두 제거
    let formattedPhone = '';
    if (phone.length < 4) {
        formattedPhone = phone;
    } else if (phone.length < 8) {
        formattedPhone = `${phone.slice(0, 3)}-${phone.slice(3)}`;
    } else {
        formattedPhone = `${phone.slice(0, 3)}-${phone.slice(3, 7)}-${phone.slice(7, 11)}`;
    }
    event.target.value = formattedPhone;
}