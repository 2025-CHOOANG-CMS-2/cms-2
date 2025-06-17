// /js/student-management.js 파일 내용

// ===============================================
// 사이드바 및 서브메뉴 토글 로직
// ===============================================
const sidebar = document.getElementById("sidebar");
const hamburger = document.getElementById("hamburger");
if (hamburger && sidebar) {
    hamburger.addEventListener("click", () => {
        sidebar.classList.toggle("show"); // 'active' -> 'show'로 변경: HTML과 일치
    });
}

// ===============================================
// 전역 변수 (필요시)
// ===============================================
let currentPage = 0; // 현재 페이지 (0부터 시작)
const pageSize = 10; // 페이지당 항목 수
let currentEditingStudent = null; // 현재 수정 중인 학생 정보 객체

// 새로운 학과 목록 데이터 (백엔드의 DEPT_MAP과 동일하게 유지되어야 함)
const DEPT_LIST = [
    { code: "001", name: "국어국문학과" },
    { code: "002", name: "영어영문학과" },
    { code: "003", name: "철학과" },
    { code: "004", name: "정치외교학과" },
    { code: "005", name: "심리학과" },
    { code: "006", name: "사회복지학과" },
    { code: "007", name: "통계학과" },
    { code: "008", name: "천문학과" },
    { code: "009", "name": "화학과" },
    { code: "010", name: "기계공학과" },
    { code: "011", name: "컴퓨터공학과" },
    { code: "012", name: "건축학과" },
    { code: "013", name: "스마트시스템과학과" },
    { code: "014", name: "동양화과" },
    { code: "015", "name": "조소과" },
    { code: "016", name: "공예과" },
    { code: "017", name: "교육학과" },
    { code: "018", name: "식품영양학과" },
    { code: "019", name: "의류학과" },
    { code: "020", name: "성악과" },
    { code: "021", name: "의예과" }
];


// ===============================================
// 도우미 함수 (Helper Functions)
// ===============================================

/**
 * 학과 코드를 학과 이름으로 변환합니다.
 * @param {string} deptCode - 학과 코드 (예: "001")
 * @returns {string} 학과 이름 (예: "국어국문학과") 또는 매핑되지 않으면 원래 코드
 */
function getDeptName(deptCode) {
    const foundDept = DEPT_LIST.find(dept => dept.code === deptCode);
    return foundDept ? foundDept.name : deptCode;
}

/**
 * 상태 코드를 한글 라벨로 변환합니다.
 * @param {string} statusCode - 상태 코드 (예: "ENROLL")
 * @returns {string} 상태 라벨 (예: "재학") 또는 매핑되지 않으면 원래 코드
 */
function getStatusLabel(statusCode) {
    const statusMap = {
        "ENROLL": "재학",
        "LEAVE": "휴학",
        "GRAD": "졸업",
    };
    return statusMap[statusCode] || statusCode;
}

/**
 * 상태 코드에 따른 Bootstrap 배지 클래스를 반환합니다.
 * @param {string} status - 상태 코드
 * @returns {string} CSS 클래스 문자열
 */
function getStatusBadgeClass(status) {
    switch (status) {
        case "ENROLL":
            return "status-enroll"; // 이 클래스는 student-management.css에 정의되어야 함
        case "LEAVE":
            return "status-leave"; // 이 클래스는 student-management.css에 정의되어야 함
        case "GRAD":
            return "status-grad"; // 이 클래스는 student-management.css에 정의되어야 함
        default:
            return "status-default"; // 이 클래스는 student-management.css에 정의되어야 함
    }
}

/**
 * 페이지 내의 모든 학과 드롭다운을 동적으로 채웁니다.
 */
function populateDeptDropdowns() {
    const searchDeptSelect = document.getElementById('searchDept');
    const addDeptSelect = document.getElementById('add_dept');
    const editDeptSelect = document.getElementById('edit_dept');

    // 검색 학과 드롭다운 초기화 및 채우기
    if (searchDeptSelect) {
        searchDeptSelect.innerHTML = '<option value="">학과 전체</option>';
        DEPT_LIST.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.code;
            option.textContent = dept.name;
            searchDeptSelect.appendChild(option);
        });
    }

    // 등록 모달 학과 드롭다운 초기화 및 채우기
    if (addDeptSelect) {
        addDeptSelect.innerHTML = '<option value="">학과를 선택하세요</option>';
        DEPT_LIST.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.code;
            option.textContent = dept.name;
            addDeptSelect.appendChild(option);
        });
    }

    // 수정 모달 학과 드롭다운 초기화 및 채우기
    if (editDeptSelect) {
        editDeptSelect.innerHTML = ''; // 기본 "학과를 선택하세요" 옵션 없이 시작
        DEPT_LIST.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.code;
            option.textContent = dept.name;
            editDeptSelect.appendChild(option);
        });
    }
}


// ===============================================
// 데이터 로드, 테이블 렌더링 및 페이지네이션 핵심 로직
// ===============================================

// DOMContentLoaded: HTML 문서가 완전히 로드되고 파싱된 후에 실행
document.addEventListener('DOMContentLoaded', () => {
    populateDeptDropdowns(); // 페이지 로드 시 학과 드롭다운 먼저 채우기
    fetchStudents(currentPage, pageSize); // 초기 학생 목록 로드

    // 검색 버튼 클릭 이벤트 리스너 연결
    const filterButton = document.getElementById("filterButton");
    if (filterButton) {
        filterButton.addEventListener("click", filterStudents);
    } else {
        console.warn("경고: HTML에서 ID 'filterButton'을 가진 검색 버튼을 찾을 수 없습니다. HTML을 확인해주세요.");
    }

    // 사이드바 서브메뉴 토글 로직 (기존 HTML에서 가져옴)
    document.querySelectorAll('.nav-link.has-submenu').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const key = this.getAttribute("data-menu");
            const submenu = document.getElementById(`submenu-${key}`);
            if (submenu) {
                document.querySelectorAll(".submenu").forEach((sm) => {
                    if (sm !== submenu) sm.style.display = "none";
                });
                submenu.style.display = submenu.style.display === 'block' ? 'none' : 'block';
            }
        });
    });
});

/**
 * 백엔드 API에서 학생 목록을 비동기적으로 가져와 테이블과 페이지네이션 UI를 업데이트합니다.
 * 검색 조건과 페이징 정보를 함께 전송합니다.
 * @param {number} page - 조회할 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @param {string} searchName - 검색할 학생 이름 (선택 사항)
 * @param {string} searchDept - 검색할 학과 코드 (선택 사항)
 * @param {string} searchStatus - 검색할 상태 코드 (선택 사항)
 */
async function fetchStudents(page = 0, size = 10, searchName = '', searchDept = '', searchStatus = '') {
    currentPage = page; // 현재 페이지 전역 변수 업데이트

    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);

    // 검색 조건이 있을 경우에만 URL 파라미터로 추가 (백엔드의 @RequestParam 이름과 일치)
    if (searchName) {
        params.append('searchName', searchName.trim());
    }
    if (searchDept) {
        params.append('searchDept', searchDept);
    }
    if (searchStatus) {
        params.append('searchStatus', searchStatus);
    }

    const apiUrl = `/admin/student?${params.toString()}`;

    try {
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP 오류! 상태: ${response.status}, 메시지: ${errorText}`);
        }
        
        const pageData = await response.json(); // 백엔드에서 Page<StdInfoDto> 형태로 반환됨

        const students = pageData.content; // 실제 학생 데이터 배열
        const totalPages = pageData.totalPages; // 전체 페이지 수
        const currentPageFromBackend = pageData.number; // 백엔드가 알려주는 현재 페이지 번호

        // 1. 학생 테이블 데이터 업데이트
        const studentTableBody = document.querySelector('#studentTable tbody'); 
        if (!studentTableBody) {
            console.error("오류: #studentTable tbody 요소를 찾을 수 없습니다.");
            return;
        }
        studentTableBody.innerHTML = ''; // 기존 테이블 내용 모두 지우기

        if (students.length === 0) {
            studentTableBody.innerHTML = '<tr><td colspan="8">등록된 학생 정보가 없습니다.</td></tr>'; // 컬럼 수에 맞게 colspan 조정
        } else {
            const rowsHtml = students.map(s => {
                // student 객체를 JSON 문자열로 변환하고 HTML에서 안전하게 사용하기 위해 큰따옴표 이스케이프
                const studentJsonString = JSON.stringify(s).replace(/"/g, "&quot;");

                return `
                    <tr onclick="showDetail(${studentJsonString})">
                        <td>${s.STD_NO || ''}</td>
                        <td>${s.STD_NM || ''}</td>
                        <td>${getDeptName(s.SCSBJT_CD) || ''}</td>
                        <td>${s.SCH_YR || ''}</td>
                        <td><span class="badge ${getStatusBadgeClass(s.STD_STAT_CD)}">${getStatusLabel(s.STD_STAT_CD) || ''}</span></td>
                        <td>${s.STD_TELNO || ''}</td>
                        <td>${s.STD_EML_ADDR || ''}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-secondary" onclick="event.stopPropagation(); openEditModal(${studentJsonString})">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="event.stopPropagation(); deleteStudent('${s.STD_NO}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');

            studentTableBody.innerHTML = rowsHtml; // 생성된 HTML로 테이블 내용 업데이트
        }

        // 2. 페이지네이션 UI 업데이트
        const paginationUl = document.querySelector('.pagination'); 
        if (!paginationUl) {
            console.error("오류: .pagination ul 요소를 찾을 수 없습니다.");
            return;
        }
        paginationUl.innerHTML = ''; // 기존 페이지네이션 버튼들 모두 지우기

        // 현재 검색 조건을 가져오는 헬퍼 함수
        const getCurrentSearchParams = () => {
            const name = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
            const dept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
            const status = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
            return { name, dept, status };
        };

        // '이전' 버튼 생성
        const prevLi = document.createElement('li');
        prevLi.classList.add('page-item');
        if (currentPageFromBackend === 0) {
            prevLi.classList.add('disabled'); // 첫 페이지일 경우 비활성화
        }
        const prevLink = document.createElement('a');
        prevLink.classList.add('page-link');
        prevLink.href = "#"; // 링크 클릭 시 페이지 새로고침 방지
        prevLink.setAttribute('aria-label', 'Previous');
        prevLink.innerHTML = '<span aria-hidden="true">&laquo;</span>';
        prevLink.onclick = (e) => {
            e.preventDefault(); // 기본 링크 동작 방지
            if (currentPageFromBackend > 0) {
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(currentPageFromBackend - 1, size, name, dept, status);
            }
        };
        prevLi.appendChild(prevLink);
        paginationUl.appendChild(prevLi);

        // 페이지 번호 버튼들 생성
        let startPage = Math.max(0, currentPageFromBackend - 2); // 현재 페이지 기준으로 시작 페이지 계산
        let endPage = Math.min(totalPages - 1, currentPageFromBackend + 2); // 현재 페이지 기준으로 끝 페이지 계산

        // 페이지 버튼 범위 조정 (항상 최소 5개 버튼 표시)
        if (endPage - startPage < 4 && totalPages > 1) { // 5개 버튼이 안 될 경우
            if (currentPageFromBackend < 2) { // 앞쪽에 가까우면 끝 페이지 조정
                endPage = Math.min(totalPages - 1, 4);
                startPage = 0;
            } else if (currentPageFromBackend > totalPages - 3) { // 뒤쪽에 가까우면 시작 페이지 조정
                startPage = Math.max(0, totalPages - 5);
                endPage = totalPages - 1;
            }
        }

        // 1페이지로 가는 버튼 (필요시)
        if (startPage > 0) {
            const li = document.createElement('li');
            li.classList.add('page-item');
            const link = document.createElement('a');
            link.classList.add('page-link');
            link.href = "#";
            link.textContent = '1';
            link.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(0, size, name, dept, status);
            };
            li.appendChild(link);
            paginationUl.appendChild(li);
            if (startPage > 1) {
                const ellipsis = document.createElement('li');
                ellipsis.classList.add('page-item', 'disabled');
                ellipsis.innerHTML = '<span class="page-link">...</span>';
                paginationUl.appendChild(ellipsis);
            }
        }

        // 실제 페이지 번호 버튼들
        for (let i = startPage; i <= endPage; i++) {
            const li = document.createElement('li');
            li.classList.add('page-item');
            if (i === currentPageFromBackend) {
                li.classList.add('active'); // 현재 페이지 활성화 표시
            }
            const link = document.createElement('a');
            link.classList.add('page-link');
            link.href = "#";
            link.textContent = i + 1; // 페이지 번호는 1부터 시작하므로 +1
            link.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(i, size, name, dept, status);
            };
            li.appendChild(link);
            paginationUl.appendChild(li);
        }

        // 마지막 페이지로 가는 버튼 (필요시)
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                const ellipsis = document.createElement('li');
                ellipsis.classList.add('page-item', 'disabled');
                ellipsis.innerHTML = '<span class="page-link">...</span>';
                paginationUl.appendChild(ellipsis);
            }
            const li = document.createElement('li');
            li.classList.add('page-item');
            const link = document.createElement('a');
            link.classList.add('page-link');
            link.href = "#";
            link.textContent = totalPages;
            link.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(totalPages - 1, size, name, dept, status);
            };
            li.appendChild(link);
            paginationUl.appendChild(li);
        }

        // '다음' 버튼 생성
        const nextLi = document.createElement('li');
        nextLi.classList.add('page-item');
        if (currentPageFromBackend === totalPages - 1) {
            nextLi.classList.add('disabled'); // 마지막 페이지일 경우 비활성화
        }
        const nextLink = document.createElement('a');
        nextLink.classList.add('page-link');
        nextLink.href = "#";
        nextLink.setAttribute('aria-label', 'Next');
        nextLink.innerHTML = '<span aria-hidden="true">&raquo;</span>';
        nextLink.onclick = (e) => {
            e.preventDefault();
            if (currentPageFromBackend < totalPages - 1) {
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(currentPageFromBackend + 1, size, name, dept, status);
            }
        };
        nextLi.appendChild(nextLink);
        paginationUl.appendChild(nextLi);

    } catch (error) {
        console.error('학생 목록을 가져오는 중 오류 발생:', error);
        alert('학생 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요.');
    }
}

/**
 * 검색 필드의 값을 가져와 학생 목록을 새로 필터링합니다.
 * 항상 첫 페이지부터 검색 결과를 표시합니다.
 */
function filterStudents() {
    const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
    const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
    const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';

    fetchStudents(0, pageSize, searchName, searchDept, searchStatus); // 검색 시 항상 0페이지부터 시작
}


// ===============================================
// 상세 보기 모달 (`detailModal`) 관련 함수
// ===============================================

/**
 * 학생 상세 정보를 모달에 표시합니다.
 * @param {object} student - 표시할 학생 정보 객체
 */
function showDetail(student) {
    currentEditingStudent = student; // 현재 상세 보거나 수정할 학생 정보 저장

    const modalBody = document.getElementById("detailContent");
    if (!modalBody) {
        console.error("오류: 'detailContent' 요소를 찾을 수 없습니다.");
        return;
    }

    // 학생 상세 정보 HTML 구성
    modalBody.innerHTML = `
    <div class="student-detail-card">
      <div class="detail-section">
        <div class="section-title">기본 정보</div>
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학번</div>
            <div class="detail-value">${student.STD_NO || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학생명</div>
            <div class="detail-value">${student.STD_NM || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">학과명</div>
            <div class="detail-value">${getDeptName(student.SCSBJT_CD) || ''}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item">
            <div class="detail-label">학년</div>
            <div class="detail-value">${student.SCH_YR || ''}학년</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">재학여부</div>
            <div class="detail-value">
              <span class="badge ${getStatusBadgeClass(student.STD_STAT_CD)}"> <!-- 'status-badge' -> 'badge'로 변경 -->
                ${getStatusLabel(student.STD_STAT_CD) || ''}
              </span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">입학일자</div>
            <div class="detail-value">${student.ENTR_DT || 'N/A'}</div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">주소 정보</div>
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">우편번호</div>
            <div class="detail-value">${student.STD_ZIP || 'N/A'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">주소</div>
            <div class="detail-value">${student.STD_ADDR || 'N/A'}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">상세주소</div>
            <div class="detail-value">${student.STD_DADDR || 'N/A'}</div>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">연락처</div>
        <div class="detail-row two-items">
          <div class="detail-item">
            <div class="detail-label">이메일</div>
            <div class="detail-value">${student.STD_EML_ADDR || ''}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">전화번호</div>
            <div class="detail-value">${student.STD_TELNO || ''}</div>
          </div>
        </div>
        <div class="detail-row">
          <div class="detail-item full-width">
            <div class="detail-label">등록 관리자</div>
            <div class="detail-value">${student.USER_ID2 || '정보 없음'}</div>
          </div>
        </div>
      </div>
    </div>
  `;

    // 상세 모달에서 수정 버튼 클릭 시 수정 모달 열기
    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.onclick = function () {
            // 상세 모달 닫기
            const detailModalInstance = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
            if (detailModalInstance) {
                detailModalInstance.hide();
            }
            openEditModal(currentEditingStudent); // 수정 모달 열기
        };
    }

    // 부트스트랩 모달 인스턴스 생성 및 표시
    new bootstrap.Modal(document.getElementById("detailModal")).show();
}

// ===============================================
// 학생 수정 모달 (`editModal`) 관련 함수
// ===============================================

/**
 * 학생 수정 모달을 열고 선택된 학생의 정보로 폼 필드를 채웁니다.
 * @param {object} student - 수정할 학생 정보 객체
 */
function openEditModal(student) {
    currentEditingStudent = student; // 현재 수정할 학생 정보 저장

    // 폼 필드에 StdInfoDto 필드명에 맞춰서 데이터 채우기 (대문자 스네이크 케이스)
    document.getElementById("edit_id").value = student.STD_NO || ''; // 학번 필드에 STD_NO 값 채우기 (읽기 전용)
    document.getElementById("edit_name").value = student.STD_NM || '';
    document.getElementById("edit_dept").value = student.SCSBJT_CD || ''; // 학과 드롭다운 선택
    document.getElementById("edit_year").value = student.SCH_YR || '';
    document.getElementById("edit_status").value = student.STD_STAT_CD || '';
    document.getElementById("edit_admission_date").value = student.ENTR_DT || '';
    document.getElementById("edit_zipcode").value = student.STD_ZIP || '';
    document.getElementById("edit_address").value = student.STD_ADDR || '';
    document.getElementById("edit_address_detail").value = student.STD_DADDR || '';
    document.getElementById("edit_email").value = student.STD_EML_ADDR || '';
    document.getElementById("edit_phone").value = student.STD_TELNO || '';
    // USER_ID2는 등록/수정 관리자 ID이므로, 수정 모달에서는 표시하거나 직접 수정하지 않을 수 있음
    // document.getElementById("edit_user_id2").value = student.USER_ID2 || '';

    // 상세 모달이 열려있다면 닫기
    const detailModal = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
    if (detailModal) {
        detailModal.hide();
    }

    // 수정 모달 열기
    new bootstrap.Modal(document.getElementById("editModal")).show();
}

/**
 * 수정된 학생 정보를 백엔드로 전송하여 저장합니다.
 */
async function saveStudent() {
    // 수정할 학생의 학번을 가져옴 (URL 경로 변수로 사용)
    const studentStdNo = document.getElementById("edit_id").value;
    
    // 백엔드 StdInfoDto 필드명에 맞춰서 데이터 구성
    const updatedStudentData = {
        // "STD_NO"는 경로변수로 전달되므로 요청 본문에 포함하지 않음 (선택 사항)
        "STD_NM": document.getElementById("edit_name").value,
        "SCSBJT_CD": document.getElementById("edit_dept").value, // 드롭다운에서 선택된 숫자 학과 코드
        "SCH_YR": parseInt(document.getElementById("edit_year").value),
        "ENTR_DT": document.getElementById("edit_admission_date").value,
        "STD_STAT_CD": document.getElementById("edit_status").value,
        "STD_ZIP": document.getElementById("edit_zipcode").value,
        "STD_ADDR": document.getElementById("edit_address").value,
        "STD_DADDR": document.getElementById("edit_address_detail").value,
        "STD_EML_ADDR": document.getElementById("edit_email").value,
        "STD_TELNO": document.getElementById("edit_phone").value,
        // USER_ID2와 USE_YN은 현재 로그인한 관리자의 정보 또는 기존 값을 유지 (백엔드에서 처리될 것임)
        "USER_ID2": currentEditingStudent.USER_ID2, // 기존 등록 관리자 ID 유지
        "USE_YN": currentEditingStudent.USE_YN // 기존 사용 여부 유지
    };

    try {
        // PUT 요청으로 API 호출 (URL에 학번 포함)
        const response = await fetch(`/admin/student/${studentStdNo}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedStudentData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 정보 업데이트 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json(); // 등록 성공 시 백엔드에서 반환된 StdInfoDto
        console.log("학생 정보 업데이트 성공:", result);
        alert("학생 정보가 성공적으로 수정되었습니다.");

        // 수정 모달 닫기
        const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
        if (editModal) {
            editModal.hide();
        }

        // 현재 페이지와 검색 조건을 유지하며 학생 목록 새로고침
        const { name, dept, status } = getCurrentSearchParams();
        fetchStudents(currentPage, pageSize, name, dept, status);

    } catch (error) {
        console.error("학생 정보 업데이트 중 오류 발생:", error);
        alert("학생 정보 업데이트 중 오류가 발생했습니다: " + error.message);
    }
}

// ===============================================
// 학생 등록 모달 (`addModal`) 관련 함수
// ===============================================

/**
 * 학생 등록 모달을 열고 폼을 초기화합니다.
 */
function openAddModal() {
    console.log("학생 등록 모달 열기 요청됨.");
    const addModalElement = document.getElementById('addModal');
    if (addModalElement) {
        const addModal = new bootstrap.Modal(addModalElement);
        addModal.show();

        const addForm = document.getElementById('addForm');
        if (addForm) {
            addForm.reset(); // 폼 필드 초기화
            // 학번 필드는 읽기 전용이고 백엔드에서 생성되므로 메시지 표시
            if (document.getElementById('add_id')) document.getElementById('add_id').value = "학번은 자동 생성됩니다.";
            
            // 기본값 설정 (필요시)
            if (document.getElementById('add_year')) document.getElementById('add_year').value = "1";
            if (document.getElementById('add_status')) document.getElementById('add_status').value = "ENROLL";
            
            // 기타 필드 초기화
            if (document.getElementById('add_email')) document.getElementById('add_email').value = "";
            if (document.getElementById('add_dept')) document.getElementById('add_dept').value = "";
            // 기타 필드들: add_name, add_admission_date, add_zipcode, add_address, add_address_detail, add_phone
            // 명시적으로 빈 값으로 설정하여 확실히 초기화
            if (document.getElementById('add_name')) document.getElementById('add_name').value = "";
            if (document.getElementById('add_admission_date')) document.getElementById('add_admission_date').value = "";
            if (document.getElementById('add_zipcode')) document.getElementById('add_zipcode').value = "";
            if (document.getElementById('add_address')) document.getElementById('add_address').value = "";
            if (document.getElementById('add_address_detail')) document.getElementById('add_address_detail').value = "";
            if (document.getElementById('add_phone')) document.getElementById('add_phone').value = "";
        }
    } else {
        console.error("오류: 'addModal' 요소 (학생 등록 모달)를 찾을 수 없습니다.");
    }
}

/**
 * 새 학생 정보를 백엔드로 전송하여 등록합니다.
 */
async function addStudent() {
    // 폼 필드에서 데이터 가져와 백엔드 StdInfoDto 필드명에 맞춰 구성
    const studentData = {
        "STD_NM": document.getElementById('add_name').value,
        "SCSBJT_CD": document.getElementById('add_dept').value, // 드롭다운에서 선택된 숫자 학과 코드
        "SCH_YR": parseInt(document.getElementById('add_year').value),
        "ENTR_DT": document.getElementById('add_admission_date').value,
        "STD_STAT_CD": document.getElementById('add_status').value, // ENROLL, LEAVE, GRAD 중 하나
        "STD_ZIP": document.getElementById('add_zipcode').value,
        "STD_ADDR": document.getElementById('add_address').value,
        "STD_DADDR": document.getElementById('add_address_detail').value,
        "STD_TELNO": document.getElementById('add_phone').value,
        "STD_EML_ADDR": document.getElementById('add_email').value,
        // 이 부분은 실제 로그인한 관리자의 정보 또는 기존 값을 유지 (백엔드에서 처리될 것임)
        "USER_ID2": "admin", // !!! 요청에 따라 'admin'으로 설정 !!!
        "USE_YN": "Y" // 기본값 'Y'
    };

    // 필수 필드 유효성 검사 (FRONT-END)
    if (
        !studentData.STD_NM || !studentData.SCSBJT_CD || 
        isNaN(studentData.SCH_YR) || !studentData.ENTR_DT || 
        !studentData.STD_STAT_CD || !studentData.STD_EML_ADDR || 
        !studentData.STD_TELNO || !studentData.USER_ID2
    ) {
        alert("모든 필수 정보를 입력해주세요 (학생명, 학과명, 학년, 입학일자, 재학여부, 이메일, 전화번호).");
        return;
    }

    try {
        // POST 요청으로 API 호출
        const response = await fetch('/admin/student', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(studentData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 등록 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json(); // 등록 성공 시 백엔드에서 반환된 StdInfoDto
        console.log('학생 등록 성공:', result);
        alert(`학생 ${result.STD_NM}(학번: ${result.STD_NO})이(가) 성공적으로 등록되었습니다!`);

        // 등록 모달 닫기
        const addModal = bootstrap.Modal.getInstance(document.getElementById('addModal'));
        if (addModal) {
            addModal.hide();
        }

        // 학생 목록 첫 페이지부터 다시 로드하여 최신 정보 반영
        fetchStudents(0, pageSize);

    } catch (error) {
        console.error('학생 등록 중 오류 발생:', error);
        alert('학생 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n상세: ' + error.message);
    }
}

// ===============================================
// 학생 삭제 관련 함수
// ===============================================

/**
 * 특정 학번의 학생을 삭제합니다.
 * @param {string} stdNo - 삭제할 학생의 학번
 */
async function deleteStudent(stdNo) {
    if (!confirm(`학번 ${stdNo} 학생을 정말 삭제하시겠습니까?`)) {
        return; // 사용자가 취소하면 아무것도 하지 않음
    }

    try {
        // DELETE 요청으로 API 호출 (URL에 학번 포함)
        const response = await fetch(`/admin/student/${stdNo}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 삭제 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        console.log('학생 삭제 성공:', stdNo);
        alert(`학생 ${stdNo}이(가) 성공적으로 삭제되었습니다.`);

        // 학생 목록 새로고침 (삭제 후 현재 페이지 유지)
        const { name, dept, status } = getCurrentSearchParams();
        fetchStudents(currentPage, pageSize, name, dept, status);

    } catch (error) {
        console.error("학생 삭제 중 오류 발생:", error);
        alert("학생 삭제 중 오류가 발생했습니다: " + error.message);
    }
}
