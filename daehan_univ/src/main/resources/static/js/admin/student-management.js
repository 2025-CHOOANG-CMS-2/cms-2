// /js/student-management.js 파일 내용

// ===============================================
// 사이드바 및 서브메뉴 토글 로직
// ===============================================
document.addEventListener('DOMContentLoaded', function () { // DOMContentLoaded로 전체를 감쌈
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

    // ===============================================
    // 전역 변수
    // ===============================================
    // 전역 변수는 DOMContentLoaded 바깥에 선언하는 것이 일반적이지만,
    // 이 스크립트가 단일 파일로 구성된다면 이 안에서 관리해도 무방합니다.
    // 여기서는 기존 구조를 유지합니다.

    // ===============================================
    // 초기화 및 이벤트 리스너 연결
    // ===============================================
    populateDeptDropdowns(); // 페이지 로드 시 학과 드롭다운 먼저 채우기
    fetchStudents(currentPage, pageSize); // 초기 학생 목록 로드

    // 검색 버튼 클릭 이벤트 리스너 연결
    const filterButton = document.getElementById("filterButton");
    if (filterButton) {
        filterButton.addEventListener("click", filterStudents);
    } else {
        console.warn("경고: HTML에서 ID 'filterButton'을 가진 검색 버튼을 찾을 수 없습니다. HTML을 확인해주세요.");
    }

    // 상세 보기 모달에서 수정 버튼 클릭 시 수정 모달 열기
    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.addEventListener('click', function() {
            if (currentEditingStudent) {
                const detailModalInstance = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
                if (detailModalInstance) {
                    detailModalInstance.hide();
                }
                openEditModal(currentEditingStudent);
            }
        });
    }
});


let currentPage = 0; // 현재 페이지 (0부터 시작)
const pageSize = 10; // 페이지당 항목 수
let currentEditingStudent = null; // 현재 수정 중인 학생 정보 객체

// 학과 목록 데이터 (백엔드의 AdminStdService.SCSBJT_MAP과 동일하게 유지되어야 함)
const DEPT_LIST = [
    { code: "001", name: "국어국문학과" },
    { code: "002", name: "영어영문학과" },
    { code: "003", name: "철학과" },
    { code: "004", name: "정치외교학과" },
    { code: "005", name: "심리학과" },
    { code: "006", name: "사회복지학과" },
    { code: "007", name: "통계학과" },
    { code: "008", name: "천문학과" },
    { code: "009", name: "화학과" },
    { code: "010", name: "기계공학과" },
    { code: "011", name: "컴퓨터공학과" },
    { code: "012", name: "건축학과" },
    { code: "013", name: "스마트시스템과학과" },
    { code: "014", name: "동양화과" },
    { code: "015", name: "조소과" },
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
        "EXPEL": "제적",
        "GRAD_WAIT": "졸업유예",
        "ABSENT_LEAVE": "자퇴"
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
        case "ENROLL": return "status-enroll";
        case "LEAVE": return "status-leave";
        case "GRAD": return "status-grad";
        case "EXPEL": return "status-expel";
        default: return "status-default";
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
            e.preventDefault();
            const { name, dept, status } = getCurrentSearchParams();
            fetchStudents(currentPageFromBackend - 1, pageSize, name, dept, status);
        };
        paginationUl.appendChild(prevLi).appendChild(prevLink);


        // 페이지 번호 버튼들 생성
        const maxPagesToShow = 5; // 화면에 보여줄 최대 페이지 번호 개수
        let startPage = Math.max(0, currentPageFromBackend - Math.floor(maxPagesToShow / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

        // 시작 페이지 조정 (끝 페이지가 충분히 나오지 않을 경우)
        if (endPage - startPage + 1 < maxPagesToShow && totalPages > maxPagesToShow) {
            startPage = Math.max(0, endPage - maxPagesToShow + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            const li = document.createElement('li');
            li.classList.add('page-item');
            if (i === currentPageFromBackend) {
                li.classList.add('active'); // 현재 페이지 활성화
            }
            const link = document.createElement('a');
            link.classList.add('page-link');
            link.href = "#";
            link.textContent = i + 1; // 페이지 번호는 1부터 시작
            link.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(i, pageSize, name, dept, status);
            };
            paginationUl.appendChild(li).appendChild(link);
        }

        // '다음' 버튼 생성
        const nextLi = document.createElement('li');
        nextLi.classList.add('page-item');
        if (currentPageFromBackend === totalPages - 1 || totalPages === 0) {
            nextLi.classList.add('disabled'); // 마지막 페이지일 경우 비활성화
        }
        const nextLink = document.createElement('a');
        nextLink.classList.add('page-link');
        nextLink.href = "#";
        nextLink.setAttribute('aria-label', 'Next');
        nextLink.innerHTML = '<span aria-hidden="true">&raquo;</span>';
        nextLink.onclick = (e) => {
            e.preventDefault();
            const { name, dept, status } = getCurrentSearchParams();
            fetchStudents(currentPageFromBackend + 1, pageSize, name, dept, status);
        };
        paginationUl.appendChild(nextLi).appendChild(nextLink);
    } catch (error) {
        console.error('학생 목록을 가져오는 중 오류 발생:', error);
        alert('학생 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요: ' + error.message);
    }
}


/**
 * 검색 필드의 값을 가져와 학생 목록을 새로 필터링하고 첫 페이지부터 표시합니다.
 */
function filterStudents() {
    const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
    const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
    const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
    fetchStudents(0, pageSize, searchName, searchDept, searchStatus); // 항상 첫 페이지부터 필터링
}


// ===============================================
// 상세 보기 모달 (`detailModal`) 관련 함수
// ===============================================

/**
 * 학생 상세 정보를 모달에 표시합니다.
 * @param {object} student - 표시할 학생 정보 객체 (StdInfoDto)
 */
function showDetail(student) {
    currentEditingStudent = student; // 현재 수정할 학생 정보 저장

    const modalBody = document.getElementById("detailContent");
    if (!modalBody) {
        console.error("오류: 'detailContent' 요소를 찾을 수 없습니다.");
        return;
    }

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
                        <div class="detail-label">이름</div>
                        <div class="detail-value">${student.STD_NM || ''}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-label">학과</div>
                        <div class="detail-value">${getDeptName(student.SCSBJT_CD) || ''}</div>
                    </div>
                </div>
                <div class="detail-row">
                    <div class="detail-item">
                        <div class="detail-label">학년</div>
                        <div class="detail-value">${student.SCH_YR || ''}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-label">상태</div>
                        <div class="detail-value">
                            <span class="badge ${getStatusBadgeClass(student.STD_STAT_CD)}">
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
                        <div class="detail-label">등록 관리자 (User ID)</div>
                        <div class="detail-value">${student.CREATED_BY || '정보 없음'}</div>
                    </div>
                </div>
                <div class="detail-row">
                    <div class="detail-item full-width">
                        <div class="detail-label">프로필 이미지</div>
                        <div class="detail-value">
                            <img src="${student.PROFILE_IMAGE_URL || 'https://placehold.co/100x100?text=No+Image'}" alt="프로필 이미지" class="img-thumbnail" style="max-width: 150px; max-height: 150px;">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    const detailModal = new bootstrap.Modal(document.getElementById("detailModal"));
    detailModal.show();
}

// ===============================================
// 학생 수정 모달 (`editModal`) 관련 함수
// ===============================================

/**
 * 학생 수정 모달을 열고 선택된 학생의 정보로 폼 필드를 채웁니다.
 */
function openEditModal(student) {
    currentEditingStudent = student; // 현재 수정할 학생 정보 저장

    // HTML 폼 필드에 데이터 채우기 (StdInfoDto의 필드명에 맞춰 수정)
    // document.getElementById("edit_std_id").value = student.STD_ID || ''; // DTO에서 ID 가져오기
    document.getElementById("edit_std_no").value = student.STD_NO || ''; // 학번은 readOnly
    document.getElementById("edit_std_nm").value = student.STD_NM || '';
    document.getElementById("edit_scsbjt_cd").value = student.SCSBJT_CD || '';
    document.getElementById("edit_sch_yr").value = student.SCH_YR || '';
    document.getElementById("edit_entr_dt").value = student.ENTR_DT || '';
    document.getElementById("edit_std_stat_cd").value = student.STD_STAT_CD || '';
    document.getElementById("edit_std_zip").value = student.STD_ZIP || '';
    document.getElementById("edit_std_addr").value = student.STD_ADDR || '';
    document.getElementById("edit_std_daddr").value = student.STD_DADDR || '';
    document.getElementById("edit_std_telno").value = student.STD_TELNO || '';
    document.getElementById("edit_std_eml_addr").value = student.STD_EML_ADDR || '';
    document.getElementById("edit_use_yn").value = student.USE_YN || '';
    document.getElementById("edit_created_by").value = student.CREATED_BY || '';

    const editModal = new bootstrap.Modal(document.getElementById("editModal"));
    editModal.show();
}

/**
 * 수정된 학생 정보를 백엔드로 전송하여 저장합니다.
 */
async function saveStudent() {
    const stdNo = document.getElementById("edit_std_no").value; // 학번은 PathVariable로 사용

    const updatedData = {
        STD_ID: currentEditingStudent.STD_ID, // 기존 ID를 유지하여 전송
        STD_NO: stdNo, // DTO에도 포함 (Backend @RequestBody 매핑 위함)
        STD_NM: document.getElementById("edit_std_nm").value.trim(),
        SCSBJT_CD: document.getElementById("edit_scsbjt_cd").value,
        SCH_YR: parseInt(document.getElementById("edit_sch_yr").value),
        ENTR_DT: document.getElementById("edit_entr_dt").value,
        STD_STAT_CD: document.getElementById("edit_std_stat_cd").value,
        STD_ZIP: document.getElementById("edit_std_zip").value,
        STD_ADDR: document.getElementById("edit_std_addr").value,
        STD_DADDR: document.getElementById("edit_std_daddr").value,
        STD_TELNO: document.getElementById("edit_std_telno").value.trim(),
        STD_EML_ADDR: document.getElementById("edit_std_eml_addr").value.trim(),
        USE_YN: document.getElementById("edit_use_yn").value,
        PROFILE_IMAGE_URL: currentEditingStudent.PROFILE_IMAGE_URL, // 프로필 이미지는 별도 업로드 함수 사용 가정
        CREATED_BY: document.getElementById("edit_created_by").value
    };

    // 프론트엔드 유효성 검사 (추가적으로 필요하면 더 구현)
    if (
        !updatedData.STD_NM || !updatedData.SCSBJT_CD || 
        isNaN(updatedData.SCH_YR) || !updatedData.ENTR_DT || !updatedData.STD_STAT_CD ||
        !updatedData.STD_TELNO || !updatedData.STD_EML_ADDR
    ) {
        alert("모든 필수 정보를 입력해주세요.");
        return;
    }

    try {
        const response = await fetch(`/admin/student/${stdNo}`, { // 학번을 PathVariable로 전달
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 정보 업데이트 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log("학생 정보 업데이트 성공:", result);
        alert("학생 정보가 성공적으로 수정되었습니다.");

        const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
        if (editModal) { editModal.hide(); }

        const { name, dept, status } = getCurrentSearchParams();
        fetchStudents(currentPage, pageSize, name, dept, status); // 업데이트 후 목록 새로고침

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
            addForm.reset();
            // 학번은 백엔드에서 자동 생성되므로, 폼에서는 표시만
            if (document.getElementById('add_std_no')) {
                document.getElementById('add_std_no').value = "학번은 자동 생성됩니다.";
                document.getElementById('add_std_no').readOnly = true;
            }
            
            // 기본값 설정
            if (document.getElementById('add_use_yn')) document.getElementById('add_use_yn').value = "Y";
            if (document.getElementById('add_std_stat_cd')) document.getElementById('add_std_stat_cd').value = "ENROLL"; // 기본 재학
            if (document.getElementById('add_sch_yr')) document.getElementById('add_sch_yr').value = 1; // 기본 1학년
            if (document.getElementById('add_entr_dt')) {
                const today = new Date();
                const year = today.getFullYear();
                const month = String(today.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작
                const day = String(today.getDate()).padStart(2, '0');
                document.getElementById('add_entr_dt').value = `${year}-${month}-${day}`;
            }

            // 그 외 필드는 비우기
            if (document.getElementById('add_std_nm')) document.getElementById('add_std_nm').value = "";
            if (document.getElementById('add_scsbjt_cd')) document.getElementById('add_scsbjt_cd').value = ""; // 학과 선택 유도
            if (document.getElementById('add_std_zip')) document.getElementById('add_std_zip').value = "";
            if (document.getElementById('add_std_addr')) document.getElementById('add_std_addr').value = "";
            if (document.getElementById('add_std_daddr')) document.getElementById('add_std_daddr').value = "";
            if (document.getElementById('add_std_telno')) document.getElementById('add_std_telno').value = "";
            if (document.getElementById('add_std_eml_addr')) document.getElementById('add_std_eml_addr').value = "";
            // CREATED_BY는 로그인된 관리자의 ID로 자동 채워져야 함 (백엔드에서 처리하거나, 프론트엔드에서 세션 등으로 가져와야 함)
            if (document.getElementById('add_created_by')) document.getElementById('add_created_by').value = "admin"; // 임시값
        }
    } else {
        console.error("오류: 'addModal' 요소를 찾을 수 없습니다.");
    }
}

/**
 * 새 학생 정보를 백엔드로 전송하여 등록합니다.
 */
async function addStudent() {
    const newStudentData = {
        STD_NM: document.getElementById("add_std_nm").value.trim(),
        SCSBJT_CD: document.getElementById("add_scsbjt_cd").value,
        SCH_YR: parseInt(document.getElementById("add_sch_yr").value),
        ENTR_DT: document.getElementById("add_entr_dt").value,
        STD_STAT_CD: document.getElementById("add_std_stat_cd").value,
        STD_ZIP: document.getElementById("add_std_zip").value,
        STD_ADDR: document.getElementById("add_std_addr").value,
        STD_DADDR: document.getElementById("add_std_daddr").value,
        STD_TELNO: document.getElementById("add_std_telno").value.trim(),
        STD_EML_ADDR: document.getElementById("add_std_eml_addr").value.trim(),
        USE_YN: document.getElementById("add_use_yn").value,
        PROFILE_IMAGE_URL: null, // 초기 등록 시 프로필 이미지 없음
        CREATED_BY: document.getElementById("add_created_by").value
    };

    if (
        !newStudentData.STD_NM || !newStudentData.SCSBJT_CD || 
        isNaN(newStudentData.SCH_YR) || !newStudentData.ENTR_DT || !newStudentData.STD_STAT_CD ||
        !newStudentData.STD_TELNO || !newStudentData.STD_EML_ADDR || !newStudentData.CREATED_BY
    ) {
        alert("모든 필수 정보를 입력해주세요.");
        return;
    }
    
    try {
        const response = await fetch('/admin/student', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newStudentData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 등록 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log('학생 등록 성공:', result);
        alert(`학생 ${result.STD_NM}(학번: ${result.STD_NO})이(가) 성공적으로 등록되었습니다!`);

        const addModal = bootstrap.Modal.getInstance(document.getElementById('addModal'));
        if (addModal) { addModal.hide(); }

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
        return;
    }

    try {
        const response = await fetch(`/admin/student/${stdNo}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 삭제 실패! (HTTP ${response.status} ${response.statusText}): ${errorBody}`);
        }

        console.log('학생 삭제 성공:', stdNo);
        alert(`학생 ${stdNo}이(가) 성공적으로 삭제되었습니다.`);

        const { name, dept, status } = getCurrentSearchParams();
        fetchStudents(currentPage, pageSize, name, dept, status);

    } catch (error) {
        console.error("학생 삭제 중 오류 발생:", error);
        alert("학생 삭제 중 오류가 발생했습니다: " + error.message);
    }
}
