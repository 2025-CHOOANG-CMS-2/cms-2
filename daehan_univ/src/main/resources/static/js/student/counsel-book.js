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
	document.querySelector('input[name="phone"]').addEventListener('input', handlePhoneInput);
}

/**
 * 상담 유형 필터 변경 시 실행되는 메인 함수
 */
async function handleFilterChange() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorSelect = document.getElementById('counselorFilter');

    counselorSelect.innerHTML = '<option value="all">상담 유형을 먼저 선택해주세요</option>';

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

// counsel-book.js

/**
 * 백엔드에서 예약 가능 데이터를 가져와 캘린더를 그리는 함수
 */
async function fetchAndRenderCalendar() {
    const type = document.getElementById('counselingTypeFilter').value;
    const counselorId = document.getElementById('counselorFilter').value;
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth() + 1;

    try {
        const url = `/api/counseling/available-slots?year=${year}&month=${month}&type=${type}&counselorId=${counselorId}`;
        console.log("요청하는 API 주소:", url);
        
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('예약 가능 시간 로딩 실패');
        }
        
        availableDataCache = await response.json();
        
        // [수정] renderCalendar 함수를 호출할 때, 받아온 데이터를 파라미터로 전달합니다.
        renderCalendar(year, month - 1, availableDataCache); 
        
        clearTimeSlotsAndSelection();

    } catch (error) {
        console.error("fetchAndRenderCalendar에서 에러 발생:", error);
        document.getElementById("calendarGrid").innerHTML = '<div class="empty-state-sm" style="grid-column: span 7;">예약 정보를 불러올 수 없습니다.</div>';
    }
}

/**
 * 캘린더 UI를 그리고 날짜를 채우는 함수
 */
// [수정] 세 번째 파라미터로 availableData를 받도록 변경합니다.
function renderCalendar(year, month, availableData) {
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
        
        const today = new Date();
        const currentDate = new Date(dateStr);
        today.setHours(0, 0, 0, 0);

        if (currentDate < today) {
            dayCell.classList.add("unavailable");
        } else if (availableData && availableData[dateStr] && availableData[dateStr].length > 0) {
            // [수정] 이제 파라미터로 받은 availableData를 사용하므로 에러가 나지 않습니다.
            dayCell.classList.add("available");
            dayCell.addEventListener("click", () => handleDateClick(dateStr, dayCell));
        } else {
            dayCell.classList.add("unavailable");
        }
        calendarGrid.appendChild(dayCell);
    }
}

/**
 * 캘린더에서 특정 날짜를 클릭했을 때 실행되는 함수
 * @param {string} dateStr - 클릭된 날짜 문자열 (예: "2025-06-20")
 * @param {HTMLElement} dayCell - 클릭된 날짜의 div 요소
 */
function handleDateClick(dateStr, dayCell) {
    const counselorFilter = document.getElementById('counselorFilter');

    // 1. (가장 간단한 UX를 위한 핵심 로직) '모든 상담사'가 선택된 경우, 경고 메시지를 보여주고 함수를 종료합니다.
    if (counselorFilter.value === 'all') {
        alert('상담사를 먼저 선택해주세요.');
        return; // 여기서 함수를 끝내서 시간 슬롯을 불러오지 않음
    }

    // 2. 전역 변수에 선택된 날짜를 저장하고, 시간은 초기화합니다.
    selectedInfo.date = dateStr;
    selectedInfo.time = null;

    // 3. 이전에 선택된 날짜의 'selected' 클래스는 제거하고, 새로 클릭된 날짜에 추가하여 시각적으로 표시합니다.
    document.querySelectorAll("#calendarGrid .calendar-day.selected").forEach(d => d.classList.remove("selected"));
    dayCell.classList.add("selected");

    // 4. API로 미리 받아온 월간 데이터 캐시에서, 클릭된 날짜에 해당하는 시간 목록을 가져옵니다.
    const timeSlotsData = availableDataCache[dateStr] || [];
    const slotsContainer = document.getElementById("timeSlots");
    slotsContainer.innerHTML = ""; // 기존 시간 버튼들을 모두 삭제합니다.

    if (timeSlotsData.length === 0) {
        slotsContainer.innerHTML = '<div class="empty-state-sm">선택 가능한 시간이 없습니다.</div>';
        updateSelectedInfo(); // 선택 정보 텍스트도 업데이트
        return;
    }

    // 5. 조회된 시간 목록을 바탕으로 시간 버튼(div)들을 동적으로 생성합니다.
    timeSlotsData.forEach(slotData => {
        const slotEl = document.createElement("div");
        slotEl.className = "time-slot";
        slotEl.dataset.time = slotData.time;
        slotEl.textContent = slotData.time;
        slotEl.title = `${slotData.counselorName} 상담사`;

        // 6. 각 시간 버튼에 클릭 이벤트를 추가합니다.
        slotEl.addEventListener("click", () => {
            // 전역 변수에 선택된 시간과 상담사 정보를 저장합니다.
            selectedInfo.time = slotData.time;
            selectedInfo.counselor = { emplNo: slotData.counselorEmplNo, name: slotData.counselorName };

            // 클릭된 버튼에 'selected' 클래스를 부여하여 시각적으로 표시합니다.
            document.querySelectorAll("#timeSlots .time-slot.selected").forEach(s => s.classList.remove("selected"));
            slotEl.classList.add("selected");

            // 오른쪽 폼의 '선택 정보' 텍스트를 업데이트합니다.
            updateSelectedInfo();
        });
        slotsContainer.appendChild(slotEl);
    });
    
    // 최종적으로 선택 정보 텍스트를 업데이트합니다.
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

function handlePhoneInput(event) {
    const phone = event.target.value.replace(/[^0-9]/g, ''); // 숫자만 추출
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

/**
 * 최종 '상담 예약하기' 폼 제출 처리 함수
 */
async function handleBookingSubmit(event) {
    // 1. 폼의 기본 제출(새로고침) 동작을 막습니다.
    event.preventDefault(); 
    
    // 2. 사용자가 모든 정보를 선택했는지 확인합니다.
    if (!selectedInfo.date || !selectedInfo.time || !selectedInfo.counselor) {
        alert("상담 유형, 상담사, 날짜와 시간을 모두 선택해주세요.");
        return;
    }

    const form = event.target;
	const phoneInput = form.elements.phone.value;
	
    // 3. 폼의 유효성을 체크합니다. (HTML의 required 속성)
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    // 4. 백엔드 DTO 형식에 맞춰 보낼 데이터를 생성합니다.
    const reservationData = {
        stdNo: '2025004001', // TODO: 실제 로그인한 학생의 학번으로 교체 필요
        emplNo: selectedInfo.counselor.emplNo,
        counselingType: document.getElementById('counselingTypeFilter').value,
        applyDateTime: `${selectedInfo.date}T${selectedInfo.time}:00`,
        counselingMethod: form.elements.counselingMethod.value,
        content: form.elements.content.value,
        phone: phoneInput.replace(/-/g, '')
    };

    console.log("서버로 전송할 예약 데이터:", reservationData);

    // 5. fetch API를 사용해 서버에 예약 생성(POST) 요청을 보냅니다.
    try {
        const response = await fetch('/api/counseling/reservations', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reservationData)
        });

        if (!response.ok) {
            // 서버에서 보낸 에러 메시지가 있다면 사용하고, 없다면 기본 메시지를 사용합니다.
            const errorData = await response.json().catch(() => null);
            const errorMessage = errorData ? errorData.message : '예약에 실패했습니다. 다시 시도해주세요.';
            throw new Error(errorMessage);
        }

        alert('상담 예약이 성공적으로 완료되었습니다.');
        window.location.reload(); // 성공 후 페이지 새로고침

    } catch (error) {
        console.error('예약 처리 중 에러:', error);
        alert(error.message);
    }
}