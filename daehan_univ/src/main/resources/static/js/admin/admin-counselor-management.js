let editingCounselorId = null

document.addEventListener("DOMContentLoaded", () => {
  fetchCounselors();	
  initializeEventListeners()
})

// 1. 백엔드 API를 호출해서 상담사 데이터를 가져오는 함수
async function fetchCounselors() {
    try {
        // 우리가 만든 API 주소를 호출합니다.
        const response = await fetch('/api/admin/counselors');
        if (!response.ok) {
            throw new Error('데이터를 불러오는 데 실패했습니다.');
        }
        const counselors = await response.json(); // JSON 데이터를 자바스크립트 객체로 변환
        renderCounselors(counselors); // 받은 데이터로 화면을 그리는 함수 호출
    } catch (error) {
        console.error(error);
        // 사용자에게 에러 알림을 보여줄 수 있습니다.
        alert('상담사 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

// 2. 받아온 데이터로 실제 HTML을 만들어 화면에 그려주는 함수
function renderCounselors(counselors) {
    const grid = document.querySelector('.counselor-grid');
    grid.innerHTML = ''; // 기존에 내용이 있다면 깨끗하게 비웁니다.

    if (counselors.length === 0) {
        grid.innerHTML = '<p>등록된 상담사가 없습니다.</p>';
        return;
    }

    counselors.forEach(counselor => {
        const statusText = counselor.status === 'active' ? '활성' : '비활성';
        
        // JSON 데이터를 사용해 상담사 카드 HTML을 동적으로 생성합니다.
        const cardHTML = `
            <div class="counselor-card-admin" data-status="${counselor.status}" data-specialty="${counselor.specialty}">
                <div class="counselor-header">
                    <div class="counselor-avatar">${counselor.name.charAt(0)}</div>
                    <div class="counselor-basic-info">
                        <h3>${counselor.name}</h3>
                        <p class="counselor-id">ID: ${counselor.counselorId}</p>
                        <span class="status-badge ${counselor.status}">${statusText}</span>
                    </div>
                    <div class="counselor-actions">
                        <button class="btn btn-outline btn-sm" title="수정"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-outline btn-sm" title="상태변경"><i class="fas fa-power-off"></i></button>
                        <button class="btn btn-outline btn-sm" title="삭제"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
                <div class="counselor-details">
                    <div class="detail-row">
                        <span class="label">전문분야:</span>
                        <span class="value">${counselor.specialty}</span>
                    </div>
                    <div class="detail-row">
                        <span class="label">이메일:</span>
                        <span class="value">${counselor.email}</span>
                    </div>
                    <div class="detail-row">
                        <span class="label">연락처:</span>
                        <span class="value">${counselor.phone}</span>
                    </div>
                </div>
                <div class="counselor-stats">
                    <div class="stat-item"><i class="fas fa-comments"></i><span>상담: ${counselor.consultationCount}건</span></div>
                    <div class="stat-item"><i class="fas fa-star"></i><span>평점: ${counselor.averageRating.toFixed(1)}</span></div>
                </div>
            </div>
        `;
        // 생성된 카드를 그리드 영역에 추가합니다.
        grid.insertAdjacentHTML('beforeend', cardHTML);
    });
}

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
