// /js/employee-management.js 파일 내용

// ===============================================
// 사이드바 및 서브메뉴 토글 로직
// ===============================================
document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById("sidebar");
    const hamburger = document.getElementById("hamburger");
    if (hamburger && sidebar) {
        hamburger.addEventListener("click", () => {
            sidebar.classList.toggle("show");
        });
    }

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

    populateDeptDropdowns(); // 페이지 로드 시 부서 드롭다운 먼저 채우기
    fetchEmployees(0); // 초기 교직원 목록 로드

    // 검색 버튼 클릭 이벤트 리스너 연결
    const filterButton = document.querySelector('button[onclick="filterEmployees()"]');
    if (filterButton) {
        filterButton.addEventListener("click", filterEmployees);
    } else {
        console.warn("경고: 'filterEmployees()' onclick 속성을 가진 검색 버튼을 찾을 수 없습니다. HTML을 확인해주세요.");
    }

    // 상세보기 모달에서 수정 버튼 클릭 시 수정 모달 열기
    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.addEventListener('click', function() {
            if (currentEditingEmployee) {
                const detailModalInstance = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
                if (detailModalInstance) {
                    detailModalInstance.hide();
                }
                openEditEmployeeModal(currentEditingEmployee);
            }
        });
    }
});


// ===============================================
// 전역 변수 및 부서/상태 데이터 정의
// ===============================================
let currentPage = 0; // 페이지 번호 (0부터 시작)
const pageSize = 10; // 페이지당 항목 수
let currentEditingEmployee = null; // 현재 수정 중인 교직원 정보 객체

// 부서 목록 데이터
const DEPT_LIST = [
    { code: "101", name: "교무처" }, { code: "102", name: "학생처" },
    { code: "103", name: "입학처" }, { code: "104", name: "총무처" },
    { code: "105", name: "기획처" }, { code: "106", name: "산학협력단" },
    { code: "107", name: "도서관" }, { code: "108", name: "전산정보원" },
    { code: "109", name: "국제교류처" }, { code: "110", name: "연구처" },
    { code: "111", name: "진로취창업팀" }
];

// 재직 상태 코드와 라벨 매핑
const STATUS_MAP = {
  "Y": "재직", "N": "퇴직", "L": "휴직"
};

// ===============================================
// 도우미 함수 (Helper Functions)
// ===============================================

/**
 * 부서 코드를 부서 이름으로 변환합니다.
 */
function getDeptName(deptCode) {
    const foundDept = DEPT_LIST.find(dept => dept.code === deptCode);
    return foundDept ? foundDept.name : deptCode;
}

/**
 * 재직 상태 코드를 한글 라벨로 변환합니다.
 */
function getStatusLabel(statusCode) {
  return STATUS_MAP[statusCode] || statusCode;
}

/**
 * 상태 코드에 따른 Bootstrap 배지 클래스를 반환합니다.
 */
function getStatusBadgeClass(status) {
  switch (status) {
    case "Y": return "status-active";
    case "N": return "status-inactive";
    case "L": return "status-leave";
    default: return "status-active";
  }
}

/**
 * 페이지 내의 모든 부서 드롭다운을 동적으로 채웁니다.
 */
function populateDeptDropdowns() {
    const searchDeptSelect = document.getElementById('searchDept');
    const addDeptSelect = document.getElementById('add_dept');
    const editDeptSelect = document.getElementById('edit_dept');

    [searchDeptSelect, addDeptSelect, editDeptSelect].forEach(selectElement => {
        if (selectElement) {
            selectElement.innerHTML = ''; // 기존 옵션 제거
            const defaultOptionText = (selectElement.id === 'searchDept') ? '부서 전체' : '부서를 선택하세요';
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = defaultOptionText;
            selectElement.appendChild(defaultOption);

            DEPT_LIST.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.code;
                option.textContent = dept.name;
                selectElement.appendChild(option);
            });
        }
    });
}

// ===============================================
// 데이터 로드, 테이블 렌더링 및 페이지네이션 핵심 로직
// ===============================================

/**
 * 백엔드 API에서 교직원 목록을 비동기적으로 가져와 테이블과 페이지네이션 UI를 업데이트합니다.
 */
async function fetchEmployees(page = 0, size = pageSize, searchName = '', searchDept = '', searchStatus = '') {
    currentPage = page;

    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    if (searchName) params.append('searchName', searchName.trim());
    if (searchDept) params.append('searchDept', searchDept);
    if (searchStatus) params.append('searchStatus', searchStatus);

    const apiUrl = `/admin/employee?${params.toString()}`;

    try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP 오류! 상태: ${response.status}, 메시지: ${errorText}`);
        }
        const pageData = await response.json();

        const employeeList = pageData.content;
        const totalPages = pageData.totalPages;
        const currentPageFromBackend = pageData.number;

        const employeeTableBody = document.getElementById('employeeTableBody');
        if (!employeeTableBody) {
            console.error("오류: 'employeeTableBody' 요소를 찾을 수 없습니다.");
            return;
        }
        employeeTableBody.innerHTML = '';

        if (employeeList.length === 0) {
            employeeTableBody.innerHTML = '<tr><td colspan="8">등록된 교직원 정보가 없습니다.</td></tr>';
        } else {
            const rowsHtml = employeeList.map(e => {
                const employeeJsonString = JSON.stringify(e).replace(/"/g, "&quot;"); // HTML 속성에 삽입하기 위해 이스케이프
                return `
                    <tr onclick="showEmployeeDetail(${employeeJsonString})">
                        <td>${e.STAFF_NO || ''}</td>
                        <td>${e.STAFF_NM || ''}</td>
                        <td>${getDeptName(e.DEPT_CD) || ''}</td>
                        <td>${e.POSITION_CD || ''}</td>
                        <td><span class="badge ${getStatusBadgeClass(e.STATUS_CD)}">${getStatusLabel(e.STATUS_CD) || ''}</span></td>
                        <td>${e.STAFF_TELNO || ''}</td>
                        <td>${e.STAFF_EML_ADDR || ''}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-secondary" onclick="event.stopPropagation(); openEditEmployeeModal(${employeeJsonString})">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="event.stopPropagation(); deleteEmployee('${e.STAFF_NO}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');
            employeeTableBody.innerHTML = rowsHtml;
        }
        updatePagination(totalPages, currentPageFromBackend);

    } catch (error) {
        console.error('교직원 목록을 가져오는 중 오류 발생:', error);
        alert('교직원 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요: ' + error.message);
    }
}

/**
 * 검색 필드의 값을 가져와 교직원 목록을 새로 필터링하고 첫 페이지부터 표시합니다.
 */
function filterEmployees() {
    const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
    const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
    const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
    fetchEmployees(0, pageSize, searchName, searchDept, searchStatus);
}

/**
 * 페이지네이션 UI를 업데이트합니다.
 */
function updatePagination(totalPages, currentPage) {
    const paginationUl = document.getElementById('pagination');
    paginationUl.innerHTML = '';

    const maxPagesToShow = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

    if (endPage - startPage + 1 < maxPagesToShow && totalPages > maxPagesToShow) {
        if (currentPage < Math.floor(maxPagesToShow / 2)) {
            endPage = maxPagesToShow - 1;
        } else if (currentPage > totalPages - 1 - Math.floor(maxPagesToShow / 2)) {
            startPage = totalPages - maxPagesToShow;
        }
    }

    const appendPageItem = (page, text, isDisabled = false, isActive = false) => {
        const li = document.createElement('li');
        li.classList.add('page-item');
        if (isDisabled) li.classList.add('disabled');
        if (isActive) li.classList.add('active');
        const link = document.createElement('a');
        link.classList.add('page-link');
        link.href = "#";
        link.innerHTML = text;
        if (!isDisabled) {
            link.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchEmployees(page, pageSize, name, dept, status);
            };
        }
        li.appendChild(link);
        paginationUl.appendChild(li);
    };

    // 이전 버튼
    appendPageItem(currentPage - 1, '&laquo;', currentPage === 0);

    // 첫 페이지로 가는 버튼 및 말줄임표
    if (startPage > 0) {
        appendPageItem(0, '1');
        if (startPage > 1) {
            appendPageItem(-1, '...', true); // Disabled ellipsis
        }
    }

    // 페이지 번호 버튼
    for (let i = startPage; i <= endPage; i++) {
        appendPageItem(i, (i + 1).toString(), false, i === currentPage);
    }

    // 마지막 페이지로 가는 버튼 및 말줄임표
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            appendPageItem(-1, '...', true); // Disabled ellipsis
        }
        appendPageItem(totalPages - 1, totalPages.toString());
    }

    // 다음 버튼
    appendPageItem(currentPage + 1, '&raquo;', currentPage === totalPages - 1);
}

/**
 * 현재 검색 필드의 값을 가져오는 헬퍼 함수
 */
function getCurrentSearchParams() {
    const name = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
    const dept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
    const status = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
    return { name, dept, status };
}

// ===============================================
// 상세 보기 모달 (`detailModal`) 관련 함수
// ===============================================

/**
 * 교직원 상세 정보를 모달에 표시합니다.
 * @param {object} employee - 표시할 교직원 정보 객체
 */
function showEmployeeDetail(employee) {
    currentEditingEmployee = employee; // 현재 상세 보거나 수정할 교직원 정보 저장

    const modalBody = document.getElementById("detailContent");
    if (!modalBody) {
        console.error("오류: 'detailContent' 요소를 찾을 수 없습니다.");
        return;
    }

    // 교직원 상세 정보 HTML 구성
    modalBody.innerHTML = `
    <div class="employee-detail-card">
      <div class="detail-section">
        <div class="section-title">기본 정보</div>
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">사번</div>
            <div class="detail-value">${employee.STAFF_NO || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">이름</div>
            <div class="detail-value">${employee.STAFF_NM || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">부서</div>
            <div class="detail-value">${getDeptName(employee.DEPT_CD) || ''}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">직급</div>
            <div class="detail-value">${employee.POSITION_CD || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">재직상태</div>
            <div class="detail-value">
              <span class="badge ${getStatusBadgeClass(employee.STATUS_CD)}">
                ${getStatusLabel(employee.STATUS_CD) || ''}
              </span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">입사일자</div>
            <div class="detail-value">${employee.HIRE_DT || 'N/A'}</div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">주소 정보</div>
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">우편번호</div>
            <div class="detail-value">${employee.ZIP_CD || 'N/A'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">주소</div>
            <div class="detail-value">${employee.ADDR || 'N/A'}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">상세주소</div>
            <div class="detail-value">${employee.DADDR || 'N/A'}</div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">연락처</div>
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">이메일</div>
            <div class="detail-value">${employee.STAFF_EML_ADDR || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">연락처</div>
            <div class="detail-value">${employee.STAFF_TELNO || ''}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">등록 관리자 (User ID)</div>
            <div class="detail-value">${employee.CREATED_BY || '정보 없음'}</div>
          </div>
        </div>
      </div>
      <!-- 비밀번호는 상세 정보에서 표시하지 않습니다. -->
    </div>
  `;

    const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
    detailModal.show();
}

// ===============================================
// 교직원 수정 모달 (`editModal`) 관련 함수
// ===============================================

/**
 * 교직원 수정 모달을 열고 선택된 교직원의 정보로 폼 필드를 채웁니다.
 */
function openEditEmployeeModal(employeeMember) {
    currentEditingEmployee = employeeMember;

    // 폼 필드에 EmplInfoDto 필드명에 맞춰서 데이터 채우기 (대문자 스네이크 케이스)
    document.getElementById("edit_id").value = employeeMember.STAFF_NO || '';
    document.getElementById("edit_name").value = employeeMember.STAFF_NM || '';
    document.getElementById("edit_dept").value = employeeMember.DEPT_CD || '';
    document.getElementById("edit_position").value = employeeMember.POSITION_CD || '';
    document.getElementById("edit_status").value = employeeMember.STATUS_CD || '';
    document.getElementById("edit_hire_date").value = employeeMember.HIRE_DT || '';
    document.getElementById("edit_zipcode").value = employeeMember.ZIP_CD || '';
    document.getElementById("edit_address").value = employeeMember.ADDR || '';
    document.getElementById("edit_address_detail").value = employeeMember.DADDR || '';
    document.getElementById("edit_email").value = employeeMember.STAFF_EML_ADDR || '';
    document.getElementById("edit_phone").value = employeeMember.STAFF_TELNO || '';
    // 비밀번호 필드는 제거되었으므로 해당 로직도 제거합니다.

    const editModal = new bootstrap.Modal(document.getElementById("editModal"));
    editModal.show();
}

/**
 * 수정된 교직원 정보를 백엔드로 전송하여 저장합니다.
 */
async function saveEmployee() {
    const employeeNo = document.getElementById("edit_id").value;

    const updatedEmployeeData = {
        STAFF_NM: document.getElementById("edit_name").value.trim(),
        DEPT_CD: document.getElementById("edit_dept").value,
        POSITION_CD: document.getElementById("edit_position").value,
        STATUS_CD: document.getElementById("edit_status").value,
        HIRE_DT: document.getElementById("edit_hire_date").value,
        ZIP_CD: document.getElementById("edit_zipcode").value,
        ADDR: document.getElementById("edit_address").value,
        DADDR: document.getElementById("edit_address_detail").value,
        STAFF_EML_ADDR: document.getElementById("edit_email").value.trim(),
        STAFF_TELNO: document.getElementById("edit_phone").value.trim(),
        USE_YN: currentEditingEmployee.USE_YN, // 기존 값 유지
        CREATED_BY: "admin" // ⭐⭐ CREATED_BY를 "admin"으로 하드코딩하여 전송 ⭐⭐
    };

    try {
        const response = await fetch(`/admin/employee/${employeeNo}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedEmployeeData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`교직원 정보 업데이트 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log("교직원 정보 업데이트 성공:", result);
        alert("교직원 정보가 성공적으로 수정되었습니다.");

        const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
        if (editModal) { editModal.hide(); }

        const { name, dept, status } = getCurrentSearchParams();
        fetchEmployees(currentPage, pageSize, name, dept, status); // 현재 페이지 유지하며 목록 새로고침

    } catch (error) {
        console.error("교직원 정보 업데이트 중 오류 발생:", error);
        alert("교직원 정보 업데이트 중 오류가 발생했습니다: " + error.message);
    }
}

// ===============================================
// 교직원 등록 모달 (`addModal`) 관련 함수
// ===============================================

/**
 * 교직원 등록 모달을 열고 폼을 초기화합니다.
 */
function openAddModal() {
    console.log("교직원 등록 모달 열기 요청됨.");
    const addModalElement = document.getElementById('addModal');
    if (addModalElement) {
        const addModal = new bootstrap.Modal(addModalElement);
        addModal.show();

        const addForm = document.getElementById('addForm');
        if (addForm) {
            addForm.reset(); // 폼 필드 초기화
            // 사번 필드는 읽기 전용이고 백엔드에서 생성되므로 메시지 표시
            if (document.getElementById('add_id')) {
                document.getElementById('add_id').value = "사번은 자동 생성됩니다.";
                document.getElementById('add_id').readOnly = true; // 읽기 전용 설정
            }
            
            // 기타 필드 초기화 및 기본값 설정
            if (document.getElementById('add_status')) document.getElementById('add_status').value = "Y"; // 재직
            if (document.getElementById('add_name')) document.getElementById('add_name').value = "";
            if (document.getElementById('add_dept')) document.getElementById('add_dept').value = "";
            if (document.getElementById('add_position')) document.getElementById('add_position').value = "";
            if (document.getElementById('add_hire_date')) document.getElementById('add_hire_date').value = "";
            if (document.getElementById('add_zipcode')) document.getElementById('add_zipcode').value = "";
            if (document.getElementById('add_address')) document.getElementById('add_address').value = "";
            if (document.getElementById('add_address_detail')) document.getElementById('add_address_detail').value = "";
            if (document.getElementById('add_email')) document.getElementById('add_email').value = "";
            if (document.getElementById('add_phone')) document.getElementById('add_phone').value = "";
        }
    } else {
        console.error("오류: 'addModal' 요소 (교직원 등록 모달)를 찾을 수 없습니다.");
    }
}

/**
 * 새 교직원 정보를 백엔드로 전송하여 등록합니다.
 */
async function addEmployee() {
    // 폼 필드에서 데이터 가져와 백엔드 EmplInfoDto 필드명에 맞춰 구성
    const newEmployeeData = {
        STAFF_NM: document.getElementById("add_name").value.trim(),
        DEPT_CD: document.getElementById("add_dept").value,
        POSITION_CD: document.getElementById("add_position").value,
        STATUS_CD: document.getElementById("add_status").value,
        HIRE_DT: document.getElementById("add_hire_date").value,
        ZIP_CD: document.getElementById("add_zipcode").value,
        ADDR: document.getElementById("add_address").value,
        DADDR: document.getElementById("add_address_detail").value,
        STAFF_EML_ADDR: document.getElementById("add_email").value.trim(),
        STAFF_TELNO: document.getElementById("add_phone").value.trim(),
        USE_YN: "Y", // 기본값 'Y'
        CREATED_BY: "admin" // ⭐⭐ CREATED_BY를 "admin"으로 하드코딩하여 전송 ⭐⭐
        // 비밀번호는 사번/학번으로 자동 생성되므로 여기서 보내지 않습니다.
    };

    // 필수 필드 유효성 검사 (FRONT-END)
    if (
        !newEmployeeData.STAFF_NM || !newEmployeeData.DEPT_CD || 
        !newEmployeeData.POSITION_CD || !newEmployeeData.STATUS_CD || !newEmployeeData.HIRE_DT ||
        !newEmployeeData.STAFF_EML_ADDR || !newEmployeeData.STAFF_TELNO
    ) {
        alert("모든 필수 정보를 입력해주세요.");
        return;
    }
    
    try {
        const response = await fetch('/admin/employee', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newEmployeeData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`교직원 등록 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log('교직원 등록 성공:', result);
        alert(`교직원 ${result.STAFF_NM}(사번: ${result.STAFF_NO})이(가) 성공적으로 등록되었습니다!`);

        const addModal = bootstrap.Modal.getInstance(document.getElementById('addModal'));
        if (addModal) { addModal.hide(); }

        fetchEmployees(0, pageSize); // 첫 페이지부터 새로고침하여 최신 정보 반영

    } catch (error) {
        console.error('교직원 등록 중 오류 발생:', error);
        alert('교직원 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n상세: ' + error.message);
    }
}

// ===============================================
// 교직원 삭제 관련 함수
// ===============================================

/**
 * 특정 사번의 교직원을 삭제합니다.
 */
async function deleteEmployee(employeeNo) {
    if (!confirm(`사번 ${employeeNo} 교직원을 정말 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/admin/employee/${employeeNo}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`교직원 삭제 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        console.log('교직원 삭제 성공:', employeeNo);
        alert(`교직원 ${employeeNo}이(가) 성공적으로 삭제되었습니다.`);

        const { name, dept, status } = getCurrentSearchParams();
        fetchEmployees(currentPage, pageSize, name, dept, status); // 현재 페이지 유지하며 목록 새로고침

    } catch (error) {
        console.error("교직원 삭제 중 오류 발생:", error);
        alert("교직원 삭제 중 오류가 발생했습니다: " + error.message);
    }
}
