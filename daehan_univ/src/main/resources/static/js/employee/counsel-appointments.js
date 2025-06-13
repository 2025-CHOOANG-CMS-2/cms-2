// counsel-appointments.js

// 전역 변수
const viewDate = new Date();
const appointments = [
  { id: "apt001", date: "2025-06-21", time: "10:00", studentName: "김학생", studentId: "20241001", content: "졸업 후 진로 방향에 대해 상담받고 싶습니다.", status: "pending" },
  { id: "apt002", date: "2025-06-21", time: "14:00", studentName: "이학생", studentId: "20241002", content: "학점 관리와 효과적인 학습 방법에 대해 조언을 구하고 싶습니다.", status: "pending" },
  { id: "apt003", date: "2025-06-22", time: "11:00", studentName: "박학생", studentId: "20241003", content: "최근 스트레스가 많아서 상담을 받고 싶습니다.", status: "approved" },
  { id: "apt004", date: "2025-06-23", time: "15:00", studentName: "최학생", studentId: "20241004", content: "이력서 작성과 면접 준비에 대해 도움을 받고 싶습니다.", status: "completed" },
];

// DOM 로드 후 초기화
document.addEventListener("DOMContentLoaded", () => {
  renderCalendar();
  bindAppointmentListeners();
});

/** 캘린더 렌더링 */
function renderCalendar() {
  const calendarGrid = document.getElementById("calendarGrid");
  const monthDisplay  = document.getElementById("currentMonth");
  if (!calendarGrid || !monthDisplay) return;

  const year = viewDate.getFullYear();
  const month = viewDate.getMonth();
  const monthNames = ["1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"];
  monthDisplay.textContent = `${year}년 ${monthNames[month]}`;
  calendarGrid.innerHTML = "";

  // 요일 헤더
  ["일","월","화","수","목","금","토"].forEach(d => {
    const hd = document.createElement("div");
    hd.className = "calendar-day header";
    hd.textContent = d;
    calendarGrid.appendChild(hd);
  });

  // 1일이 시작하는 요일 + 말일 계산
  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month+1, 0).getDate();

  // 빈 칸
  for (let i=0; i<firstDay; i++) {
    const empty = document.createElement("div");
    empty.className = "calendar-day empty";
    calendarGrid.appendChild(empty);
  }

  // 날짜 칸
  for (let date=1; date<=lastDate; date++) {
    const dayEl = document.createElement("div");
    dayEl.className = "calendar-day";
    dayEl.textContent = date;
    const dateStr = `${year}-${String(month+1).padStart(2,"0")}-${String(date).padStart(2,"0")}`;

    // 예약 상태 표시
    const todays = appointments.filter(a=>a.date===dateStr);
    if (todays.some(a=>a.status==="pending"))   dayEl.classList.add("has-pending");
    if (todays.some(a=>a.status==="approved"))  dayEl.classList.add("has-approved");

    // 클릭 바인딩
    dayEl.addEventListener("click", () => showDateAppointments(dateStr, dayEl));

    calendarGrid.appendChild(dayEl);
  }
}

/** 전/월 버튼 바인딩 */
function bindAppointmentListeners() {
  const prev = document.getElementById("prevMonth");
  const next = document.getElementById("nextMonth");
  if (prev) prev.addEventListener("click", () => { viewDate.setMonth(viewDate.getMonth()-1); renderCalendar(); });
  if (next) next.addEventListener("click", () => { viewDate.setMonth(viewDate.getMonth()+1); renderCalendar(); });
}

/** 날짜 클릭 시 해당 예약들 표시 */
function showDateAppointments(dateStr, clickedEl) {
  // 선택 표시
  document.querySelectorAll("#calendarGrid .calendar-day.selected")
          .forEach(d=>d.classList.remove("selected"));
  clickedEl.classList.add("selected");

  // 예약 리스트 영역
  const container = document.getElementById("selectedDateAppointments");
  const listEl = document.getElementById("dateAppointmentList");
  const noSel  = document.getElementById("noDateSelected");
  if (!container||!listEl||!noSel) return;

  const todays = appointments.filter(a=>a.date===dateStr);
  if (todays.length===0) {
    listEl.innerHTML = `<p class="text-center text-muted">예약이 없습니다.</p>`;
  } else {
    listEl.innerHTML = "";
    todays.forEach(a => {
      const item = document.createElement("div");
      item.className = `appointment-item status-${a.status}`;
      item.innerHTML = `
        <div class="appointment-header">
          <div><i class="fas fa-clock"></i> ${a.time}</div>
          <div class="appointment-status">${a.status}</div>
        </div>
        <div class="student-info">
          <h5><i class="fas fa-user"></i> ${a.studentName} (${a.studentId})</h5>
          <p class="text-sm">${a.content}</p>
        </div>`;
      listEl.appendChild(item);
    });
  }
  container.style.display = "block";
  noSel.style.display = "none";
}
