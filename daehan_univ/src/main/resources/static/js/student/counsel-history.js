document.addEventListener("DOMContentLoaded", () => {
  initializeEventListeners()
  initializeTabs()
})

function initializeTabs() {
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      switchTab(this.dataset.tab)
    })
  })
}

function switchTab(tabId) {
  document.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"))
  document.querySelectorAll(".tab-content").forEach((c) => c.classList.remove("active"))
  document.querySelector(`[data-tab="${tabId}"]`).classList.add("active")
  document.getElementById(tabId).classList.add("active")
}

function initializeEventListeners() {
  // 예약 현황 탭 필터
  document.getElementById("periodFilter").addEventListener("change", filterAppointments)
  document.getElementById("statusFilter").addEventListener("change", filterAppointments)
  document.getElementById("typeFilter").addEventListener("change", filterAppointments)

  // 상담 결과 탭 필터
  document.getElementById("resultsPeriodFilter").addEventListener("change", filterResults)
  document.getElementById("resultsTypeFilter").addEventListener("change", filterResults)
}

function filterAppointments() {
  const period = document.getElementById("periodFilter").value
  const status = document.getElementById("statusFilter").value
  const type = document.getElementById("typeFilter").value
  console.log("예약 필터 적용:", { period, status, type })

  const appointmentsTab = document.getElementById("appointments")
  appointmentsTab.querySelectorAll(".history-item").forEach((item) => {
    let show = true
    if (status !== "all" && !item.querySelector(".history-status").classList.contains(`status-${status}`)) {
      show = false
    }
    item.style.display = show ? "block" : "none"
  })
}

function filterResults() {
  const period = document.getElementById("resultsPeriodFilter").value
  const type = document.getElementById("resultsTypeFilter").value
  console.log("결과 필터 적용:", { period, type })

  const resultsTab = document.getElementById("results")
  resultsTab.querySelectorAll(".history-item").forEach((item) => {
    let show = true
    // 결과 탭에서는 완료된 상담만 표시
    if (!item.querySelector(".history-status").classList.contains("status-completed")) {
      show = false
    }
    item.style.display = show ? "block" : "none"
  })
}

function viewCounselingDetail(id) {
  showNotification("상담 상세 내용을 확인합니다.", "success")
}

function downloadReport(id) {
  showNotification("상담 결과 PDF를 다운로드합니다.", "success")
}

function modifyBooking(id) {
  showNotification("예약 변경 페이지로 이동합니다.", "success")
}

function cancelBooking(id) {
  if (confirm("정말로 예약을 취소하시겠습니까?")) {
    showNotification("예약이 취소되었습니다.", "success")
  }
}

function addToCalendar(id) {
  showNotification("캘린더에 일정이 추가되었습니다.", "success")
}

function rebookCounseling(id) {
  window.location.href = "counsel-book.html"
  showNotification("예약 페이지로 이동합니다.", "success")
}

function showNotification(msg, type = "success") {
  const n = document.getElementById("notification")
  n.textContent = msg
  n.className = `notification ${type} show`
  setTimeout(() => n.classList.remove("show"), 3000)
}
