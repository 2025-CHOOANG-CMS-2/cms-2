/**
 * counsel-book.js
 * 학생 상담 예약 페이지의 동적 기능을 처리합니다. (API 연동 기반)
 */

// 전역 변수
const viewDate = new Date();
let availableDataCache = {}; // 월별 예약 가능 데이터를 캐싱하여 불필요한 API 호출을 줄임
let selectedInfo = {
    date: null,
    time: null,
    counselor: null // { emplNo, name } 형태의 객체
};

// 페이지 로드 후 초기화
document.addEventListener("DOMContentLoaded", () => {
    initializeEventListeners();
    handleFilterChange(); // 페이지 로드 시 필터에 맞춰 초기 데이터 로딩
});

/**
 * 모든 이벤트 리스너를 등록하는 함수
 */
function initializeEventListeners() {
    document.getElementById('counselingTypeFilter').addEventListener('change', handleFilterChange);
    document.getElementById('counselorFilter').addEventListener('change', fetchAndRenderCalendar); // 상담사 변경 시 캘린더만 새로고침
    document.getElementById('prevMonth').addEventListener('click', () => { viewDate.setMonth(viewDate.getMonth() - 1); fetchAndRenderCalendar(); });
    document.getElementById('nextMonth').addEventListener('click', () => { viewDate.setMonth(viewDate.getMonth() + 1); fetchAndRenderCalendar(); });
    document.getElementById('bookingForm').addEventListener('submit', handleBookingSubmit);
}

/**
 * 상담 유형 필터 변경 시 실행되는 메인 함수
 */
async function handleFilterChange() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorSelect = document.getElementById('counselorFilter');

    counselorSelect.innerHTML = '<option value="all">모든 상담사</option>';

    if (type !== 'all') {
        try {
            // [수정] API 호출 주소를 올바르게 변경합니다.
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
    
    fetchAndRenderCalendar();
}

/**
 * 백엔드에서 예약 가능 데이터를 가져와 캘린더를 그리는 함수
 */
async function fetchAndRenderCalendar() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorId = document.getElementById('counselorFilter').value;
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1;

    try {
        // [수정] API 호출 주소를 백엔드 컨트롤러에 맞게 변경합니다.
        const url = `/api/counseling/available-slots?year=${year}&month=${month}&type=${type}&counselorId=${counselorId}`;
        
        const response = await fetch(url);
        
        if (!response.ok) {
            // 404 에러가 발생하면 response.ok가 false가 됩니다.
            throw new Error('예약 가능 시간 로딩 실패');
        }
        
        availableDataCache = await response.json();
        renderCalendar(year, month - 1);
        clearTimeSlotsAndSelection();

    } catch (error) {
        console.error("fetchAndRenderCalendar에서 에러 발생:", error);
        // 화면의 캘린더를 비워 에러 상태임을 명확히 보여줄 수 있습니다.
        document.getElementById("calendarGrid").innerHTML = '<div class="empty-state-sm" style="grid-column: span 7;">예약 정보를 불러올 수 없습니다.</div>';
    }
}

/**
 * 캘린더 UI를 그리고 날짜를 채우는 함수
 */
function renderCalendar(year, month) {
    const calendarGrid = document.getElementById("calendarGrid");
    const monthDisplay = document.getElementById("currentMonth");
    monthDisplay.textContent = `${year}년 ${month + 1}월`;
    calendarGrid.innerHTML = "";

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

        if (availableDataCache[dateStr] && availableDataCache[dateStr].length > 0) {
            dayCell.classList.add("available");
            dayCell.addEventListener("click", () => handleDateClick(dateStr, dayCell));
        } else {
            dayCell.classList.add("unavailable");
        }
        calendarGrid.appendChild(dayCell);
    }
}

/**
 * 날짜 클릭 시 시간 슬롯 렌더링
 */
function handleDateClick(dateStr, dayCell) {
    selectedInfo.date = dateStr;
    selectedInfo.time = null; // 날짜를 새로 선택했으니 시간은 초기화

    document.querySelectorAll("#calendarGrid .calendar-day.selected").forEach(d => d.classList.remove("selected"));
    dayCell.classList.add("selected");
    
    const timeSlots = availableDataCache[dateStr] || [];
    const slotsContainer = document.getElementById("timeSlots");
    slotsContainer.innerHTML = ""; 

    if (timeSlots.length === 0) {
        slotsContainer.innerHTML = '<div class="empty-state-sm">선택 가능한 시간이 없습니다.</div>';
        return;
    }

    timeSlots.forEach(timeData => { // timeData는 { counselorEmplNo, counselorName, time } 형태일 수 있음
        const slotEl = document.createElement("div");
        slotEl.className = "time-slot";
        slotEl.dataset.time = timeData.time; // 예: "09:00"
        slotEl.textContent = timeData.time;
        // 툴팁으로 상담사 이름 표시 (선택사항)
        slotEl.title = `${timeData.counselorName} 상담사`; 

        slotEl.addEventListener("click", () => {
            selectedInfo.time = timeData.time;
            selectedInfo.counselor = { emplNo: timeData.counselorEmplNo, name: timeData.counselorName };

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
 * 최종 '상담 예약하기' 폼 제출 처리 함수
 */
async function handleBookingSubmit(event) {
    event.preventDefault();
    
    if (!selectedInfo.date || !selectedInfo.time || !selectedInfo.counselor) {
        alert("상담 날짜, 시간, 상담사를 모두 선택해주세요.");
        return;
    }

    const form = event.target;
    const reservationData = {
        stdNo: '20231234', // TODO: 실제 로그인한 학생의 학번으로 교체 필요
        emplNo: selectedInfo.counselor.emplNo,
        applyDateTime: `${selectedInfo.date}T${selectedInfo.time}:00`,
        counselingMethod: form.elements.counselingMethod.value,
        content: form.elements.content.value,
    };

    try {
        // TODO: 백엔드에 이 API를 만들어야 합니다.
        const response = await fetch('/api/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reservationData)
        });

        if (!response.ok) throw new Error('예약에 실패했습니다.');

        alert('상담 예약이 성공적으로 완료되었습니다.');
        window.location.reload();

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}