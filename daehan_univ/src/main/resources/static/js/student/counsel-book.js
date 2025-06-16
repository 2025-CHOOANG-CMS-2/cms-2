// 전역 변수
const currentDate = new Date()
let selectedDate = null
let selectedTime = null
let selectedCounselor = null

document.addEventListener("DOMContentLoaded", () => {
  initializeCalendar()
  initializeEventListeners()
})

function initializeCalendar() {
  updateCalendarDisplay()
  generateCalendar()
}

function updateCalendarDisplay() {
  const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"]
  document.getElementById("currentMonth").textContent =
    `${currentDate.getFullYear()}년 ${monthNames[currentDate.getMonth()]}`
}

function generateCalendar() {
  const firstDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1)
  const lastDay = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())

  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    const dayEl = document.createElement("div")
    dayEl.className = "calendar-day"
    dayEl.textContent = date.getDate()

    if (date.getMonth() !== currentDate.getMonth()) {
      dayEl.classList.add("unavailable")
    } else if (date >= new Date()) {
      dayEl.classList.add("available")
      dayEl.addEventListener("click", () => selectDate(date))
    } else {
      dayEl.classList.add("unavailable")
    }
    calendarGrid.appendChild(dayEl)
  }
}

function selectDate(date) {
  document.querySelectorAll(".calendar-day.selected").forEach((d) => d.classList.remove("selected"))
  event.target.classList.add("selected")
  selectedDate = date
  updateTimeSlots()
}

function updateTimeSlots() {
  const slots = document.querySelectorAll(".time-slot")
  slots.forEach((slot) => {
    slot.classList.remove("selected", "unavailable")
    slot.replaceWith(slot.cloneNode(true))
  })
  document.querySelectorAll(".time-slot").forEach((slot) => {
    if (Math.random() > 0.3) {
      slot.addEventListener("click", () => selectTime(slot))
    } else {
      slot.classList.add("unavailable")
    }
  })
}

function selectTime(slot) {
  if (slot.classList.contains("unavailable")) return
  document.querySelectorAll(".time-slot.selected").forEach((s) => s.classList.remove("selected"))
  slot.classList.add("selected")
  selectedTime = slot.dataset.time
}

document.querySelectorAll(".counselor-card").forEach((card) => {
  card.addEventListener("click", function () {
    document.querySelectorAll(".counselor-card.selected").forEach((c) => c.classList.remove("selected"))
    this.classList.add("selected")
    selectedCounselor = this.dataset.counselor
  })
})

document.getElementById("prevMonth").addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() - 1)
  updateCalendarDisplay()
  generateCalendar()
})

document.getElementById("nextMonth").addEventListener("click", () => {
  currentDate.setMonth(currentDate.getMonth() + 1)
  updateCalendarDisplay()
  generateCalendar()
})

function initializeEventListeners() {
  document.getElementById("bookingForm").addEventListener("submit", (e) => {
    e.preventDefault()
    submitBooking()
  })

  // 상담 유형에 따라 상담사 필터링
  const typeSelect = document.querySelector('select[name="counselingType"]')
  typeSelect.addEventListener("change", function () {
    const selectedType = this.value
    const counselorSection = document.querySelector(".counselor-section")
    const counselorSelect = document.querySelector(".counselor-select")

    if (!selectedType) {
      counselorSection.style.display = "none"
    } else {
      counselorSection.style.display = "block"
      counselorSelect.classList.add("show")

      document.querySelectorAll(".counselor-card").forEach((card) => {
        if (card.dataset.type === selectedType) {
          card.style.display = "flex"
        } else {
          card.style.display = "none"
        }
        card.classList.remove("selected")
      })
    }
    selectedCounselor = null
  })
}

function submitBooking() {
  if (!selectedDate || !selectedTime || !selectedCounselor) {
    showNotification("날짜, 시간, 상담사를 모두 선택해주세요.", "error")
    return
  }
  const fd = new FormData(document.getElementById("bookingForm"))
  const data = {
    date: selectedDate.toISOString().split("T")[0],
    time: selectedTime,
    counselor: selectedCounselor,
    type: fd.get("counselingType"),
    method: fd.get("counselingMethod"),
    content: fd.get("content"),
    phone: fd.get("phone"),
  }
  console.log("예약 데이터:", data)
  const btn = document.querySelector('#bookingForm button[type="submit"]')
  const txt = btn.innerHTML
  btn.innerHTML = '<div class="loading"></div> 예약 중...'
  btn.disabled = true
  setTimeout(() => {
    btn.innerHTML = txt
    btn.disabled = false
    showNotification("상담 예약이 완료되었습니다!", "success")
    document.getElementById("bookingForm").reset()
    selectedDate = selectedTime = selectedCounselor = null
    document.querySelectorAll(".selected").forEach((el) => el.classList.remove("selected"))
    document.querySelector(".counselor-section").style.display = "none"
  }, 2000)
}

function showNotification(msg, type = "success") {
  const n = document.getElementById("notification")
  n.textContent = msg
  n.className = `notification ${type} show`
  setTimeout(() => n.classList.remove("show"), 3000)
}
