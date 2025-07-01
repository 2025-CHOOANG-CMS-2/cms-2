/**
 * counselor-dashboard.js
 * 상담사 대시보드 페이지의 동적 기능을 처리합니다.
 * 탭 1: 일정 및 예약 관리
 * 탭 2: 기본 근무시간 설정
 */

// 전역 변수: 각 캘린더가 보여주는 날짜를 독립적으로 관리합니다.
const viewDate = new Date();               // 예약 현황 캘린더용
const exceptionViewDate = new Date();      // 예외 설정 캘린더용
let currentSelectedDate = null;            // 예약 목록 조회를 위한 날짜 저장
let scheduleDataCache = null;              // API로부터 받은 스케줄 데이터를 캐싱
let socket = null;
let currentRoomId = null;

/**
 * 페이지 로딩이 완료되면 모든 기능을 초기화합니다.
 */
document.addEventListener("DOMContentLoaded", () => {
    initTabs();
    initViewCalendar();         // 탭 1: 일정 및 예약 관리 초기화
    initScheduleSettingsTab();  // 탭 2: 기본 근무시간 설정 초기화
    setupTimeValidationListeners(); // [신규] 시간 유효성 검사 이벤트 리스너 설정
	setupChatEventListeners();
});

/**
 * 탭 전환 기능을 초기화합니다.
 */
function initTabs() {
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const tabId = this.dataset.tab;
            document.querySelectorAll(".tab-btn, .tab-content").forEach(el => el.classList.remove("active"));
            this.classList.add("active");
            document.getElementById(tabId).classList.add("active");

            // '기본 근무시간 설정' 탭이 활성화될 때만 스케줄 데이터를 불러옵니다.
            if (tabId === 'work-hours-setting' && !scheduleDataCache) {
                fetchAndRenderSchedules();
            }
        });
    });
}

// =================================================================================
// 탭 1: 일정 및 예약 관리 관련 함수들
// =================================================================================

/**
 * '예약 현황 캘린더'의 월 이동 및 초기 렌더링을 담당합니다.
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
    fetchAndRenderCalendar(); // 첫 페이지 로딩 시 API 호출
}

/**
 * [API 호출] 백엔드에서 해당 월의 예약 현황 데이터를 가져와 캘린더에 표시합니다.
 */
async function fetchAndRenderCalendar() {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1;
    const calendarGrid = document.getElementById("calendarGridForView");
    const monthDisplay = document.getElementById("currentMonth");

    monthDisplay.textContent = `${year}년 ${month}월`;
    calendarGrid.innerHTML = '<div class="loading-state" style="grid-column: span 7;">달력 로딩 중...</div>';

    try {
        const url = `/api/counselor/dashboard/reservations/monthly-overview?year=${year}&month=${month}&counselorId=2025110002`;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`캘린더 정보 로딩 실패 (HTTP 상태: ${response.status})`);
        const monthlyStatusList = await response.json();

        // 상태별로 Set에 담아 Map 형태로 가공
        const monthlyData = {};
        monthlyStatusList.forEach(item => {
            const date = item.date;
            const status = item.status.toLowerCase();
            if (!monthlyData[date]) monthlyData[date] = new Set();
            monthlyData[date].add(status);
        });

        renderCalendarUI(year, viewDate.getMonth(), monthlyData);
    } catch (error) {
        console.error(error);
        calendarGrid.innerHTML = '<div class="empty-state error" style="grid-column: span 7;">정보를 불러올 수 없습니다.</div>';
    }
}

/**
 * 캘린더 UI를 그리고, API로 받은 데이터를 바탕으로 날짜에 상태를 표시합니다.
 */
function renderCalendarUI(year, month0Based, monthlyData) {
    const calendarGrid = document.getElementById("calendarGridForView");
    const monthDisplay = document.getElementById("currentMonth");
    const weekdaysContainer = document.querySelector(".calendar-weekdays");

    monthDisplay.textContent = `${year}년 ${month0Based + 1}월`;
    calendarGrid.innerHTML = '';
    weekdaysContainer.innerHTML = '';
    ["일","월","화","수","목","금","토"].forEach(d => {
        weekdaysContainer.insertAdjacentHTML('beforeend', `<div>${d}</div>`);
    });

    const firstDay = new Date(year, month0Based, 1).getDay();
    const daysInMonth = new Date(year, month0Based + 1, 0).getDate();

    // 빈칸 채우기
    for (let i = 0; i < firstDay; i++) {
        calendarGrid.insertAdjacentHTML('beforeend', '<div class="calendar-day empty"></div>');
    }

    // 날짜 셀 생성
    for (let day = 1; day <= daysInMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-day';
        cell.textContent = day;
        const dateStr = `${year}-${String(month0Based+1).padStart(2,'0')}-${String(day).padStart(2,'0')}`;

        if (monthlyData[dateStr]) {
            const s = monthlyData[dateStr];
            if (s.has('pending'))      cell.classList.add('has-pending');
            else if (s.has('approved')) cell.classList.add('has-approved');
            else if (s.has('completed'))cell.classList.add('has-approved');
            else if (s.has('exception'))cell.classList.add('has-exception');
        }

        cell.dataset.date = dateStr;
        cell.addEventListener('click', () => fetchAndRenderAppointments(dateStr, cell));
        calendarGrid.appendChild(cell);
    }
}

/**
 * [API 호출] 날짜 클릭 시, 해당 날짜의 상세 예약 목록을 가져와 오른쪽에 표시합니다.
 */
async function fetchAndRenderAppointments(dateStr, clickedEl) {
    // 선택 강조
    document.querySelectorAll('#calendarGridForView .calendar-day.selected').forEach(d => d.classList.remove('selected'));
    clickedEl.classList.add('selected');
    currentSelectedDate = dateStr;

    const listEl = document.getElementById('dateAppointmentList');
    listEl.innerHTML = '<div class="loading-state">로딩 중...</div>';
    document.getElementById('selectedDateAppointments').style.display = 'block';
    document.getElementById('noDateSelected').style.display = 'none';

    try {
        const resp = await fetch(`/api/counselor/dashboard/reservations?date=${dateStr}`);
        if (!resp.ok) throw new Error('예약 목록 조회에 실패했습니다.');
        const apps = await resp.json();

        listEl.innerHTML = '';
        if (apps.length === 0) {
            listEl.innerHTML = '<div class="empty-state"><i class="fas fa-box-open"></i><p>예약된 상담이 없습니다.</p></div>';
            return;
        }
        apps.forEach(app => listEl.insertAdjacentHTML('beforeend', createAppointmentItemHtml(app)));
        bindActionButtons();
    } catch (e) {
        console.error(e);
        listEl.innerHTML = `<div class="empty-state error"><i class="fas fa-exclamation-triangle"></i><p>${e.message}</p></div>`;
    }
}

/**
 * 예약 목록 아이템의 HTML을 생성합니다.
 */
function createAppointmentItemHtml(item) {
    // 'CANCELED'를 포함한 모든 상태에 대한 한글 이름을 정의합니다.
    const statusMap = { 'PENDING':'승인대기','APPROVED':'예약확정','COMPLETED':'상담완료','REJECTED':'거부됨', 'CANCELED':'예약취소' };
    
    // CSS 클래스를 위해 상태 코드를 소문자로 변환합니다.
    const cls = item.status ? item.status.toLowerCase() : 'unknown';
    
    // 버튼을 담을 변수를 빈 문자열로 초기화합니다.
    let buttons = '';
    
    // 상태가 'PENDING'일 경우에만 승인/거부 버튼을 생성합니다.
    if (item.status === 'PENDING') {
        buttons = `<button class="btn btn-success btn-sm approve-btn" data-id="${item.applyId}" title="승인"><i class="fas fa-check"></i></button>
                   <button class="btn btn-danger btn-sm reject-btn" data-id="${item.applyId}" title="거부"><i class="fas fa-times"></i></button>`;
    
    // 상태가 'APPROVED'일 경우에만 채팅 시작 버튼을 생성합니다.
    } else if (item.status === 'APPROVED') {
        const now = new Date();
        const counselTime = new Date(item.applyDateTime);
        //const isCounselTime = now >= counselTime; // 현재 시간이 상담 시간이거나 지났는지 확인
		const isCounselTime = true;
        
        // 상담 시간에 따라 버튼의 활성화/비활성화 상태를 결정합니다.
        const disabledAttr = isCounselTime ? '' : 'disabled';
        const buttonClass = isCounselTime ? 'btn-primary' : 'btn-secondary';
        const buttonTitle = isCounselTime ? '클릭하여 채팅 시작' : '상담 시작 시간 전입니다.';

        buttons = `<button class="btn ${buttonClass} btn-sm start-chat-btn" data-apply-id="${item.applyId}" ${disabledAttr} title="${buttonTitle}">
                     <i class="fas fa-comments"></i> 채팅 시작
                   </button>`;
    }
    
    // 'COMPLETED', 'REJECTED', 'CANCELED' 상태일 경우 'buttons' 변수는 빈 값으로 유지되어 아무 버튼도 표시되지 않습니다.
    
    // 최종적으로 아이템의 전체 HTML을 구성하여 반환합니다.
    return `
    <div class="appointment-item status-${cls}">
      <div class="appointment-header">
        <div><i class="fas fa-clock"></i> ${new Date(item.applyDateTime).toLocaleTimeString('ko-KR',{hour:'2-digit',minute:'2-digit',hour12:false})}</div>
        <div class="appointment-status">${statusMap[item.status] || '알 수 없음'}</div>
      </div>
      <div class="student-info">
        <h5><i class="fas fa-user-graduate"></i> ${item.studentName} (${item.studentId})</h5>
        <p class="text-sm">${item.content}</p>
      </div>
      <div class="appointment-actions">${buttons}</div>
    </div>`;
}

/**
 * 동적 생성된 승인/거부 버튼에 클릭 이벤트를 연결합니다.
 */
function bindActionButtons() {
    // 기존 승인/거부 버튼
    document.querySelectorAll('.approve-btn').forEach(btn => {
        btn.onclick = () => handleApproval(btn.dataset.id, 'approve');
    });
    document.querySelectorAll('.reject-btn').forEach(btn => {
        btn.onclick = () => handleApproval(btn.dataset.id, 'reject');
    });

    // [추가] 채팅 시작 버튼 이벤트 리스너
    document.querySelectorAll('.start-chat-btn').forEach(btn => {
        btn.onclick = (e) => {
            const applyId = e.currentTarget.dataset.applyId;
            // 채팅방으로 이동하거나 채팅 모달을 띄우는 함수 호출
            startChatSession(applyId); 
        };
    });
}

/**
 * [API 호출] 승인 또는 거부 API를 호출합니다.
 */
async function handleApproval(applyId, action) {
    const text = action==='approve'?'승인':'거부';
    if (!confirm(`이 예약을 ${text}하시겠습니까?`)) return;
    try {
        const resp = await fetch(`/api/counselor/dashboard/reservations/${applyId}/${action}`, {method:'PATCH'});
        if (!resp.ok) throw new Error(`${text} 처리에 실패했습니다.`);
        alert(`${text} 처리되었습니다.`);
        if (currentSelectedDate) fetchAndRenderAppointments(currentSelectedDate, document.querySelector(`[data-date="${currentSelectedDate}"]`));
    } catch (e) {
        console.error(e);
        alert(e.message);
    }
}

// =================================================================================
// 탭 2: 기본 근무시간 설정 관련 함수들
// =================================================================================

/**
 * [수정] 시간 선택 드롭다운 HTML을 생성하는 헬퍼 함수
 */
function createTimeDropdownHTML(name, id, selectedValue, isStartTime) {
    let options = '';
    const startHour = isStartTime ? 9 : 10;
    const endHour = isStartTime ? 17 : 18;

    for (let i = startHour; i <= endHour; i++) {
        const timeStr = `${String(i).padStart(2, '0')}:00`;
        const selected = (selectedValue === timeStr) ? 'selected' : '';
        options += `<option value="${timeStr}" ${selected}>${timeStr}</option>`;
    }
    return `<select name="${name}" id="${id}" class="time-select">${options}</select>`;
}


/**
 * '기본 근무시간 설정' 탭의 기능을 초기화합니다.
 */
function initScheduleSettingsTab() {
    document.getElementById("prevMonthForException").onclick = () => { exceptionViewDate.setMonth(exceptionViewDate.getMonth()-1); renderExceptionCalendar(); };
    document.getElementById("nextMonthForException").onclick = () => { exceptionViewDate.setMonth(exceptionViewDate.getMonth()+1); renderExceptionCalendar(); };

    document.querySelectorAll('input[name="exceptionType"]').forEach(radio => {
        radio.onchange = e => {
            const timeRangeContainer = document.getElementById('exceptionTimeRange');
            if (e.target.value === 'partial') {
                const startTimeHTML = createTimeDropdownHTML('exceptionStartTime', 'exceptionStartTime', '09:00', true);
                const endTimeHTML = createTimeDropdownHTML('exceptionEndTime', 'exceptionEndTime', '18:00', false);
                timeRangeContainer.innerHTML = `${startTimeHTML} - ${endTimeHTML}`;
                timeRangeContainer.style.display = 'flex';
                
                // [신규] 드롭다운 생성 후 유효성 즉시 검사
                const startSelect = document.getElementById('exceptionStartTime');
                const endSelect = document.getElementById('exceptionEndTime');
                updateEndTimeOptions(startSelect, endSelect);

            } else {
                timeRangeContainer.style.display = 'none';
                timeRangeContainer.innerHTML = '';
            }
        };
    });
}

/**
 * [신규] 시작 시간에 따라 종료 시간 옵션을 조절하는 함수
 */
function updateEndTimeOptions(startSelect, endSelect) {
    if (!startSelect || !endSelect) return;
    const startHour = parseInt(startSelect.value.split(':')[0]);
    let firstAvailableValue = null;

    // 모든 옵션을 일단 보이게 초기화
    for (const option of endSelect.options) {
        option.style.display = 'block';
    }
    
    // 시작 시간보다 이르거나 같은 옵션 숨기기
    for (const option of endSelect.options) {
        const optionHour = parseInt(option.value.split(':')[0]);
        if (optionHour <= startHour) {
            option.style.display = 'none';
        } else {
            if (!firstAvailableValue) {
                firstAvailableValue = option.value;
            }
        }
    }

    // 현재 선택된 종료 시간이 유효하지 않으면, 선택 가능한 첫 시간으로 변경
    if (parseInt(endSelect.value.split(':')[0]) <= startHour) {
        endSelect.value = firstAvailableValue;
    }
}

/**
 * [신규] 시간 유효성 검사를 위한 이벤트 리스너 설정
 */
function setupTimeValidationListeners() {
    // 1. 주간 반복 근무 시간 설정
    const defaultContainer = document.getElementById('defaultScheduleContainer');
    defaultContainer.addEventListener('change', e => {
        if (e.target && e.target.matches('select[name^="defaultStartTime"]')) {
            const startSelect = e.target;
            const endSelect = startSelect.closest('.time-range').querySelector('select[name^="defaultEndTime"]');
            updateEndTimeOptions(startSelect, endSelect);
        }
    });

    // 2. 특정일 예외 시간 설정
    const exceptionContainer = document.getElementById('exceptionOptions');
    exceptionContainer.addEventListener('change', e => {
        if (e.target && e.target.matches('select[name="exceptionStartTime"]')) {
            const startSelect = e.target;
            const endSelect = document.getElementById('exceptionEndTime');
            updateEndTimeOptions(startSelect, endSelect);
        }
    });
}

/**
 * [API 호출] 기본 및 예외 스케줄 데이터를 가져와 UI에 렌더링합니다.
 */
async function fetchAndRenderSchedules() {
    const y = exceptionViewDate.getFullYear(), m = exceptionViewDate.getMonth()+1;
    try {
        const resp = await fetch(`/api/counselor/schedule?year=${y}&month=${m}`);
        if (!resp.ok) throw new Error('스케줄 정보를 불러오지 못했습니다.');
        scheduleDataCache = await resp.json();
        renderDefaultScheduleUI(scheduleDataCache.defaultSchedules);
        renderExceptionCalendar();
    } catch (e) {
        console.error(e);
        alert(e.message);
    }
}

/**
 * 기본 근무 시간 UI를 그립니다.
 */
function renderDefaultScheduleUI(defaultSchedules=[]) {
    const cont = document.getElementById('defaultScheduleContainer'); cont.innerHTML='';
    const days = ['월요일','화요일','수요일','목요일','금요일','토요일','일요일'];
    for (let i=0; i<7; i++) {
        const dayIndex = i + 1;
        const d = defaultSchedules.find(x=>x.dayOfWeek===dayIndex) || {isWorkingDay:false, startTime:'09:00', endTime:'18:00'};
        
        const startTimeSelect = createTimeDropdownHTML(`defaultStartTime-${dayIndex}`, `defaultStartTime-${dayIndex}`, d.startTime, true);
        const endTimeSelect = createTimeDropdownHTML(`defaultEndTime-${dayIndex}`, `defaultEndTime-${dayIndex}`, d.endTime, false);

        cont.insertAdjacentHTML('beforeend', `
        <div class="weekday-item" data-day="${dayIndex}">
          <label><input type="checkbox" ${d.isWorkingDay?'checked':''}> ${days[i]}</label>
          <div class="time-range">${startTimeSelect} - ${endTimeSelect}</div>
        </div>`);
        
        // [신규] 렌더링 직후 각 행의 시간 유효성 검사 실행
        const newRow = cont.querySelector(`[data-day="${dayIndex}"]`);
        const startSelect = newRow.querySelector('select[name^="defaultStartTime"]');
        const endSelect = newRow.querySelector('select[name^="defaultEndTime"]');
        updateEndTimeOptions(startSelect, endSelect);
    }
}

/**
 * 예외 설정용 캘린더 UI를 그립니다.
 */
function renderExceptionCalendar() {
    const grid = document.getElementById('calendarGridForException');
    const monthDisp = document.getElementById('currentMonthForException');
    const wk = document.getElementById('weekdaysForException');
    const y = exceptionViewDate.getFullYear(), M = exceptionViewDate.getMonth();

    monthDisp.textContent = `${y}년 ${M+1}월`;
    grid.innerHTML=''; wk.innerHTML='';
    ["일","월","화","수","목","금","토"].forEach(d=>wk.insertAdjacentHTML('beforeend', `<div>${d}</div>`));
    
    const firstDay = new Date(y,M,1).getDay();
    const daysInMonth = new Date(y,M+1,0).getDate();
    const today = new Date();
    today.setHours(0,0,0,0); 

    for(let i=0; i < firstDay; i++) {
        grid.insertAdjacentHTML('beforeend','<div class="calendar-day empty"></div>');
    }
    
    for(let day=1; day <= daysInMonth; day++){
        const cell = document.createElement('div');
        cell.className='calendar-day'; 
        cell.textContent=day;
        const dateStr = `${y}-${String(M+1).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
        const cellDate = new Date(y, M, day);

        if (cellDate < today) {
            cell.classList.add('disabled');
        }

        if (scheduleDataCache?.exceptions?.some(ex=>ex.exceptionDate===dateStr)) {
            cell.classList.add('has-exception');
        }

        cell.dataset.date=dateStr;
        if (!cell.classList.contains('disabled')) {
            cell.onclick = () => selectExceptionDate(dateStr, cell);
        }
        grid.appendChild(cell);
    }
}

/**
 * 예외 설정 날짜 클릭 시 UI 업데이트
 */
function selectExceptionDate(dateStr, el) {
    selectedExceptionDate = dateStr; // 선택된 날짜를 전역 변수에 저장
    document.querySelectorAll('#calendarGridForException .selected').forEach(d=>d.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('exceptionDateText').textContent=`선택된 날짜: ${dateStr}`;
    document.getElementById('exceptionOptions').style.display='block';

    const ex = scheduleDataCache?.exceptions?.find(e=>e.exceptionDate===dateStr);
    const dayOffRadio = document.querySelector('input[value="dayOff"]');
    const partialRadio = document.querySelector('input[value="partial"]');
    const timeRangeContainer = document.getElementById('exceptionTimeRange');
    
    if (ex) {
        if (!ex.startTime && !ex.endTime) {
            dayOffRadio.checked=true; 
            timeRangeContainer.style.display='none';
            timeRangeContainer.innerHTML = '';
        } else {
            partialRadio.checked=true;
            const startTimeHTML = createTimeDropdownHTML('exceptionStartTime', 'exceptionStartTime', ex.startTime, true);
            const endTimeHTML = createTimeDropdownHTML('exceptionEndTime', 'exceptionEndTime', ex.endTime, false);
            timeRangeContainer.innerHTML = `${startTimeHTML} - ${endTimeHTML}`;
            timeRangeContainer.style.display='flex';
            
            const startSelect = document.getElementById('exceptionStartTime');
            const endSelect = document.getElementById('exceptionEndTime');
            updateEndTimeOptions(startSelect, endSelect);
        }
    } else {
        dayOffRadio.checked=true; 
        timeRangeContainer.style.display='none';
        timeRangeContainer.innerHTML = '';
    }
}

/**
 * [기능 구현] 기본 일정 저장 (PUT /api/counselor/schedule/default)
 */
async function saveDefaultSchedule() {
    const schedules = [];
    document.querySelectorAll('#defaultScheduleContainer .weekday-item').forEach(item => {
        const schedule = {
            dayOfWeek: parseInt(item.dataset.day),
            workingDay: item.querySelector('input[type="checkbox"]').checked, // DTO 필드명 'isWorkingDay'를 'workingDay'로 수정
            startTime: item.querySelector('select[name^="defaultStartTime"]').value,
            endTime: item.querySelector('select[name^="defaultEndTime"]').value,
        };
        schedules.push(schedule);
    });

    try {
        const response = await fetch('/api/counselor/schedule/default', {
            method: 'PUT', // [수정] 백엔드 @PutMapping에 맞춰 POST에서 PUT으로 변경
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(schedules),
        });

        if (!response.ok) throw new Error('기본 일정 저장에 실패했습니다.');
        
        alert('기본 근무 일정이 저장되었습니다.');

    } catch (error) {
        console.error('기본 일정 저장 오류:', error);
        alert(error.message);
    }
}

/**
 * [기능 구현] 예외 설정 저장 (POST /api/counselor/schedule/exception)
 */
async function saveException() {
    if (!selectedExceptionDate) {
        alert('먼저 캘린더에서 날짜를 선택해주세요.');
        return;
    }

    const isDayOff = document.querySelector('input[name="exceptionType"][value="dayOff"]').checked;
    let exceptionData = {
        exceptionDate: selectedExceptionDate,
        startTime: null,
        endTime: null,
    };

    if (!isDayOff) {
        exceptionData.startTime = document.getElementById('exceptionStartTime').value;
        exceptionData.endTime = document.getElementById('exceptionEndTime').value;
    }

    try {
        const response = await fetch('/api/counselor/schedule/exception', {
            method: 'POST', // 백엔드 @PostMapping과 일치
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(exceptionData),
        });

        if (!response.ok) throw new Error('예외 설정 저장에 실패했습니다.');

        alert('예외 설정이 저장되었습니다.');
        await fetchAndRenderSchedules(); // UI 새로고침

    } catch (error) {
        console.error('예외 설정 저장 오류:', error);
        alert(error.message);
    }
}

/**
 * [기능 구현] 예외 설정 삭제 (DELETE /api/counselor/schedule/exception/{date})
 */
async function clearException() {
    if (!selectedExceptionDate) {
        alert('먼저 캘린더에서 날짜를 선택해주세요.');
        return;
    }
    
    if (!confirm(`${selectedExceptionDate}의 예외 설정을 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/api/counselor/schedule/exception/${selectedExceptionDate}`, {
            method: 'DELETE', // 백엔드 @DeleteMapping과 일치
        });

        if (!response.ok) throw new Error('예외 설정 삭제에 실패했습니다.');

        alert('예외 설정이 삭제되었습니다.');
        document.getElementById('exceptionOptions').style.display = 'none'; // 삭제 후 옵션 창 숨기기
        await fetchAndRenderSchedules(); // UI 새로고침

    } catch (error) {
        console.error('예외 설정 삭제 오류:', error);
        alert(error.message);
    }
}

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

    // Node.js 서버에 연결
    socket = io('http://210.178.108.186:3001');

    // --- 소켓 이벤트 리스너 등록 ---
    socket.on('connect', () => {
        console.log('채팅 서버 연결 성공');
        messagesContainer.innerHTML = '';
        
        // TODO: 이 객체는 각 파일(학생/상담사)에 맞게 수정해야 합니다.
        const currentUser = getUserInfoForChat();
        
        socket.emit('joinRoom', { roomId: currentRoomId, user: currentUser });
    });

    socket.on('receiveMessage', (message) => {
        addMessageToChat(message);
    });

    socket.on('disconnect', () => console.log('채팅 서버 연결 종료'));
    
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
    // TODO: 실제 로그인한 상담사 정보로 대체해야 합니다.
    return {
        id: '2025110002',
        name: '상담사'
    };
}