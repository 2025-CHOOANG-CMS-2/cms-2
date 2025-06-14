const sidebar = document.getElementById("sidebar");
const hamburger = document.getElementById("hamburger");
hamburger.addEventListener("click", () => {
  sidebar.classList.toggle("show");
});
const menuLinks = document.querySelectorAll("[data-menu]");
menuLinks.forEach((menu) => {
  menu.addEventListener("click", (e) => {
    e.preventDefault();
    const key = menu.getAttribute("data-menu");
    const submenu = document.getElementById(`submenu-${key}`);

    document.querySelectorAll(".submenu").forEach((sm) => {
      if (sm !== submenu) sm.style.display = "none";
    });

    if (submenu) {
      submenu.style.display =
        submenu.style.display === "block" ? "none" : "block";
    }
  });
});
const staff = [
  {
    id: "EMP001",
    name: "김철수",
    dept: "ADMIN",
    deptName: "행정팀",
    position: "과장",
    status: "Y",
    statusLabel: "재직",
    phone: "010-1111-2222",
    email: "kim@univ.ac.kr",
    hireDate: "2020-03-01",
  },
  {
    id: "EMP002",
    name: "이영희",
    dept: "ACADEMIC",
    deptName: "교무팀",
    position: "대리",
    status: "Y",
    statusLabel: "재직",
    phone: "010-2222-3333",
    email: "lee@univ.ac.kr",
    hireDate: "2021-05-15",
  },
  {
    id: "EMP003",
    name: "박민수",
    dept: "STUDENT",
    deptName: "학생팀",
    position: "주임",
    status: "L",
    statusLabel: "휴직",
    phone: "010-3333-4444",
    email: "park@univ.ac.kr",
    hireDate: "2019-08-20",
  },
  {
    id: "EMP004",
    name: "정하늘",
    dept: "IT",
    deptName: "정보팀",
    position: "사원",
    status: "Y",
    statusLabel: "재직",
    phone: "010-4444-5555",
    email: "jung@univ.ac.kr",
    hireDate: "2022-01-10",
  },
  {
    id: "EMP005",
    name: "최준호",
    dept: "ADMIN",
    deptName: "행정팀",
    position: "부장",
    status: "Y",
    statusLabel: "재직",
    phone: "010-5555-6666",
    email: "choi@univ.ac.kr",
    hireDate: "2018-02-28",
  },
  {
    id: "EMP006",
    name: "한수정",
    dept: "ACADEMIC",
    deptName: "교무팀",
    position: "차장",
    status: "N",
    statusLabel: "퇴직",
    phone: "010-6666-7777",
    email: "han@univ.ac.kr",
    hireDate: "2017-11-05",
  },
];

let currentPage = 1;
const pageSize = 10;
let currentEditingStaff = null;

// 등록 모달 열기 함수 (먼저 정의)
function openAddModal() {
  // 폼 초기화
  document.getElementById("addForm").reset();

  // 등록 모달 열기
  new bootstrap.Modal(document.getElementById("addModal")).show();
}

function renderTable(data) {
  const tbody = document.getElementById("staffTableBody");
  const start = (currentPage - 1) * pageSize;
  const pageData = data.slice(start, start + pageSize);

  tbody.innerHTML = pageData
    .map(
      (s) => `
<tr onclick="showDetail(${JSON.stringify(s).replace(/"/g, "&quot;")})">
  <td>${s.id}</td>
  <td>${s.name}</td>
  <td>${s.deptName}</td>
  <td>${s.position}</td>
  <td>${s.statusLabel}</td>
  <td>${s.phone}</td>
  <td>${s.email}</td>
  <td>
    <button class="btn btn-sm btn-outline-secondary" onclick="event.stopPropagation(); openEditModal(${JSON.stringify(
      s
    ).replace(/"/g, "&quot;")})">
      <i class="bi bi-pencil"></i>
    </button>
  </td>
</tr>`
    )
    .join("");
}

function getStatusBadgeClass(status) {
  switch (status) {
    case "Y":
      return "status-active";
    case "N":
      return "status-inactive";
    case "L":
      return "status-leave";
    default:
      return "status-active";
  }
}

function showDetail(staff) {
  currentEditingStaff = staff;
  const modalBody = document.getElementById("detailContent");
  modalBody.innerHTML = `
    <div class="staff-detail-card">
      <div class="detail-section">
        <div class="section-title">기본 정보</div>
        
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">사번</div>
            <div class="detail-value">${staff.id}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">이름</div>
            <div class="detail-value">${staff.name}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">부서</div>
            <div class="detail-value">${staff.deptName}</div>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">직급</div>
            <div class="detail-value">${staff.position}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">재직상태</div>
            <div class="detail-value">
              <span class="status-badge ${getStatusBadgeClass(
                staff.status
              )}">${staff.statusLabel}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">입사일자</div>
            <div class="detail-value">${staff.hireDate}</div>
          </div>
        </div>
      </div>
      
      <div class="detail-section">
        <div class="section-title">주소 정보</div>
        
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">우편번호</div>
            <div class="detail-value">12345</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">주소</div>
            <div class="detail-value">서울특별시 종로구 대학로</div>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">상세주소</div>
            <div class="detail-value">대한대학교 교직원 숙소 A동 102호</div>
          </div>
        </div>
      </div>
      
      <div class="detail-section">
        <div class="section-title">연락처</div>
        
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">이메일</div>
            <div class="detail-value">${staff.email}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">연락처</div>
            <div class="detail-value">${staff.phone}</div>
          </div>
        </div>
      </div>
    </div>
  `;

  // 수정 버튼 이벤트 설정
  document.getElementById("editFromDetailBtn").onclick = function () {
    openEditModal(currentEditingStaff);
  };

  new bootstrap.Modal(document.getElementById("detailModal")).show();
}

// 수정 모달 열기
function openEditModal(staffMember) {
  currentEditingStaff = staffMember;

  // 폼에 기존 데이터 채우기
  document.getElementById("edit_id").value = staffMember.id;
  document.getElementById("edit_name").value = staffMember.name;
  document.getElementById("edit_dept").value = staffMember.dept;
  document.getElementById("edit_position").value = staffMember.position;
  document.getElementById("edit_status").value = staffMember.status;
  document.getElementById("edit_email").value = staffMember.email;
  document.getElementById("edit_phone").value = staffMember.phone;
  document.getElementById("edit_hire_date").value = staffMember.hireDate;

  // 상세보기 모달이 열려있다면 닫기
  const detailModal = bootstrap.Modal.getInstance(
    document.getElementById("detailModal")
  );
  if (detailModal) {
    detailModal.hide();
  }

  // 수정 모달 열기
  new bootstrap.Modal(document.getElementById("editModal")).show();
}

// 교직원 정보 저장
function saveStaff() {
  const staffId = document.getElementById("edit_id").value;
  const updatedStaff = {
    id: staffId,
    name: document.getElementById("edit_name").value,
    dept: document.getElementById("edit_dept").value,
    deptName: getDeptName(document.getElementById("edit_dept").value),
    position: document.getElementById("edit_position").value,
    status: document.getElementById("edit_status").value,
    statusLabel: getStatusLabel(
      document.getElementById("edit_status").value
    ),
    email: document.getElementById("edit_email").value,
    phone: document.getElementById("edit_phone").value,
    hireDate: document.getElementById("edit_hire_date").value,
  };

  // 배열에서 해당 교직원 찾아서 업데이트
  const staffIndex = staff.findIndex((s) => s.id === staffId);
  if (staffIndex !== -1) {
    staff[staffIndex] = updatedStaff;

    // 테이블 다시 렌더링
    filterStaff();

    // 모달 닫기
    bootstrap.Modal.getInstance(
      document.getElementById("editModal")
    ).hide();

    alert("교직원 정보가 성공적으로 수정되었습니다.");
  }
}

// 부서 코드를 이름으로 변환
function getDeptName(deptCode) {
  const deptMap = {
    ADMIN: "행정팀",
    ACADEMIC: "교무팀",
    STUDENT: "학생팀",
    IT: "정보팀",
  };
  return deptMap[deptCode] || deptCode;
}

// 상태 코드를 라벨로 변환
function getStatusLabel(statusCode) {
  const statusMap = {
    Y: "재직",
    N: "퇴직",
    L: "휴직",
  };
  return statusMap[statusCode] || statusCode;
}

// 교직원 등록
function addStaff() {
  // 폼 데이터 가져오기
  const newStaff = {
    id: document.getElementById("add_id").value.trim(),
    name: document.getElementById("add_name").value.trim(),
    dept: document.getElementById("add_dept").value,
    deptName: getDeptName(document.getElementById("add_dept").value),
    position: document.getElementById("add_position").value,
    status: document.getElementById("add_status").value,
    statusLabel: getStatusLabel(
      document.getElementById("add_status").value
    ),
    email: document.getElementById("add_email").value.trim(),
    phone: document.getElementById("add_phone").value.trim(),
    hireDate: document.getElementById("add_hire_date").value,
  };

  // 유효성 검사
  if (
    !newStaff.id ||
    !newStaff.name ||
    !newStaff.dept ||
    !newStaff.position ||
    !newStaff.status ||
    !newStaff.email ||
    !newStaff.phone ||
    !newStaff.hireDate
  ) {
    alert("모든 필수 정보를 입력해주세요.");
    return;
  }

  // 사번 중복 체크
  if (staff.find((s) => s.id === newStaff.id)) {
    alert("이미 존재하는 사번입니다.");
    return;
  }

  // 교직원 배열에 추가
  staff.push(newStaff);

  // 테이블 다시 렌더링
  filterStaff();

  // 모달 닫기
  bootstrap.Modal.getInstance(document.getElementById("addModal")).hide();

  alert("교직원이 성공적으로 등록되었습니다.");
}

function renderPagination(data) {
  const totalPages = Math.ceil(data.length / pageSize);
  const pagination = document.getElementById("pagination");
  pagination.innerHTML = "";

  for (let i = 1; i <= totalPages; i++) {
    const li = document.createElement("li");
    li.className = "page-item" + (i === currentPage ? " active" : "");
    li.innerHTML = `<button class="page-link">${i}</button>`;
    li.addEventListener("click", () => {
      currentPage = i;
      renderTable(filteredData);
      renderPagination(filteredData);
    });
    pagination.appendChild(li);
  }
}

let filteredData = [...staff];

function filterStaff() {
  const name = document.getElementById("searchName").value.toLowerCase();
  const dept = document.getElementById("searchDept").value;
  const status = document.getElementById("searchStatus").value;

  filteredData = staff.filter(
    (s) =>
      (!name || s.name.toLowerCase().includes(name)) &&
      (!dept || s.dept === dept) &&
      (!status || s.status === status)
  );
  currentPage = 1;
  renderTable(filteredData);
  renderPagination(filteredData);
}

renderTable(filteredData);
renderPagination(filteredData);