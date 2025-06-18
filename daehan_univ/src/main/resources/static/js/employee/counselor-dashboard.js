/**
 * counselor-dashboard.js
 * 상담사 대시보드 페이지의 모든 JavaScript 기능을 관리합니다.
 * (탭, 캘린더, 예약 목록, 근무시간 설정 등)
 */

// =================================================================================
// 전역 변수 및 목업(Mock) 데이터
// =================================================================================

const viewDate = new Date(); // 캘린더 표시용 날짜 객체

// TODO: 이 데이터는 나중에 백엔드 API를 통해 받아와야 합니다.
const mockAppointments = [
  { id: "apt001", date: "2025-06-21", time: "10:00", studentName: "김학생", studentId: "20241001", content: "졸업 후 진로 방향에 대해 상담받고 싶습니다.", status: "pending" },
  { id: "apt002", date: "2025-06-21", time: "14:00", studentName: "이학생", studentId: "20241002", content: "학점 관리와 효과적인 학습 방법에 대해 조언을 구하고 싶습니다.", status: "pending" },
  { id: "apt003", date: "2025-06-22", time: "11:00", studentName: "박학생", studentId: "20241003", content: "최근 스트레스가 많아서 상담을 받고 싶습니다.", status: "approved" },
  { id: "apt004", date: "2025-06-23", time: "15:00", studentName: "최학생", studentId: "20241004", content: "이력서 작성과 면접 준비에 대해 도움을 받고 싶습니다.", status: "completed" },
];
const mockExceptions = {}; // 예: { "2025-06-25": { type: 'unavailable' } }


// =================================================================================
// 초기화 및 메인 로직
// =================================================================================

document.addEventListener("DOMContentLoaded", () => {
    initTabs();
    initViewCalendar();
    initExceptionCalendar();
    bindExceptionOptionListeners();
});

/** 탭 전환 기능을 초기화하는 함수 */
function initTabs() {
    const tabButtons = document.querySelectorAll(".tab-btn");
    const tabContents = document.querySelectorAll(".tab-content");

    tabButtons.forEach(button => {
        button.addEventListener("click", () => {
            tabButtons.forEach(btn => btn.classList.remove("active"));
            tabContents.forEach(content => content.classList.remove("active"));
            button.classList.add("active");
            document.getElementById(button.dataset.tab).classList.add("active");
        });
    });
}

/** '예약 현황 캘린더' 관련 기능을 초기화하는 함수 */
function initViewCalendar() {
    const calendarGrid = document.getElementById("calendarGridForView");
    const monthDisplay = document.getElementById("currentMonth");

    document.getElementById("prevMonth").addEventListener("click", () => {
        viewDate.setMonth(viewDate.getMonth() - 1);
        renderCalendar(viewDate, calendarGrid, monthDisplay, showDateAppointments);
    });
    document.getElementById("nextMonth").addEventListener("click", () => {
        viewDate.setMonth(viewDate.getMonth() + 1);
        renderCalendar(viewDate, calendarGrid, monthDisplay, showDateAppointments);
    });
    renderCalendar(viewDate, calendarGrid, monthDisplay, showDateAppointments);
}

/** '예외 설정 캘린더' 관련 기능을 초기화하는 함수 */
function initExceptionCalendar() {
    const calendarGrid = document.getElementById("calendarGridForException");
    const monthDisplay = document.getElementById("currentMonthForException");
    const exceptionDate = new Date(); // 이 캘린더 전용 날짜 객체

    document.getElementById("prevMonthForException").addEventListener("click", () => {
        exceptionDate.setMonth(exceptionDate.getMonth() - 1);
        renderCalendar(exceptionDate, calendarGrid, monthDisplay, selectExceptionDate);
    });
    document.getElementById("nextMonthForException").addEventListener("click", () => {
        exceptionDate.setMonth(exceptionDate.getMonth() + 1);
        renderCalendar(exceptionDate, calendarGrid, monthDisplay, selectExceptionDate);
    });
    renderCalendar(exceptionDate, calendarGrid, monthDisplay, selectExceptionDate);
}


// =================================================================================
// 캘린더 공통 함수
// =================================================================================

/**
 * 캘린더 UI를 그리고 날짜를 채우는 공통 함수
 * @param {Date} dateObject - 기준 날짜
 * @param {HTMLElement} gridContainer - 달력이 그려질 div
 * @param {HTMLElement} monthDisplayElement - '연도 월'이 표시될 h4
 * @param {Function} onDateClick - 날짜 클릭 시 실행될 콜백 함수
 */
function renderCalendar(dateObject, gridContainer, monthDisplayElement, onDateClick) {
    if (!gridContainer || !monthDisplayElement) return;

    const year = dateObject.getFullYear();
    const month = dateObject.getMonth();
    
    monthDisplayElement.textContent = `${year}년 ${month + 1}월`;
    gridContainer.innerHTML = ''; 

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) {
        gridContainer.insertAdjacentHTML('beforeend', '<div class="calendar-day empty"></div>');
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dayCell = document.createElement("div");
        dayCell.className = "calendar-day";
        dayCell.textContent = day;
        
        const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        
        // --- 캘린더별 상태 표시 ---
        if (gridContainer.id === 'calendarGridForView') {
            const todays = mockAppointments.filter(a => a.date === dateStr);
            if (todays.some(a => a.status === "pending")) dayCell.classList.add("has-pending");
            if (todays.some(a => a.status === "approved")) dayCell.classList.add("has-approved");
        }
        if (gridContainer.id === 'calendarGridForException') {
            if (mockExceptions[dateStr]) dayCell.classList.add("has-exception");
        }
        // -------------------------
        
        // 날짜 클릭 이벤트 연결
        dayCell.addEventListener("click", () => onDateClick(dateStr, dayCell));
        
        gridContainer.appendChild(dayCell);
    }
}


// =================================================================================
// 탭 1: 일정 및 예약 관리 관련 함수 (기존 counsel-appointments.js 내용)
// =================================================================================

/** 날짜 클릭 시 해당 예약들 표시 */
function showDateAppointments(dateStr, clickedEl) {
  document.querySelectorAll("#calendarGridForView .calendar-day.selected")
          .forEach(d => d.classList.remove("selected"));
  clickedEl.classList.add("selected");

  const container = document.getElementById("selectedDateAppointments");
  const listEl = document.getElementById("dateAppointmentList");
  const noSel = document.getElementById("noDateSelected");
  if (!container || !listEl || !noSel) return;

  const todays = mockAppointments.filter(a => a.date === dateStr);
  if (todays.length === 0) {
    listEl.innerHTML = `<p class="text-center text-muted">예약이 없습니다.</p>`;
  } else {
    listEl.innerHTML = "";
    todays.sort((a,b) => a.time.localeCompare(b.time)).forEach(a => {
      const item = document.createElement("div");
      item.className = `appointment-item status-${a.status}`;
      item.innerHTML = `
        <div class="appointment-header">
          <div><i class="fas fa-clock"></i> ${a.time}</div>
          <div class="appointment-status">${a.status === 'pending' ? '승인대기' : (a.status === 'approved' ? '예약확정' : '상담완료')}</div>
        </div>
        <div class="student-info">
          <h5><i class="fas fa-user"></i> ${a.studentName} (${a.studentId})</h5>
          <p class="text-sm">${a.content}</p>
        </div>
      `;
      // TODO: 여기에 승인/반려 버튼 및 이벤트 추가 필요
      listEl.appendChild(item);
    });
  }
  container.style.display = "block";
  noSel.style.display = "none";
}


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

function saveDefaultSchedule() {
    // TODO: 요일별 근무시간 설정 저장 API 호출
    alert("기본 일정이 저장되었습니다. (구현 필요)");
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