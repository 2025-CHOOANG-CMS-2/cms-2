let editingCounselorId = null

document.addEventListener("DOMContentLoaded", () => {
  initializeEventListeners()
})

function initializeEventListeners() {
  // 검색 기능
  document.getElementById("searchInput").addEventListener("input", filterCounselors)

  // 필터 기능
  document.getElementById("statusFilter").addEventListener("change", filterCounselors)
  document.getElementById("specialtyFilter").addEventListener("change", filterCounselors)
}

function filterCounselors() {
  const searchTerm = document.getElementById("searchInput").value.toLowerCase()
  const statusFilter = document.getElementById("statusFilter").value
  const specialtyFilter = document.getElementById("specialtyFilter").value

  const counselorCards = document.querySelectorAll(".counselor-card-admin")

  counselorCards.forEach((card) => {
    const name = card.querySelector("h3").textContent.toLowerCase()
    const specialty = card.dataset.specialty
    const status = card.dataset.status

    let show = true

    // 검색어 필터
    if (searchTerm && !name.includes(searchTerm)) {
      show = false
    }

    // 상태 필터
    if (statusFilter !== "all" && status !== statusFilter) {
      show = false
    }

    // 전문분야 필터
    if (specialtyFilter !== "all" && specialty !== specialtyFilter) {
      show = false
    }

    card.style.display = show ? "block" : "none"
  })
}

function showAddCounselorModal() {
  editingCounselorId = null
  document.getElementById("modalTitle").textContent = "상담사 추가"
  document.getElementById("counselorForm").reset()
  showModal("counselorModal")
}

function editCounselor(counselorId) {
  editingCounselorId = counselorId
  document.getElementById("modalTitle").textContent = "상담사 정보 수정"

  // 기존 데이터 로드 (실제로는 서버에서 가져와야 함)
  const counselorData = getCounselorData(counselorId)

  document.getElementById("counselorName").value = counselorData.name
  document.getElementById("counselorEmail").value = counselorData.email
  document.getElementById("counselorPhone").value = counselorData.phone
  document.getElementById("counselorExperience").value = counselorData.experience
  document.getElementById("counselorSpecialty").value = counselorData.specialty
  document.getElementById("counselorBio").value = counselorData.bio
  document.getElementById("counselorStatus").value = counselorData.status

  showModal("counselorModal")
}

function getCounselorData(counselorId) {
  // 실제로는 서버에서 데이터를 가져와야 함
  const mockData = {
    C001: {
      name: "김상담",
      email: "kim.counselor@edu.ac.kr",
      phone: "010-1234-5678",
      experience: 10,
      specialty: "academic",
      bio: "학업 상담 전문가로 10년간 학생들의 학습 방법 개선과 성취도 향상을 도와왔습니다.",
      status: "active",
    },
    C002: {
      name: "이상담",
      email: "lee.counselor@edu.ac.kr",
      phone: "010-2345-6789",
      experience: 8,
      specialty: "career",
      bio: "진로 상담 전문가로 학생들의 진로 탐색과 설계를 지원합니다.",
      status: "active",
    },
  }

  return mockData[counselorId] || {}
}

function saveCounselor() {
  const form = document.getElementById("counselorForm")

  if (!form.checkValidity()) {
    form.reportValidity()
    return
  }

  const counselorData = {
    name: document.getElementById("counselorName").value,
    email: document.getElementById("counselorEmail").value,
    phone: document.getElementById("counselorPhone").value,
    experience: document.getElementById("counselorExperience").value,
    specialty: document.getElementById("counselorSpecialty").value,
    bio: document.getElementById("counselorBio").value,
    status: document.getElementById("counselorStatus").value,
  }

  console.log("저장할 상담사 데이터:", counselorData)

  if (editingCounselorId) {
    showNotification("상담사 정보가 수정되었습니다.", "success")
    updateCounselorCard(editingCounselorId, counselorData)
  } else {
    showNotification("새 상담사가 추가되었습니다.", "success")
    addCounselorCard(counselorData)
  }

  closeModal("counselorModal")
}

function updateCounselorCard(counselorId, data) {
  // 실제로는 서버 API 호출 후 페이지 새로고침 또는 동적 업데이트
  console.log(`상담사 ${counselorId} 정보 업데이트:`, data)
}

function addCounselorCard(data) {
  // 실제로는 서버 API 호출 후 페이지 새로고침 또는 동적 추가
  console.log("새 상담사 추가:", data)
}

function toggleCounselorStatus(counselorId) {
  const card = document.querySelector(`[data-status]`)
  const currentStatus = card.dataset.status
  const newStatus = currentStatus === "active" ? "inactive" : "active"

  if (confirm(`이 상담사를 ${newStatus === "active" ? "활성화" : "비활성화"}하시겠습니까?`)) {
    card.dataset.status = newStatus
    const statusBadge = card.querySelector(".status-badge")
    statusBadge.className = `status-badge ${newStatus}`
    statusBadge.textContent = newStatus === "active" ? "활성" : "비활성"

    showNotification(`상담사가 ${newStatus === "active" ? "활성화" : "비활성화"}되었습니다.`, "success")

    // 실제로는 서버 API 호출
    console.log(`상담사 ${counselorId} 상태 변경: ${newStatus}`)
  }
}

function deleteCounselor(counselorId) {
  if (confirm("정말로 이 상담사를 삭제하시겠습니까?\n삭제된 데이터는 복구할 수 없습니다.")) {
    showNotification("상담사가 삭제되었습니다.", "success")

    // 카드 제거
    const card = document.querySelector(`[onclick*="${counselorId}"]`).closest(".counselor-card-admin")
    if (card) {
      card.remove()
    }

    // 실제로는 서버 API 호출
    console.log(`상담사 ${counselorId} 삭제`)
  }
}

function showModal(modalId) {
  document.getElementById(modalId).classList.add("show")
}

function closeModal(modalId) {
  document.getElementById(modalId).classList.remove("show")
}

function showNotification(msg, type = "success") {
  const n = document.getElementById("notification")
  n.textContent = msg
  n.className = `notification ${type} show`
  setTimeout(() => n.classList.remove("show"), 3000)
}
