// /js/student-management.js 파일 내용

// ===============================================
// 사이드바 및 서브메뉴 토글 로직
// ===============================================
// DOMContentLoaded 밖으로 이동하여 스크립트 로드 즉시 작동하도록 함
// (원래 코드에서 DOMContentLoaded 밖에 있었으므로 유지)
const sidebar = document.getElementById("sidebar");
const hamburger = document.getElementById("hamburger");
if (hamburger && sidebar) { // 요소가 존재하는지 확인
    hamburger.addEventListener("click", () => {
        // HTML의 사이드바 토글 클래스가 'active' 이므로 'show' 대신 'active' 사용
        sidebar.classList.toggle("active");
    });
}


// ===============================================
// 임시 데이터 (실제 서버 연동 시 제거 또는 테스트용으로 사용)
// ===============================================
const students = [
    {
        id: "20231234", name: "홍길동", dept: "CSE", deptName: "컴퓨터공학과", year: "3",
        status: "ENROLL", statusLabel: "재학", phone: "010-1234-5678", email: "hong@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231235", name: "김영희", dept: "CSE", deptName: "컴퓨터공학과", year: "2",
        status: "LEAVE", statusLabel: "휴학", phone: "010-2345-6789", email: "kim@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231236", name: "이철수", dept: "LAW", deptName: "법학과", year: "1",
        status: "ENROLL", statusLabel: "재학", phone: "010-3456-7890", email: "lee@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231237", name: "박민수", dept: "ME", deptName: "기계공학과", year: "4",
        status: "GRAD", statusLabel: "졸업", phone: "010-4567-8901", email: "park@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231238", name: "최하늘", dept: "CSE", deptName: "컴퓨터공학과", year: "3",
        status: "ENROLL", statusLabel: "재학", phone: "010-5678-9012", email: "choi@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231239", name: "정예린", dept: "LAW", deptName: "법학과", year: "2",
        status: "LEAVE", statusLabel: "휴학", phone: "010-6789-0123", email: "jung@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231240", name: "한유진", dept: "ME", deptName: "기계공학과", year: "1",
        status: "ENROLL", statusLabel: "재학", phone: "010-7890-1234", email: "han@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231241", name: "배지훈", dept: "CSE", deptName: "컴퓨터공학과", year: "4",
        status: "GRAD", statusLabel: "졸업", phone: "010-8901-2345", email: "bae@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231242", name: "장도윤", dept: "LAW", deptName: "법학과", year: "3",
        status: "ENROLL", statusLabel: "재학", phone: "010-9012-3456", email: "jang@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231243", name: "윤태희", dept: "ME", deptName: "기계공학과", year: "2",
        status: "LEAVE", statusLabel: "휴학", phone: "010-0123-4567", email: "yoon@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
    {
        id: "20231244", name: "서지호", dept: "CSE", deptName: "컴퓨터공학과", year: "2",
        status: "ENROLL", statusLabel: "재학", phone: "010-1122-3344", email: "seo@univ.ac.kr",
        admissionDate: "2023-03-02", zipcode: "12345", address: "서울특별시 종로구 대학로", detailAddress: "대한대학교 학생생활관 301호"
    },
];

let currentPage = 1;
const pageSize = 10;
let currentEditingStudent = null; // 현재 수정 중인 학생 데이터를 저장

// ===============================================
// 도우미 함수 (Helper Functions)
// ===============================================

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

// 상태에 따른 배지 클래스 반환 (CSS에 정의된 클래스와 일치해야 함)
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

// ===============================================
// 테이블 렌더링 및 페이지네이션
// ===============================================

function renderTable(data) {
    const tbody = document.getElementById("studentTableBody");
    if (!tbody) {
        console.error("Error: Element with ID 'studentTableBody' not found.");
        return;
    }

    const start = (currentPage - 1) * pageSize;
    const pageData = data.slice(start, start + pageSize);

    tbody.innerHTML = pageData
        .map(
            (s) => `
<tr onclick="showDetail(${JSON.stringify(s).replace(/"/g, "&quot;")})">
  <td>${s.id || ''}</td>
  <td>${s.name || ''}</td>
  <td>${s.deptName || getDeptName(s.dept) || ''}</td> <td>${s.year || ''}</td>
  <td>${s.statusLabel || getStatusLabel(s.status) || ''}</td> <td>${s.phone || ''}</td>
  <td>${s.email || ''}</td>
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

function renderPagination(data) {
    const totalPages = Math.ceil(data.length / pageSize);
    const pagination = document.getElementById("pagination");
    if (!pagination) {
        console.error("Error: Element with ID 'pagination' not found.");
        return;
    }
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

let filteredData = [...students]; // 초기 필터링 데이터는 전체 학생 목록


// ===============================================
// 상세 보기 모달 (`detailModal`)
// ===============================================

function showDetail(student) {
    currentEditingStudent = student; // 현재 보고 있는 학생을 저장하여 수정 버튼에서 활용

    const modalBody = document.getElementById("detailContent");
    if (!modalBody) {
        console.error("Error: Element with ID 'detailContent' not found.");
        return;
    }

    modalBody.innerHTML = `
    <div class="student-detail-card">
      <div class="detail-section">
        <div class="section-title">기본 정보</div>

        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학번</div>
            <div class="detail-value">${student.id || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학생명</div>
            <div class="detail-value">${student.name || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학과명</div>
            <div class="detail-value">${student.deptName || getDeptName(student.dept) || ''}</div>
          </div>
        </div>

        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학년</div>
            <div class="detail-value">${student.year || ''}학년</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">재학여부</div>
            <div class="detail-value">
              <span class="status-badge ${getStatusBadgeClass(
                student.status
            )}">${student.statusLabel || getStatusLabel(student.status) || ''}</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">입학일자</div>
            <div class="detail-value">${student.admissionDate || 'N/A'}</div> </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">주소 정보</div>

        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">우편번호</div>
            <div class="detail-value">${student.zipcode || 'N/A'}</div> </div>
          <div class="detail-item">
            <div class="detail-label">주소</div>
            <div class="detail-value">${student.address || 'N/A'}</div> </div>
        </div>

        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">상세주소</div>
            <div class="detail-value">${student.detailAddress || 'N/A'}</div> </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">연락처</div>

        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">이메일</div>
            <div class="detail-value">${student.email || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">전화번호</div>
            <div class="detail-value">${student.phone || ''}</div>
          </div>
        </div>
      </div>
    </div>
  `;

    // 수정 버튼 이벤트 설정
    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.onclick = function () {
            // 상세 모달을 닫고 수정 모달을 엽니다.
            const detailModalInstance = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
            if (detailModalInstance) {
                detailModalInstance.hide();
            }
            openEditModal(currentEditingStudent);
        };
    }

    new bootstrap.Modal(document.getElementById("detailModal")).show();
}


// ===============================================
// 학생 수정 모달 (`editModal`)
// ===============================================

// 수정 모달 열기
function openEditModal(student) {
    currentEditingStudent = student; // 현재 수정 중인 학생 데이터 저장

    // 폼에 기존 데이터 채우기 (HTML ID와 JS 필드명 매핑)
    document.getElementById("edit_id").value = student.id || '';
    document.getElementById("edit_name").value = student.name || '';
    document.getElementById("edit_dept").value = student.dept || '';
    document.getElementById("edit_year").value = student.year || '';
    document.getElementById("edit_status").value = student.status || '';
    document.getElementById("edit_admission_date").value = student.admissionDate || ''; // HTML에 있지만 JS 데이터에 없던 필드 추가
    document.getElementById("edit_zipcode").value = student.zipcode || ''; // HTML에 있지만 JS 데이터에 없던 필드 추가
    document.getElementById("edit_address").value = student.address || ''; // HTML에 있지만 JS 데이터에 없던 필드 추가
    document.getElementById("edit_address_detail").value = student.detailAddress || ''; // HTML에 있지만 JS 데이터에 없던 필드 추가
    document.getElementById("edit_email").value = student.email || '';
    document.getElementById("edit_phone").value = student.phone || '';


    // 상세보기 모달이 열려있다면 닫기 (선택 사항)
    const detailModal = bootstrap.Modal.getInstance(
        document.getElementById("detailModal")
    );
    if (detailModal) {
        detailModal.hide();
    }

    // 수정 모달 열기
    new bootstrap.Modal(document.getElementById("editModal")).show();
}

async function saveStudent() {
    const studentId = document.getElementById("edit_id").value; 
    const updatedStudentData = {
        // 백엔드 DTO 필드명에 맞춰서 데이터 구성
        // 예: STD_NM, SCSBJT_CD, SCH_YR, ENTR_DT, STD_STAT_CD, STD_ZIP, STD_ADDR, STD_DADDR, STD_TELNO, STD_EML_ADDR
        "STD_NO": studentId, // 학번은 기존 학번으로 보냄 (수정에서는 필요)
        "STD_NM": document.getElementById("edit_name").value,
        "SCSBJT_CD": document.getElementById("edit_dept").value,
        "SCH_YR": parseInt(document.getElementById("edit_year").value),
        "ENTR_DT": document.getElementById("edit_admission_date").value,
        "STD_STAT_CD": document.getElementById("edit_status").value,
        "STD_ZIP": document.getElementById("edit_zipcode").value,
        "STD_ADDR": document.getElementById("edit_address").value,
        "STD_DADDR": document.getElementById("edit_address_detail").value,
        "STD_EML_ADDR": document.getElementById("edit_email").value,
        "STD_TELNO": document.getElementById("edit_phone").value,
        // USER_ID2, USE_YN 등은 필요에 따라 추가
    };

    try {
        // 서버의 PUT 엔드포인트로 요청
        const response = await fetch(`/admin/student/${studentId}`, { // PUT 요청 경로는 그대로 유지
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedStudentData) // 이제 JSON 문자열의 키가 STD_NM, SCSBJT_CD 등이 됨
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 정보 업데이트 실패! (${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json(); // 서버 응답 (업데이트된 학생 정보)
        console.log("학생 정보 업데이트 성공:", result);
        alert("학생 정보가 성공적으로 수정되었습니다.");

        // 모달 닫기
        const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
        if (editModal) {
            editModal.hide();
        }

        // 실제 데이터를 업데이트 (임시 데이터 사용 시)
        // 백엔드 연동 시에는 서버에서 최신 목록을 다시 가져오는 fetchAndRenderStudents() 호출이 더 좋습니다.
        const studentIndex = students.findIndex((s) => s.id === studentId); // id는 여전히 JS 배열의 id 사용
        if (studentIndex !== -1) {
            // 임시 데이터 업데이트 (실제 서버 연동 시에는 이 부분 대신 fetchAndRenderStudents() 호출)
            students[studentIndex] = {
                ...students[studentIndex], // 기존 데이터 유지
                // 업데이트된 필드 덮어쓰기 (DTO 필드명에 맞춰서)
                id: result.STD_NO, // 업데이트 후 학번 다시 설정
                name: result.STD_NM,
                dept: result.SCSBJT_CD,
                deptName: getDeptName(result.SCSBJT_CD),
                year: result.SCH_YR.toString(),
                status: result.STD_STAT_CD,
                statusLabel: getStatusLabel(result.STD_STAT_CD),
                phone: result.STD_TELNO,
                email: result.STD_EML_ADDR,
                admissionDate: result.ENTR_DT,
                zipcode: result.STD_ZIP,
                address: result.STD_ADDR,
                detailAddress: result.STD_DADDR
            };
        }

        // 테이블 새로고침 (필터링된 상태를 유지하면서)
        filterStudents();

    } catch (error) {
        console.error("학생 정보 업데이트 중 오류 발생:", error);
        alert("학생 정보 업데이트 중 오류가 발생했습니다: " + error.message);
    }
}

// ===============================================
// 검색 필터링
// ===============================================

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
    currentPage = 1; // 검색 시 첫 페이지로 이동
    renderTable(filteredData);
    renderPagination(filteredData);
}

// ===============================================
// 학생 등록 모달 (`addModal`) 관련 함수
// ===============================================

// '학생 등록' 버튼 클릭 시 호출될 함수 (모달 열기)
function openAddModal() {
    console.log("학생 등록 모달 열기 요청됨.");
    const addModalElement = document.getElementById('addModal');
    if (addModalElement) {
        const addModal = new bootstrap.Modal(addModalElement);
        addModal.show();

        // 모달이 열릴 때 폼 필드 초기화
        const addForm = document.getElementById('addForm');
        if (addForm) {
            addForm.reset(); // 폼의 모든 입력 필드 초기화
            // HTML에 readonly/value로 설정된 필드도 명시적으로 초기화 (필요 시)
            document.getElementById('add_id').value = "학과 선택 시 자동 생성";
            document.getElementById('add_year').value = "1학년";
            document.getElementById('add_status').value = "재학";
            // 이메일 입력 필드 초기화 (숨겨진 필드 포함)
            document.getElementById('add_email_id').value = "";
            document.getElementById('add_email').value = "";
            document.getElementById('add_dept').value = ""; // 학과 선택 초기화
        }
    } else {
        console.error("Error: 'addModal' element (학생 등록 모달) not found.");
    }
}

async function addStudent() {
    const studentData = {
        "STD_NM": document.getElementById('add_name').value,
        "SCSBJT_CD": document.getElementById('add_dept').value,
        "SCH_YR": parseInt(document.getElementById('add_year').value),
        "ENTR_DT": document.getElementById('add_admission_date').value,
        "STD_STAT_CD": document.getElementById('add_status').value,
        "STD_ZIP": document.getElementById('add_zipcode').value,
        "STD_ADDR": document.getElementById('add_address').value,
        "STD_DADDR": document.getElementById('add_address_detail').value,
        "STD_TELNO": document.getElementById('add_phone').value,
        "STD_EML_ADDR": document.getElementById('add_email').value,

       
        
        "USER_ID2": "user01", // 예: "ADMIN001", "teacher_id", "testuser" 등
        "USE_YN": "Y" 
    };

    try {
        const response = await fetch('/admin/student', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(studentData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 등록 실패! (${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log('학생 등록 성공:', result);
        alert(`학생 ${result.STD_NM}(학번: ${result.STD_NO})이(가) 성공적으로 등록되었습니다!`);

        const addModal = bootstrap.Modal.getInstance(document.getElementById('addModal'));
        if (addModal) {
            addModal.hide();
        }

        students.push({
            id: result.STD_NO,
            name: result.STD_NM,
            dept: result.SCSBJT_CD,
            deptName: getDeptName(result.SCSBJT_CD),
            year: result.SCH_YR.toString(),
            status: result.STD_STAT_CD,
            statusLabel: getStatusLabel(result.STD_STAT_CD),
            phone: result.STD_TELNO,
            email: result.STD_EML_ADDR,
            admissionDate: result.ENTR_DT,
            zipcode: result.STD_ZIP,
            address: result.STD_ADDR,
            detailAddress: result.STD_DADDR
        });
        filterStudents();

    } catch (error) {
        console.error('학생 등록 중 오류 발생:', error);
        alert('학생 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n상세: ' + error.message);
    }
}

// ===============================================
// DOMContentLoaded 이벤트 리스너 (페이지 로드 시 초기화)
// ===============================================
document.addEventListener('DOMContentLoaded', () => {

    // 서브메뉴 토글 기능 (DOMContentLoaded 안으로 이동)
    document.querySelectorAll('.nav-link.has-submenu').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const key = this.getAttribute("data-menu"); // `this` 사용
            const submenu = document.getElementById(`submenu-${key}`);
            if (submenu) {
                // 다른 서브메뉴 닫기
                document.querySelectorAll(".submenu").forEach((sm) => {
                    if (sm !== submenu) sm.style.display = "none";
                });
                // 현재 서브메뉴 토글
                submenu.style.display = submenu.style.display === 'block' ? 'none' : 'block';
            }
        });
    });

    // 이메일 입력 필드 처리: add_email_id 입력 시 add_email hidden 필드 업데이트
    // 등록 모달 HTML에 <input type="text" class="form-control" id="add_email_id" required> 가 없으므로
    // HTML에서 <div class="mb-3"> <label for="add_email" class="form-label">이메일</label> <input type="email" class="form-control form-control-sm" id="add_email" placeholder="이메일을 입력하세요" /> </div>
    // 로 되어있습니다. 이를 바탕으로 add_email 필드만 직접 사용하거나, HTML을 수정하여 add_email_id를 추가해야 합니다.
    // 현재 HTML에 맞춰서 add_email 필드를 직접 사용하고, 이메일 도메인은 백엔드에서 추가하는 것이 더 적절해 보입니다.
    // 만약 @dhuniv.ac.kr 부분을 프론트엔드에서 고정하고 싶다면 HTML의 add_email 필드를 분리해야 합니다.
    // 현재 HTML 기준으로 add_email은 전체 이메일 주소를 입력받는 필드로 가정합니다.
    // 따라서 이메일 분리 로직은 주석 처리합니다. 필요하다면 HTML 수정 후 아래 주석 해제하여 사용하세요.
    /*
    const addEmailIdInput = document.getElementById('add_email_id');
    const addEmailHiddenInput = document.getElementById('add_email'); // 이 필드는 hidden이 아니므로 주의
    if (addEmailIdInput && addEmailHiddenInput) {
        addEmailIdInput.addEventListener('input', function() {
            addEmailHiddenInput.value = this.value + '@dhuniv.ac.kr';
        });
    }
    */

    // 초기 학생 목록 로드 (서버 연동 시 fetchAndRenderStudents() 사용)
    filterStudents(); // 초기 데이터 로드 및 렌더링 (현재는 로컬 students 배열 사용)
});

// 페이지 로드 시 초기 데이터 로드 및 렌더링 (DOMContentLoaded 밖에서 호출)
// filterStudents()가 이미 students 배열을 사용하므로, 이 함수 대신 filterStudents()가 초기 렌더링을 담당합니다.
// fetchAndRenderStudents()는 서버에서 데이터를 가져올 때 사용됩니다.
/*
async function fetchAndRenderStudents() {
    try {
        const response = await fetch('/admin/student'); // GET 요청
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const studentsFromServer = await response.json();
        // 여기서 students 배열을 서버에서 가져온 데이터로 교체하고 필터링/렌더링
        students = studentsFromServer; // 전역 students 변수를 업데이트
        filterStudents(); // 업데이트된 students 배열로 테이블 렌더링 및 페이지네이션
    } catch (error) {
        console.error('학생 목록을 불러오는 중 오류 발생:', error);
        alert('학생 목록을 불러오는데 실패했습니다.');
    }
}
*/
// 현재는 students 배열이 이미 정의되어 있으므로 아래 두 줄로 초기화합니다.
// filterStudents(); // 위 DOMContentLoaded 안에서 호출하도록 이동