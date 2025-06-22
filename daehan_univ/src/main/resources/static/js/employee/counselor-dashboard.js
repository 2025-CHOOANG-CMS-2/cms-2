//**
 /* counselor-dashboard.js
 * 상담사 대시보드 페이지의 동적 기능을 처리합니다.
 */

// 전역 변수로 현재 캘린더가 보여주는 날짜와, 클릭된 날짜를 관리합니다.
const viewDate = new Date();
let currentSelectedDate = null;

/**
 * 페이지 로딩이 완료되면 모든 기능을 초기화합니다.
 */
document.addEventListener("DOMContentLoaded", () => {
    initTabs();
    initViewCalendar();
    // TODO: '기본 근무시간 설정' 탭의 기능 초기화 로직도 여기에 추가해야 합니다.
});

/**
 * '일정 및 예약 관리' / '기본 근무시간 설정' 탭 전환 기능을 초기화합니다.
 */
function initTabs() {
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const tabId = this.dataset.tab;
            document.querySelectorAll(".tab-btn, .tab-content").forEach(el => el.classList.remove("active"));
            document.querySelector(`[data-tab="${tabId}"]`).classList.add("active");
            document.getElementById(tabId).classList.add("active");
        });
    });
}

/**
 * '예약 현황 캘린더'의 월 이동 버튼과 초기 렌더링을 담당합니다.
 */
function initViewCalendar() {
    document.getElementById("prevMonth").addEventListener("click", () => {
        viewDate.setMonth(viewDate.getMonth() - 1);
        fetchAndRenderCalendar();
    });
    document.getElementById("nextMonth").addEventListener("click", () => {
        viewDate.setMonth(viewDate.getMonth() + 1);
        fetchAndRenderCalendar();
    });
    fetchAndRenderCalendar(); // 페이지 로딩 시 첫 캘린더 그리기
}

/**
 * [API 호출] 백엔드에서 해당 월의 예약 현황 데이터를 가져와 캘린더에 표시합니다.
 */
async function fetchAndRenderCalendar() {
    console.log("--- fetchAndRenderCalendar 함수 시작 ---");

    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1;
    const calendarGrid = document.getElementById("calendarGridForView");
    const monthDisplay = document.getElementById("currentMonth");

    // 캘린더를 다시 그리기 전에, 월 표시를 먼저 업데이트합니다.
    monthDisplay.textContent = `${year}년 ${month}월`;
    calendarGrid.innerHTML = '<div class="loading-state" style="grid-column: span 7;">달력 로딩 중...</div>';

    try {
        // TODO: 임시 ID '2025110002'는 실제 로그인한 상담사 ID로 교체해야 합니다.
        const url = `/api/counselor/dashboard/reservations/monthly-overview?year=${year}&month=${month}&counselorId=2025110002`;
        
        console.log("1. API 요청 주소:", url); // <<-- 디버깅 로그 1
        
        const response = await fetch(url);
        
        console.log("2. 백엔드 API 응답 수신:", response); // <<-- 디버깅 로그 2
        
        if (!response.ok) {
            throw new Error(`캘린더 정보 로딩 실패 (HTTP 상태: ${response.status})`);
        }
        
        const monthlyStatusList = await response.json();
        
        console.log("3. JSON으로 변환된 실제 데이터:", monthlyStatusList); // <<-- 디버깅 로그 3
        
        // API 결과를 UI에 그리기 편한 형태로 가공합니다.
        const monthlyData = {};
        monthlyStatusList.forEach(item => {
            const date = item.date;
            const status = item.status.toLowerCase(); 
            if (!monthlyData[date]) {
                monthlyData[date] = new Set();
            }
            monthlyData[date].add(status);
        });

        console.log("4. UI 렌더링을 위해 가공된 데이터 (Map 객체):", monthlyData); // <<-- 디버깅 로그 4

        // 실제 데이터로 캘린더 UI를 그립니다.
        renderCalendarUI(year, month - 1, monthlyData);

        console.log("5. 캘린더 UI 렌더링 함수 호출 완료"); // <<-- 디버깅 로그 5

    } catch (error) {
        console.error("fetchAndRenderCalendar에서 에러 발생:", error);
        calendarGrid.innerHTML = '<div class="empty-state error" style="grid-column: span 7;">정보를 불러올 수 없습니다.</div>';
    } finally {
        console.log("--- fetchAndRenderCalendar 함수 종료 ---");
    }
}

/**
 * 캘린더 UI를 그리고, API로 받은 데이터를 바탕으로 날짜에 상태를 표시합니다.
 */
function renderCalendarUI(year, month, monthlyData) { // month는 0~11
    const calendarGrid = document.getElementById("calendarGridForView");
    const monthDisplay = document.getElementById("currentMonth");
    const weekdaysContainer = document.querySelector(".calendar-weekdays");

    if (!calendarGrid || !monthDisplay || !weekdaysContainer) return;

    monthDisplay.textContent = `${year}년 ${month + 1}월`;
    
    calendarGrid.innerHTML = "";
    weekdaysContainer.innerHTML = "";
    ["일", "월", "화", "수", "목", "금", "토"].forEach(d => {
        weekdaysContainer.insertAdjacentHTML('beforeend', `<div>${d}</div>`);
    });

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) {
        calendarGrid.insertAdjacentHTML('beforeend', '<div class="calendar-day empty"></div>');
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement("div");
        dayCell.className = "calendar-day";
        dayCell.textContent = day;
        const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        
        // [수정] 우선순위에 따라 하나의 상태 클래스만 추가하도록 변경
        if (monthlyData && monthlyData[dateStr]) {
            const statuses = monthlyData[dateStr]; // statuses는 Set 객체입니다.
            
            if (statuses.has('pending')) {
                dayCell.classList.add('has-pending');
            } else if (statuses.has('approved')) {
                dayCell.classList.add('has-approved');
            } else if (statuses.has('completed')) {
                // '완료' 상태도 '확정'과 같은 색으로 표시하거나 다른 색 지정 가능
                dayCell.classList.add('has-approved'); 
            } else if (statuses.has('exception')) {
                dayCell.classList.add('has-exception');
            }
        }
        
        dayCell.addEventListener("click", () => fetchAndRenderAppointments(dateStr, dayCell));
        calendarGrid.appendChild(dayCell);
    }
}

/**
 * [API 호출] 날짜 클릭 시, 해당 날짜의 상세 예약 목록을 가져와 오른쪽에 표시합니다.
 */
async function fetchAndRenderAppointments(dateStr, clickedEl) {
    if (clickedEl) {
        document.querySelectorAll("#calendarGridForView .calendar-day.selected").forEach(d => d.classList.remove("selected"));
        clickedEl.classList.add("selected");
        currentSelectedDate = dateStr;
    }

    const listEl = document.getElementById("dateAppointmentList");
    const container = document.getElementById("selectedDateAppointments");
    const noSel = document.getElementById("noDateSelected");
    
    listEl.innerHTML = '<div class="loading-state">로딩 중...</div>';
    container.style.display = "block";
    noSel.style.display = "none";
    
    try {
        const response = await fetch(`/api/counselor/dashboard/reservations?date=${dateStr}`);
        if (!response.ok) throw new Error('예약 목록 조회에 실패했습니다.');
        const appointments = await response.json();
        
        listEl.innerHTML = "";
        if (appointments.length === 0) {
            listEl.innerHTML = '<div class="empty-state"><i class="fas fa-box-open"></i><p>예약된 상담이 없습니다.</p></div>';
            return;
        }

        appointments.forEach(app => {
            const itemHtml = createAppointmentItemHtml(app);
            listEl.insertAdjacentHTML('beforeend', itemHtml);
        });

        // 새로 생성된 승인/거부 버튼에 이벤트 리스너를 다시 연결
        bindActionButtons();

    } catch (error) {
        console.error(error);
        listEl.innerHTML = `<div class="empty-state error"><i class="fas fa-exclamation-triangle"></i><p>${error.message}</p></div>`;
    }
}

/**
 * 예약 목록 아이템의 HTML을 생성합니다.
 */
function createAppointmentItemHtml(item) {
    // DB의 상태 코드(PENDING 등)를 화면에 보여줄 한글 텍스트로 변환합니다.
    const statusMap = { 
        'PENDING': '승인대기', 
        'APPROVED': '예약확정', 
        'COMPLETED': '상담완료', 
        'REJECTED': '거부됨' 
    };
    const statusClass = item.status ? item.status.toLowerCase() : 'unknown';
    
    // [수정] 상태에 따라 다른 버튼들이 보이도록 로직을 추가합니다.
    let actionButtons = '';
    if (item.status === 'PENDING') {
        actionButtons = `
            <button class="btn btn-success btn-sm approve-btn" data-id="${item.applyId}" title="승인">
                <i class="fas fa-check"></i> 승인
            </button>
            <button class="btn btn-danger btn-sm reject-btn" data-id="${item.applyId}" title="거부">
                <i class="fas fa-times"></i> 거부
            </button>
        `;
    } else if (item.status === 'APPROVED') {
        // 예약이 확정되면 '결과 작성' 버튼이 보입니다.
        actionButtons = `
            <button class="btn btn-primary btn-sm write-result-btn" data-id="${item.applyId}" title="결과 작성">
                <i class="fas fa-edit"></i> 결과 작성
            </button>
        `;
    } else if (item.status === 'COMPLETED') {
        // 상담이 완료되면 '결과 보기' 버튼이 보입니다.
        actionButtons = `
             <button class="btn btn-outline-secondary btn-sm view-result-btn" data-id="${item.applyId}" title="결과 보기">
                <i class="fas fa-eye"></i> 결과 보기
             </button>
        `;
    }

    // 최종적으로 생성될 HTML 구조
    return `
        <div class="appointment-item status-${statusClass}">
            <div class="appointment-header">
                <div><i class="fas fa-clock"></i> ${new Date(item.applyDateTime).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })}</div>
                <div class="appointment-status">${statusMap[item.status] || '알 수 없음'}</div>
            </div>
            <div class="student-info">
                <h5><i class="fas fa-user-graduate"></i> ${item.studentName} (${item.studentId})</h5>
                <p class="text-sm">${item.content}</p>
            </div>
            <div class="appointment-actions">${actionButtons}</div>
        </div>
    `;
}

/**
 * 동적으로 생성된 승인/거부 버튼에 클릭 이벤트를 연결합니다.
 */
function bindActionButtons() {
    document.querySelectorAll('.approve-btn').forEach(btn => {
        btn.addEventListener('click', () => handleApproval(btn.dataset.id, 'approve'));
    });
    document.querySelectorAll('.reject-btn').forEach(btn => {
        btn.addEventListener('click', () => handleApproval(btn.dataset.id, 'reject'));
    });
	document.querySelectorAll('.write-result-btn').forEach(btn => {
	       btn.addEventListener('click', () => openResultModal(btn.dataset.id));
    });
}

/**
 * [API 호출] 승인 또는 거부 API를 호출합니다.
 */
async function handleApproval(applyId, action) {
    const actionText = action === 'approve' ? '승인' : '거부';
    if (!confirm(`이 예약을 ${actionText}하시겠습니까?`)) return;

    try {
        const response = await fetch(`/api/counselor/dashboard/reservations/${applyId}/${action}`, { method: 'PATCH' });
        if (!response.ok) throw new Error(`${actionText} 처리에 실패했습니다.`);
        
        alert(`${actionText} 처리되었습니다.`);
        // 현재 선택된 날짜의 예약 목록을 다시 불러와서 화면을 즉시 갱신
        if(currentSelectedDate) fetchAndRenderAppointments(currentSelectedDate, null);
        
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

// TODO: '기본 근무시간 설정' 탭의 JS 로직 구현 필요

// =================================================================================
// 탭 2: 기본 근무시간 설정 관련 함수 (기존 counsel-default-schedule.js 내용)
// =================================================================================

function bindExceptionOptionListeners() {
    document.querySelectorAll('input[name="exceptionType"]').forEach(radio => {
        radio.addEventListener("change", () => {
            const timeSlotsContainer = document.getElementById("exceptionTimeSlots");
            timeSlotsContainer.style.display = (radio.value === "unavailable" ? "none" : "block");
        });
    });
}

function selectExceptionDate(dateStr, el) {
    document.querySelectorAll("#calendarGridForException .selected").forEach(d => d.classList.remove("selected"));
    el.classList.add("selected");

    const txt = document.getElementById("exceptionDateText");
    txt.textContent = `선택된 날짜: ${dateStr}`;
    document.getElementById("exceptionOptions").style.display = "block";

    generateExceptionTimeSlots(dateStr);
}

function generateExceptionTimeSlots(dateStr) {
    const grid = document.getElementById("exceptionTimeSlotGrid");
    grid.innerHTML = "";
    // TODO: 실제로는 해당 날짜의 기본 근무 시간을 서버에서 가져와야 함
    for(let h = 9; h < 18; h++){
        if(h === 12) continue; // 점심시간 제외
        const slot = document.createElement("div");
        slot.className = "time-slot";
        slot.dataset.time = `${String(h).padStart(2, "0")}:00`;
        slot.textContent = slot.dataset.time;
        slot.addEventListener("click", () => slot.classList.toggle("selected"));
        grid.appendChild(slot);
    }
}

function openResultModal(applyId) {
    // 1. 숨겨진 input 태그에, 어떤 예약에 대한 결과인지 ID를 설정합니다.
    document.getElementById('resultApplyId').value = applyId;
    
    // 2. 이전에 작성했을 수도 있는 내용을 지우기 위해 텍스트 영역을 비웁니다.
    document.getElementById('resultContent').value = '';
    
    // 3. 숨겨져 있던 모달 창을 보여줍니다.
    document.getElementById('resultModal').style.display = 'flex';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function saveDefaultSchedule() {
    // TODO: 요일별 근무시간 설정 저장 API 호출
    alert("기본 일정이 저장되었습니다. (구현 필요)");
}

async function saveResult() {
    // 1. 모달 안의 숨겨진 input과 textarea에서 값을 가져옵니다.
    const applyId = document.getElementById('resultApplyId').value;
    const content = document.getElementById('resultContent').value;

    // 2. 프론트엔드 예외 처리: 내용이 비어있는지 확인합니다.
    if (!content.trim()) {
        alert('상담 내용을 입력해주세요.');
        return;
    }

    // 3. 백엔드로 보낼 데이터(DTO)를 생성합니다.
    const resultData = {
        applyId: applyId,
        counselingContent: content
        // satisfactionScore는 학생이 평가하므로 여기서는 보내지 않습니다.
    };

    console.log("서버로 전송할 결과 데이터:", resultData);

    // 4. fetch API로 백엔드에 결과 저장(POST) 요청을 보냅니다.
    try {
        const response = await fetch('/api/counselor/dashboard/results', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(resultData)
        });

        // 5. 서버 응답에 따른 성공/실패 처리
        if (!response.ok) {
            // 서버에서 보낸 에러 메시지가 있다면 사용하고, 없다면 기본 메시지를 사용합니다.
            const errorData = await response.json().catch(() => null);
            const errorMessage = errorData?.message || '결과 저장에 실패했습니다.';
            throw new Error(errorMessage);
        }

        alert('상담 결과가 성공적으로 저장되었습니다.');
        closeModal('resultModal'); // 성공 시 모달 닫기
        
        // 현재 보고있는 날짜의 예약 목록을 새로고침하여 상태 변경('COMPLETED')을 반영합니다.
        if (currentSelectedDate) {
            fetchAndRenderAppointments(currentSelectedDate, null);
        }

    } catch (error) {
        console.error('결과 저장 중 에러:', error);
        alert(error.message);
    }
}

function saveException() {
    const selectedDay = document.querySelector("#calendarGridForException .selected");
    if (!selectedDay) { alert("예외를 설정할 날짜를 캘린더에서 선택해주세요."); return; }
    // TODO: 예외 설정 저장 API 호출
    alert("예외 설정이 저장되었습니다. (구현 필요)");
}

function clearException() {
    const selectedDay = document.querySelector("#calendarGridForException .selected");
    if (!selectedDay) { alert("삭제할 예외 날짜를 캘린더에서 선택해주세요."); return; }
    // TODO: 예외 설정 삭제 API 호출
    alert("예외 설정이 삭제되었습니다. (구현 필요)");
}