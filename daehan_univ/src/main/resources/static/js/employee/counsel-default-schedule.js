// counsel-default-schedule.js

// 전역 변수
const exceptionDate = new Date();
const defaultSchedule = {
  monday: { enabled: true,  start: "09:00", end: "17:00" },
  tuesday:{ enabled: true,  start: "09:00", end: "17:00" },
  wednesday: { enabled: true, start: "09:00", end: "17:00" },
  thursday:{ enabled: true, start: "09:00", end: "17:00" },
  friday: { enabled: true,  start: "09:00", end: "17:00" },
  saturday:{ enabled: false, start: "09:00", end: "13:00" },
  sunday: { enabled: false, start: "09:00", end: "13:00" }
};
const exceptions = {};  // 저장된 예외 { "2025-06-23": {...} }

document.addEventListener("DOMContentLoaded", () => {
  renderExceptionCalendar();
  bindExceptionListeners();
});

function renderExceptionCalendar() {
  const grid = document.getElementById("calendarGridForException");
  const monthDisp = document.getElementById("currentMonthForException");
  if (!grid||!monthDisp) return;

  const y = exceptionDate.getFullYear(), m = exceptionDate.getMonth();
  monthDisp.textContent = `${y}년 ${m+1}월`;
  grid.innerHTML = "";

  ["일","월","화","수","목","금","토"].forEach(d=>{
    const hd = document.createElement("div");
    hd.className="calendar-day header"; hd.textContent=d;
    grid.appendChild(hd);
  });

  const firstDay = new Date(y,m,1).getDay();
  const lastDate = new Date(y,m+1,0).getDate();
  // 빈칸
  for(let i=0;i<firstDay;i++){
    const e=document.createElement("div"); e.className="calendar-day empty";
    grid.appendChild(e);
  }
  // 날짜
  for(let d=1; d<=lastDate; d++){
    const el = document.createElement("div");
    el.className="calendar-day"; el.textContent=d;
    const ds = `${y}-${String(m+1).padStart(2,"0")}-${String(d).padStart(2,"0")}`;

    // 이미 저장된 예외 표시
    if (exceptions[ds]) el.classList.add("has-exception");

    el.addEventListener("click", ()=> selectExceptionDate(ds, el));
    grid.appendChild(el);
  }
}

function bindExceptionListeners() {
  const prev = document.getElementById("prevMonthForException");
  const next = document.getElementById("nextMonthForException");
  if (prev) prev.addEventListener("click", () => { exceptionDate.setMonth(exceptionDate.getMonth()-1); renderExceptionCalendar(); });
  if (next) next.addEventListener("click", () => { exceptionDate.setMonth(exceptionDate.getMonth()+1); renderExceptionCalendar(); });

  document.querySelectorAll('input[name="exceptionType"]').forEach(radio=>{
    radio.addEventListener("change", () => {
      const ts = document.getElementById("exceptionTimeSlots");
      ts.style.display = (radio.value==="unavailable" ? "none" : "block");
    });
  });
}

function selectExceptionDate(dateStr, el) {
  // visual
  document.querySelectorAll("#calendarGridForException .selected")
          .forEach(d=>d.classList.remove("selected"));
  el.classList.add("selected");

  // 텍스트
  const txt = document.getElementById("exceptionDateText");
  txt.textContent = `선택된 날짜: ${dateStr.replace(/-/g,"년 ").replace(/$/, "일")}`;

  // 옵션 보이기
  document.getElementById("exceptionOptions").style.display = "block";

  // 시간 슬롯 생성
  generateExceptionTimeSlots(dateStr);
}

function generateExceptionTimeSlots(dateStr) {
  const grid = document.getElementById("exceptionTimeSlotGrid");
  grid.innerHTML = "";
  const dt = new Date(dateStr);
  const dow = ["sunday","monday","tuesday","wednesday","thursday","friday","saturday"][dt.getDay()];
  const sched = defaultSchedule[dow];
  if (!sched.enabled) {
    grid.innerHTML = "<p>이 요일은 기본 근무일이 아닙니다.</p>";
    return;
  }
  const startH = +sched.start.split(":")[0];
  const endH   = +sched.end.split(":")[0];
  for(let h=startH; h<endH; h++){
    const slot = document.createElement("div");
    slot.className = "time-slot";
    slot.dataset.time = `${String(h).padStart(2,"0")}:00`;
    slot.textContent = slot.dataset.time;
    slot.addEventListener("click", ()=>slot.classList.toggle("selected"));
    grid.appendChild(slot);
  }
}

// 저장/삭제
function saveException() {
  const sel = document.querySelector("#calendarGridForException .selected");
  if (!sel) { alert("날짜 선택"); return; }
  const dateStr = `${exceptionDate.getFullYear()}-${String(exceptionDate.getMonth()+1).padStart(2,"0")}-${String(sel.textContent).padStart(2,"0")}`;
  const type = document.querySelector('input[name="exceptionType"]:checked').value;
  const times = Array.from(document.querySelectorAll("#exceptionTimeSlotGrid .selected"))
                     .map(s=>s.dataset.time);
  exceptions[dateStr] = { type, times };
  alert("저장됨");
  renderExceptionCalendar();
}
function clearException() {
  const sel = document.querySelector("#calendarGridForException .selected");
  if(!sel) return;
  const dateStr = `${exceptionDate.getFullYear()}-${String(exceptionDate.getMonth()+1).padStart(2,"0")}-${String(sel.textContent).padStart(2,"0")}`;
  delete exceptions[dateStr];
  alert("삭제됨");
  renderExceptionCalendar();
}
