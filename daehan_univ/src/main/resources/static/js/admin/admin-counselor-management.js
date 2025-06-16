// 전역 변수로 현재 수정 중인 상담사 ID와 미등록 교직원 목록을 관리합니다.
let editingCounselorId = null;
let unregisteredEmployees = [];

// 페이지가 처음 로드될 때 실행됩니다.
document.addEventListener("DOMContentLoaded", () => {
    fetchCounselors();
    initializeEventListeners();
});

// ### 1. Read (목록 조회) ###
async function fetchCounselors() {
    try {
        const response = await fetch('/api/admin/counselors');
        if (!response.ok) throw new Error('데이터 로딩 실패');
        const counselors = await response.json();
        renderCounselors(counselors);
    } catch (error) {
        console.error(error);
        alert('상담사 목록을 불러오는 중 오류가 발생했습니다.');
    }
}

function renderCounselors(counselors) {
    const grid = document.querySelector('.counselor-grid');
    grid.innerHTML = ''; // 기존 목록을 깨끗하게 비웁니다.

    if (counselors.length === 0) {
        grid.innerHTML = '<p>등록된 상담사가 없습니다.</p>';
        return;
    }

    counselors.forEach(counselor => {
        const statusText = counselor.status === 'active' ? '활성' : '비활성';
        const cardHTML = `
            <div class="counselor-card-admin" data-counselor-id="${counselor.counselorId}" data-status="${counselor.status}" data-specialty="${counselor.specialty}">
                <div class="counselor-header">
                    <div class="counselor-avatar">${counselor.name.charAt(0)}</div>
                    <div class="counselor-basic-info">
                        <h3>${counselor.name}</h3>
                        <p class="counselor-id">ID: ${counselor.counselorId}</p>
                        <span class="status-badge ${counselor.status}">${statusText}</span>
                    </div>
                    <div class="counselor-actions">
                        <button onclick="showEditCounselorModal('${counselor.counselorId}')" class="btn btn-outline btn-sm" title="수정"><i class="fas fa-edit"></i></button>
                        <button onclick="toggleCounselorStatus('${counselor.counselorId}', '${counselor.status}')" class="btn btn-outline btn-sm" title="상태변경"><i class="fas fa-power-off"></i></button>
                        <button onclick="deleteCounselor('${counselor.counselorId}')" class="btn btn-outline btn-sm" title="삭제"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
                <div class="counselor-details">
                    <div class="detail-row"><span class="label">전문분야:</span><span class="value">${counselor.specialty}</span></div>
                    <div class="detail-row"><span class="label">이메일:</span><span class="value">${counselor.email}</span></div>
                    <div class="detail-row"><span class="label">연락처:</span><span class="value">${counselor.phone}</span></div>
                </div>
                <div class="counselor-stats">
                    <div class="stat-item"><i class="fas fa-comments"></i><span>상담: ${counselor.consultationCount}건</span></div>
                    <div class="stat-item"><i class="fas fa-star"></i><span>평점: ${counselor.averageRating.toFixed(1)}</span></div>
                </div>
            </div>
        `;
        grid.insertAdjacentHTML('beforeend', cardHTML);
    });
}

// ### 2. Create (상담사 추가) ###
async function showAddCounselorModal() {
    editingCounselorId = null;
    document.getElementById("counselorForm").reset();
    document.getElementById("modalTitle").textContent = "상담사 추가";

    const select = document.getElementById('counselorEmplNo');
    select.innerHTML = '<option value="">선택해주세요</option>';
    select.disabled = false;
    document.getElementById('counselorName').readOnly = true;
    document.getElementById('counselorEmail').readOnly = true;
    document.getElementById('counselorPhone').readOnly = true;


    try {
        const response = await fetch('/api/admin/counselors/unregistered');
        if (!response.ok) throw new Error('교직원 목록 로딩 실패');
        
        unregisteredEmployees = await response.json();
        
        unregisteredEmployees.forEach(emp => {
            const option = `<option value="${emp.emplNo}">${emp.emplNm} (${emp.emplNo})</option>`;
            select.insertAdjacentHTML('beforeend', option);
        });

    } catch (error) {
        console.error(error);
        alert(error.message);
    }

    showModal("counselorModal");
}

function handleEmployeeSelect(event) {
    const selectedEmplNo = event.target.value;
    const selectedEmp = unregisteredEmployees.find(emp => emp.emplNo === selectedEmplNo);

    if (selectedEmp) {
        document.getElementById('counselorName').value = selectedEmp.emplNm;
        document.getElementById('counselorEmail').value = selectedEmp.emplEmailAddr;
        document.getElementById('counselorPhone').value = selectedEmp.emplTelno;
    } else {
        document.getElementById('counselorName').value = '';
        document.getElementById('counselorEmail').value = '';
        document.getElementById('counselorPhone').value = '';
    }
}

// ### 3. Update (수정) ###
async function showEditCounselorModal(counselorId) {
    editingCounselorId = counselorId;
    document.getElementById("modalTitle").textContent = "상담사 정보 수정";

    try {
        const response = await fetch(`/api/admin/counselors/${counselorId}`);
        if(!response.ok) throw new Error('상담사 정보 로딩 실패');
        const data = await response.json();

        const select = document.getElementById('counselorEmplNo');
        select.innerHTML = `<option value="${data.counselorId}">${data.name} (${data.counselorId})</option>`;
        select.disabled = true;
        
        document.getElementById('counselorName').value = data.name;
        document.getElementById('counselorEmail').value = data.email;
        document.getElementById('counselorPhone').value = data.phone;
        document.getElementById('counselorSpecialty').value = data.specialty;
        document.getElementById('counselorBio').value = data.intro;
        document.getElementById('counselorStatus').value = data.status;
        
        showModal("counselorModal");
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

// ### Create & Update 로직 통합 (저장) ###
async function saveCounselor() {
    const form = document.getElementById("counselorForm");
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const isEditing = !!editingCounselorId;
    
    // 수정 시에는 emplNo를 보내지 않으므로, 생성 시에만 DTO에 포함합니다.
    const counselorData = {
        cnslSpec: document.getElementById('counselorSpecialty').value,
        intro: document.getElementById('counselorBio').value,
        isActive: document.getElementById('counselorStatus').value === 'active'
    };

    if (!isEditing) {
        counselorData.emplNo = document.getElementById('counselorEmplNo').value;
    }

    const url = isEditing ? `/api/admin/counselors/${editingCounselorId}` : '/api/admin/counselors';
    const method = isEditing ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(counselorData)
        });

        if (!response.ok) {
            throw new Error(isEditing ? '수정에 실패했습니다.' : '추가에 실패했습니다.');
        }

        showNotification(isEditing ? '상담사 정보가 수정되었습니다.' : '새 상담사가 추가되었습니다.', 'success');
        closeModal("counselorModal");
        fetchCounselors();
    } catch (error) {
        console.error(error);
        showNotification(error.message, 'error');
    }
}

// ### 4. Delete (삭제) & 기타 기능 ###
async function deleteCounselor(counselorId) {
    if (confirm("정말로 이 상담사를 삭제하시겠습니까?")) {
        try {
            const response = await fetch(`/api/admin/counselors/${counselorId}`, { method: 'DELETE' });
            if (!response.ok) throw new Error('삭제에 실패했습니다.');
            showNotification("상담사가 삭제되었습니다.", "success");
            fetchCounselors();
        } catch (error) {
            console.error(error);
            showNotification(error.message, 'error');
        }
    }
}

async function toggleCounselorStatus(counselorId, currentStatus) {
    const newStatus = currentStatus === 'active' ? false : true; // 서버에 보낼 boolean 값
    const newStatusText = newStatus ? '활성화' : '비활성화';

    if (confirm(`이 상담사를 ${newStatusText}하시겠습니까?`)) {
        try {
            // 1. 백엔드에 PATCH 요청 보내기
            const response = await fetch(`/api/admin/counselors/${counselorId}/status`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ isActive: newStatus }) // 요청 DTO 형식에 맞게 데이터 전송
            });

            if (!response.ok) {
                throw new Error('상태 변경에 실패했습니다.');
            }

            // 2. 성공 시 처리
            showNotification(`상담사가 ${newStatusText}되었습니다.`, 'success');
            fetchCounselors(); // 목록을 새로고침하여 변경된 상태를 즉시 반영

        } catch (error) {
            console.error(error);
            showNotification(error.message, 'error');
        }
    }
}

function initializeEventListeners() {
    document.getElementById("searchInput").addEventListener("input", filterCounselors);
    document.getElementById("statusFilter").addEventListener("change", filterCounselors);
    document.getElementById("specialtyFilter").addEventListener("change", filterCounselors);
    document.getElementById("counselorEmplNo").addEventListener("change", handleEmployeeSelect);
}

function filterCounselors() {
    const searchTerm = document.getElementById("searchInput").value.toLowerCase();
    const statusFilter = document.getElementById("statusFilter").value;
    const specialtyFilter = document.getElementById("specialtyFilter").value;
    const counselorCards = document.querySelectorAll(".counselor-card-admin");

    counselorCards.forEach(card => {
        const name = card.querySelector("h3").textContent.toLowerCase();
        const specialty = card.dataset.specialty;
        const status = card.dataset.status;
        
        const showBySearch = searchTerm ? name.includes(searchTerm) : true;
        const showByStatus = statusFilter === "all" || status === statusFilter;
        const showBySpecialty = specialtyFilter === "all" || specialty === specialty;

        card.style.display = showBySearch && showByStatus && showBySpecialty ? "flex" : "none";
    });
}

function showModal(modalId) { document.getElementById(modalId).classList.add("show"); }
function closeModal(modalId) { document.getElementById(modalId).classList.remove("show"); }
function showNotification(msg, type = "success") {
    const n = document.getElementById("notification");
    n.textContent = msg;
    n.className = `notification ${type} show`;
    setTimeout(() => n.classList.remove("show"), 3000);
}