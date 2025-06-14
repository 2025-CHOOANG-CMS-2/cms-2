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

const students = [
  {
    id: "20231234",
    name: "홍길동",
    dept: "CSE",
    deptName: "컴퓨터공학과",
    year: "3",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-1234-5678",
    email: "hong@univ.ac.kr",
  },
  {
    id: "20231235",
    name: "김영희",
    dept: "CSE",
    deptName: "컴퓨터공학과",
    year: "2",
    status: "LEAVE",
    statusLabel: "휴학",
    phone: "010-2345-6789",
    email: "kim@univ.ac.kr",
  },
  {
    id: "20231236",
    name: "이철수",
    dept: "LAW",
    deptName: "법학과",
    year: "1",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-3456-7890",
    email: "lee@univ.ac.kr",
  },
  {
    id: "20231237",
    name: "박민수",
    dept: "ME",
    deptName: "기계공학과",
    year: "4",
    status: "GRAD",
    statusLabel: "졸업",
    phone: "010-4567-8901",
    email: "park@univ.ac.kr",
  },
  {
    id: "20231238",
    name: "최하늘",
    dept: "CSE",
    deptName: "컴퓨터공학과",
    year: "3",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-5678-9012",
    email: "choi@univ.ac.kr",
  },
  {
    id: "20231239",
    name: "정예린",
    dept: "LAW",
    deptName: "법학과",
    year: "2",
    status: "LEAVE",
    statusLabel: "휴학",
    phone: "010-6789-0123",
    email: "jung@univ.ac.kr",
  },
  {
    id: "20231240",
    name: "한유진",
    dept: "ME",
    deptName: "기계공학과",
    year: "1",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-7890-1234",
    email: "han@univ.ac.kr",
  },
  {
    id: "20231241",
    name: "배지훈",
    dept: "CSE",
    deptName: "컴퓨터공학과",
    year: "4",
    status: "GRAD",
    statusLabel: "졸업",
    phone: "010-8901-2345",
    email: "bae@univ.ac.kr",
  },
  {
    id: "20231242",
    name: "장도윤",
    dept: "LAW",
    deptName: "법학과",
    year: "3",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-9012-3456",
    email: "jang@univ.ac.kr",
  },
  {
    id: "20231243",
    name: "윤태희",
    dept: "ME",
    deptName: "기계공학과",
    year: "2",
    status: "LEAVE",
    statusLabel: "휴학",
    phone: "010-0123-4567",
    email: "yoon@univ.ac.kr",
  },
  {
    id: "20231244",
    name: "서지호",
    dept: "CSE",
    deptName: "컴퓨터공학과",
    year: "2",
    status: "ENROLL",
    statusLabel: "재학",
    phone: "010-1122-3344",
    email: "seo@univ.ac.kr",
  },
];

let currentPage = 1;
const pageSize = 10;
let currentEditingStudent = null;

function renderTable(data) {
  const tbody = document.getElementById("studentTableBody");
  const start = (currentPage - 1) * pageSize;
  const pageData = data.slice(start, start + pageSize);

  tbody.innerHTML = pageData
    .map(
      (s) => `
<tr onclick="showDetail(${JSON.stringify(s).replace(/"/g, "&quot;")})">
  <td>${s.id}</td>
  <td>${s.name}</td>
  <td>${s.deptName}</td>
  <td>${s.year}</td>
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
    case "ENROLL":
      return "status-enroll";
    case "LEAVE":
      return "status-leave";
    case "GRAD":
      return "status-grad";
    default:
      return "status-enroll";
  }
}

function showDetail(student) {
  currentEditingStudent = student;
  const modalBody = document.getElementById("detailContent");
  modalBody.innerHTML = `
    <div class="student-detail-card">
      <div class="detail-section">
        <div class="section-title">기본 정보</div>
        
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학번</div>
            <div class="detail-value">${student.id}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학생명</div>
            <div class="detail-value">${student.name}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학과명</div>
            <div class="detail-value">${student.deptName}</div>
          </div>
        </div>
        
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학년</div>
            <div class="detail-value">${student.year}학년</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">재학여부</div>
            <div class="detail-value">
              <span class="status-badge ${getStatusBadgeClass(
                student.status
              )}">${student.statusLabel}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">입학일자</div>
            <div class="detail-value">2023-03-02</div>
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
            <div class="detail-value">대한대학교 학생생활관 301호</div>
          </div>
        </div>
      </div>
      
      <div class="detail-section">
        <div class="section-title">연락처</div>
        
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">이메일</div>
            <div class="detail-value">${student.email}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">전화번호</div>
            <div class="detail-value">${student.phone}</div>
          </div>
        </div>
      </div>
    </div>
  `;

  // 수정 버튼 이벤트 설정
  document.getElementById("editFromDetailBtn").onclick = function () {
    openEditModal(currentEditingStudent);
  };

  new bootstrap.Modal(document.getElementById("detailModal")).show();
}

// 수정 모달 열기
function openEditModal(student) {
  currentEditingStudent = student;

  // 폼에 기존 데이터 채우기
  document.getElementById("edit_id").value = student.id;
  document.getElementById("edit_name").value = student.name;
  document.getElementById("edit_dept").value = student.dept;
  document.getElementById("edit_year").value = student.year;
  document.getElementById("edit_status").value = student.status;
  document.getElementById("edit_email").value = student.email;
  document.getElementById("edit_phone").value = student.phone;

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

// 학생 정보 저장
function saveStudent() {
  const studentId = document.getElementById("edit_id").value;
  const updatedStudent = {
    id: studentId,
    name: document.getElementById("edit_name").value,
    dept: document.getElementById("edit_dept").value,
    deptName: getDeptName(document.getElementById("edit_dept").value),
    year: document.getElementById("edit_year").value,
    status: document.getElementById("edit_status").value,
    statusLabel: getStatusLabel(
      document.getElementById("edit_status").value
    ),
    email: document.getElementById("edit_email").value,
    phone: document.getElementById("edit_phone").value,
  };

  // 배열에서 해당 학생 찾아서 업데이트
  const studentIndex = students.findIndex((s) => s.id === studentId);
  if (studentIndex !== -1) {
    students[studentIndex] = updatedStudent;

    // 테이블 다시 렌더링
    filterStudents();

    // 모달 닫기
    bootstrap.Modal.getInstance(
      document.getElementById("editModal")
    ).hide();

    alert("학생 정보가 성공적으로 수정되었습니다.");
  }
}

// 학과 코드를 이름으로 변환
function getDeptName(deptCode) {
  const deptMap = {
    CSE: "컴퓨터공학과",
    ME: "기계공학과",
    LAW: "법학과",
  };
  return deptMap[deptCode] || deptCode;
}

// 상태 코드를 라벨로 변환
function getStatusLabel(statusCode) {
  const statusMap = {
    ENROLL: "재학",
    LEAVE: "휴학",
    GRAD: "졸업",
  };
  return statusMap[statusCode] || statusCode;
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

let filteredData = [...students];

function filterStudents() {
  const name = document.getElementById("searchName").value.toLowerCase();
  const dept = document.getElementById("searchDept").value;
  const status = document.getElementById("searchStatus").value;

  filteredData = students.filter(
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