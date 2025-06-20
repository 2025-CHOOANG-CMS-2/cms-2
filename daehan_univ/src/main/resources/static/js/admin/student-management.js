// /js/admin/student-management.js 파일 내용

// ===============================================
// DOMContentLoaded 이벤트 리스너: HTML 문서가 완전히 로드되고 파싱된 후 실행됩니다.
// 모든 JavaScript 로직과 함수 정의를 이 블록 안에 포함시킵니다.
// ===============================================
document.addEventListener('DOMContentLoaded', function() {

    // ===============================================
    // 전역 변수 및 상수 정의 (DOMContentLoaded 스코프 내에서 접근 가능)
    // ===============================================
    let currentPage = 0; // 현재 페이지 (0부터 시작)
    const pageSize = 10; // 페이지당 항목 수
    let currentEditingStudent = null; // 현재 수정 중인 학생 정보 객체

    // 학과 목록 데이터 (백엔드의 AdminStdService.SCSBJT_MAP과 동일하게 유지)
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
        { code: "016", "name": "공예과" },
        { code: "017", name: "교육학과" },
        { code: "018", name: "식품영양학과" },
        { code: "019", name: "의류학과" },
        { code: "020", name: "성악과" },
        { code: "021", name: "의예과" }
    ];

    // ===============================================
    // 도우미 함수 (Helper Functions) - DOMContentLoaded 스코프
    // ===============================================

    /**
     * 학과 코드를 학과 이름으로 변환합니다.
     */
    function getDeptName(deptCode) {
        const foundDept = DEPT_LIST.find(dept => dept.code === deptCode);
        return foundDept ? foundDept.name : deptCode;
    }

    /**
     * 상태 코드를 한글 라벨로 변환합니다.
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

        if (searchDeptSelect) {
            searchDeptSelect.innerHTML = '<option value="">학과 전체</option>';
            DEPT_LIST.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.code;
                option.textContent = dept.name;
                searchDeptSelect.appendChild(option);
            });
        }

        if (addDeptSelect) {
            addDeptSelect.innerHTML = '<option value="">학과를 선택하세요</option>';
            DEPT_LIST.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.code;
                option.textContent = dept.name;
                addDeptSelect.appendChild(option);
            });
        }

        if (editDeptSelect) {
            editDeptSelect.innerHTML = '';
            DEPT_LIST.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.code;
                option.textContent = dept.name;
                editDeptSelect.appendChild(option);
            });
        }
    }

    /**
     * 현재 URL의 검색 파라미터를 객체로 반환합니다.
     * 이 함수는 검색 필터 UI의 현재 값을 읽어옵니다.
     */
    function getCurrentSearchParams() {
        const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
        const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
        const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
        return { name: searchName, dept: searchDept, status: searchStatus };
    }


    // ===============================================
    // 데이터 로드, 테이블 렌더링 및 페이지네이션 핵심 로직 (DOMContentLoaded 스코프)
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
            
            const pageData = await response.json();

            const students = pageData.content;
            const totalPages = pageData.totalPages;
            const currentPageFromBackend = pageData.number;

            const studentTableBody = document.querySelector('#studentTable tbody'); 
            if (!studentTableBody) {
                console.error("오류: #studentTable tbody 요소를 찾을 수 없습니다.");
                return;
            }
            studentTableBody.innerHTML = '';

            if (students.length === 0) {
                studentTableBody.innerHTML = '<tr><td colspan="7">등록된 학생 정보가 없습니다.</td></tr>';
            } else {
                const rowsHtml = students.map(s => {
                    const studentJsonString = JSON.stringify(s).replace(/"/g, "&quot;");

                    return `
                        <tr>
                            <td>${s.STD_NO || ''}</td>
                            <td>${s.STD_NM || ''}</td>
                            <td>${getDeptName(s.SCSBJT_CD) || ''}</td>
                            <td>${s.SCH_YR || ''}</td>
                            <td><span class="badge ${getStatusBadgeClass(s.STD_STAT_CD)}">${getStatusLabel(s.STD_STAT_CD) || ''}</span></td>
                            <td>${s.STD_TELNO || ''}</td>
                            <td>${s.STD_EML_ADDR || ''}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-secondary" data-student-data='${studentJsonString}' onclick="window.showDetail(JSON.parse(this.dataset.studentData))">
                                    <i class="bi bi-eye"></i> <!-- 상세보기 아이콘 -->
                                </button>
                                <button class="btn btn-sm btn-outline-secondary" data-student-data='${studentJsonString}' onclick="window.openEditModal(JSON.parse(this.dataset.studentData))">
                                    <i class="bi bi-pencil"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');

                studentTableBody.innerHTML = rowsHtml;
            }

            const paginationUl = document.querySelector('.pagination'); 
            if (!paginationUl) {
                console.error("오류: .pagination ul 요소를 찾을 수 없습니다.");
                return;
            }
            paginationUl.innerHTML = '';

            const prevLi = document.createElement('li');
            prevLi.classList.add('page-item');
            if (currentPageFromBackend === 0) {
                prevLi.classList.add('disabled');
            }
            const prevLink = document.createElement('a');
            prevLink.classList.add('page-link');
            prevLink.href = "#";
            prevLink.setAttribute('aria-label', 'Previous');
            prevLink.innerHTML = '<span aria-hidden="true">&laquo;</span>';
            prevLink.onclick = (e) => {
                e.preventDefault();
                const { name, dept, status } = getCurrentSearchParams();
                fetchStudents(currentPageFromBackend - 1, pageSize, name, dept, status);
            };
            paginationUl.appendChild(prevLi).appendChild(prevLink);

            const maxPagesToShow = 5;
            let startPage = Math.max(0, currentPageFromBackend - Math.floor(maxPagesToShow / 2));
            let endPage = Math.min(startPage + maxPagesToShow, totalPages - 1);

            if (endPage - startPage + 1 < maxPagesToShow && totalPages > maxPagesToShow) {
                startPage = Math.max(0, endPage - maxPagesToShow + 1);
            }

            for (let i = startPage; i <= endPage; i++) {
                const li = document.createElement('li');
                li.classList.add('page-item');
                if (i === currentPageFromBackend) {
                    li.classList.add('active');
                }
                const link = document.createElement('a');
                link.classList.add('page-link');
                link.href = "#";
                link.textContent = i + 1;
                link.onclick = (e) => {
                    e.preventDefault();
                    const { name, dept, status } = getCurrentSearchParams();
                    fetchStudents(i, pageSize, name, dept, status);
                };
                paginationUl.appendChild(li).appendChild(link);
            }

            const nextLi = document.createElement('li');
            nextLi.classList.add('page-item');
            if (currentPageFromBackend === totalPages - 1 || totalPages === 0) {
                nextLi.classList.add('disabled');
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
            alert('학생 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요: ' + (error.message || '알 수 없는 오류 발생'));
        }
    }

    /**
     * 검색 필드의 값을 가져와 학생 목록을 새로 필터링하고 첫 페이지부터 표시합니다.
     */
    function filterStudents() {
        currentPage = 0; // 검색 시 항상 첫 페이지로
        const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
        const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
        const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';
        fetchStudents(currentPage, pageSize, searchName, searchDept, searchStatus);
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

        document.getElementById("edit_id").value = student.STD_NO || ''; // 학번은 readOnly
        document.getElementById("edit_name").value = student.STD_NM || '';
        document.getElementById("edit_dept").value = student.SCSBJT_CD || '';
        document.getElementById("edit_year").value = student.SCH_YR || '';
        document.getElementById("edit_admission_date").value = student.ENTR_DT || '';
        document.getElementById("edit_status").value = student.STD_STAT_CD || '';
        document.getElementById("edit_zipcode").value = student.STD_ZIP || '';
        document.getElementById("edit_address").value = student.STD_ADDR || '';
        document.getElementById("edit_address_detail").value = student.STD_DADDR || '';
        document.getElementById("edit_email").value = student.STD_EML_ADDR || '';
        document.getElementById("edit_phone").value = student.STD_TELNO || '';

        const editModal = new bootstrap.Modal(document.getElementById("editModal"));
        editModal.show();
    }

    /**
     * 수정된 학생 정보를 백엔드로 전송하여 저장합니다.
     */
    async function saveStudent() {
        const stdNo = document.getElementById("edit_id")?.value || '';

        const updatedData = {
            STD_ID: currentEditingStudent.STD_ID, // 기존 ID를 유지하여 전송
            STD_NO: stdNo, // DTO에도 포함
            STD_NM: document.getElementById("edit_name")?.value.trim() || '',
            SCSBJT_CD: document.getElementById("edit_dept")?.value || '',
            SCH_YR: parseInt(document.getElementById("edit_year")?.value || '0'),
            ENTR_DT: document.getElementById("edit_admission_date")?.value || '',
            STD_STAT_CD: document.getElementById("edit_status")?.value || '',
            STD_ZIP: document.getElementById("edit_zipcode")?.value || '',
            STD_ADDR: document.getElementById("edit_address")?.value || '',
            STD_DADDR: document.getElementById("edit_address_detail")?.value || '',
            STD_TELNO: document.getElementById("edit_phone")?.value.trim() || '',
            STD_EML_ADDR: document.getElementById("edit_email")?.value.trim() || '',
            USE_YN: currentEditingStudent.USE_YN || 'Y',
            CREATED_BY: currentEditingStudent.CREATED_BY || 'admin',
            PROFILE_IMAGE_URL: currentEditingStudent.PROFILE_IMAGE_URL
        };

        // 프론트엔드 유효성 검사 (HTML에 존재하는 필수 필드만 검사)
        if (
            !updatedData.STD_NM || !updatedData.SCSBJT_CD ||
            isNaN(updatedData.SCH_YR) || !updatedData.ENTR_DT || !updatedData.STD_STAT_CD ||
            !updatedData.STD_TELNO || !updatedData.STD_EML_ADDR
        ) {
            alert("모든 필수 정보를 입력해주세요."); // alert() 사용
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
                let errorMessage = `학생 정보 업데이트 실패! (HTTP ${response.status} ${response.statusText})`;
                try { // 에러 본문이 JSON일 경우 파싱 시도
                    const errorJson = JSON.parse(errorBody);
                    errorMessage = errorJson.message || errorMessage;
                } catch (e) {
                    // JSON이 아닐 경우 원본 텍스트 사용
                    errorMessage += `: ${errorBody}`;
                }
                throw new Error(errorMessage); // 에러 발생 시 throw
            }

            let result = {};
            const contentLength = response.headers.get('Content-Length');
            const contentType = response.headers.get('Content-Type');

            if (contentLength !== '0' && contentType && contentType.includes('application/json')) {
                try {
                    result = await response.json();
                } catch (jsonParseError) {
                    console.warn("학생 정보 업데이트는 성공했으나, JSON 응답 파싱 중 오류가 발생했습니다:", jsonParseError);
                }
            }

            console.log("학생 정보 업데이트 성공:", result);
            alert("학생 정보가 성공적으로 수정되었습니다."); // alert() 사용

            try {
                const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
                if (editModal) { editModal.hide(); }
            } catch (modalHideError) {
                console.error("수정 모달 숨김 중 오류 발생:", modalHideError);
            }
            
            const { name, dept, status } = getCurrentSearchParams();
            fetchStudents(currentPage, pageSize, name, dept, status);


        } catch (error) {
            console.error("학생 정보 업데이트 중 오류 발생:", error);
            alert("학생 정보 업데이트 중 오류가 발생했습니다: " + (error.message || '알 수 없는 오류 발생')); // alert() 사용
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
                if (document.getElementById('add_id')) {
                    document.getElementById('add_id').value = "학번은 자동 생성됩니다.";
                    document.getElementById('add_id').readOnly = true;
                }
                
                if (document.getElementById('add_status')) document.getElementById('add_status').value = "ENROLL";
                if (document.getElementById('add_year')) document.getElementById('add_year').value = 1;
                if (document.getElementById('add_admission_date')) {
                    const today = new Date();
                    const year = today.getFullYear();
                    const month = String(today.getMonth() + 1).padStart(2, '0');
                    const day = String(today.getDate()).padStart(2, '0');
                    document.getElementById('add_admission_date').value = `${year}-${month}-${day}`;
                }

                if (document.getElementById('add_name')) document.getElementById('add_name').value = "";
                if (document.getElementById('add_dept')) document.getElementById('add_dept').value = "";
                if (document.getElementById('add_zipcode')) document.getElementById('add_zipcode').value = "";
                if (document.getElementById('add_address')) document.getElementById('add_address').value = "";
                if (document.getElementById('add_address_detail')) document.getElementById('add_address_detail').value = "";
                if (document.getElementById('add_phone')) document.getElementById('add_phone').value = "";
                if (document.getElementById('add_email')) document.getElementById('add_email').value = "";
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
            STD_NO: null, // 백엔드에서 자동 생성되므로 null로 전송
            STD_NM: document.getElementById("add_name")?.value.trim() || '',
            SCSBJT_CD: document.getElementById("add_dept")?.value || '',
            SCH_YR: parseInt(document.getElementById("add_year")?.value || '0'),
            ENTR_DT: document.getElementById("add_admission_date")?.value || '',
            STD_STAT_CD: document.getElementById("add_status")?.value || '',
            STD_ZIP: document.getElementById("add_zipcode")?.value || '',
            STD_ADDR: document.getElementById("add_address")?.value || '',
            STD_DADDR: document.getElementById("add_address_detail")?.value || '',
            STD_TELNO: document.getElementById("add_phone")?.value.trim() || '',
            STD_EML_ADDR: document.getElementById("add_email")?.value.trim() || '',
            USE_YN: 'Y', // 기본값
            CREATED_BY: 'admin', // 또는 로그인 사용자 ID
            PROFILE_IMAGE_URL: null // 초기에는 프로필 이미지 없음
        };

        // 필수 필드 유효성 검사
        if (
            !newStudentData.STD_NM || !newStudentData.SCSBJT_CD ||
            isNaN(newStudentData.SCH_YR) || !newStudentData.ENTR_DT || !newStudentData.STD_STAT_CD ||
            !newStudentData.STD_TELNO || !newStudentData.STD_EML_ADDR
        ) {
            alert("모든 필수 정보를 입력해주세요."); // alert() 사용
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
                let errorMessage = `학생 등록 실패! (HTTP ${response.status} ${response.statusText})`;
                try {
                    const errorJson = JSON.parse(errorBody);
                    errorMessage = errorJson.message || errorMessage;
                } catch (e) {
                    errorMessage += `: ${errorBody}`;
                }
                throw new Error(errorMessage);
            }

            let result = {};
            const contentLength = response.headers.get('Content-Length');
            const contentType = response.headers.get('Content-Type');

            if (contentLength !== '0' && contentType && contentType.includes('application/json')) {
                try {
                    result = await response.json();
                } catch (jsonParseError) {
                    console.warn("학생 정보 등록은 성공했으나, JSON 응답 파싱 중 오류가 발생했습니다:", jsonParseError);
                }
            }

            console.log("학생 정보 등록 성공:", result);
            alert('학생이 성공적으로 등록되었습니다. 학번: ' + (result.STD_NO || 'N/A')); // alert() 사용

            try {
                const addModal = bootstrap.Modal.getInstance(document.getElementById("addModal"));
                if (addModal) { addModal.hide(); }
            } catch (modalHideError) {
                console.error("등록 모달 숨김 중 오류 발생:", modalHideError);
            }
            
            const { name, dept, status } = getCurrentSearchParams();
            fetchStudents(0, pageSize, name, dept, status); // 등록 후 첫 페이지로 이동하여 새로고침

        } catch (error) {
            console.error("학생 등록 중 오류 발생:", error);
            alert("학생 등록 중 오류가 발생했습니다: " + (error.message || '알 수 없는 오류 발생')); // alert() 사용
        }
    }


    // ===============================================
    // DOMContentLoaded 이벤트 리스너: 페이지 로드 후 초기화 및 이벤트 연결
    // 이 블록은 DOM이 완전히 로드된 후에 한 번만 실행됩니다.
    // 여기에 전역 함수들을 window 객체에 할당하여 HTML onclick에서 접근 가능하도록 합니다.
    // ===============================================

    // 사이드바 및 햄버거 메뉴 이벤트 리스너 설정
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
                submenu.style.display = submenu.style.display === "block" ? "none" : "block";
            }
        });
    });

    // --- 초기화 함수 호출 ---
    populateDeptDropdowns(); // 학과 드롭다운 채우기
    fetchStudents(currentPage, pageSize); // 초기 학생 목록 로드

    // --- 이벤트 리스너 연결 ---
    const filterButton = document.getElementById("filterButton");
    if (filterButton) {
        filterButton.addEventListener("click", filterStudents);
    } else {
        console.warn("경고: HTML에서 ID 'filterButton'을 가진 검색 버튼을 찾을 수 없습니다.");
    }

    // 상세 보기 모달에서 수정 버튼
    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.addEventListener('click', function() {
            if (currentEditingStudent) {
                const detailModalInstance = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
                if (detailModalInstance) { detailModalInstance.hide(); }
                openEditModal(currentEditingStudent);
            }
        });
    }

    // 학생 등록 버튼 (모달 열기)
    const addStudentBtn = document.getElementById('addStudentBtn'); // 이 ID로 버튼이 있다고 가정
    if (addStudentBtn) {
        addStudentBtn.addEventListener('click', openAddModal);
    } else {
        console.warn("경고: 'addStudentBtn' 요소를 찾을 수 없습니다. 학생 추가 기능이 제한될 수 있습니다.");
    }

    // 학생 추가 폼 제출 이벤트
    const addStudentForm = document.getElementById('addForm'); // HTML의 addForm ID로 수정
    if (addStudentForm) {
        addStudentForm.addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 방지
            addStudent();
        });
    } else {
        console.warn("경고: 'addForm' 요소를 찾을 수 없습니다. 학생 추가 기능이 제한될 수 있습니다.");
    }

    // 학생 수정 폼 제출 이벤트
    const editStudentForm = document.getElementById('editForm'); // HTML의 editForm ID로 수정
    if (editStudentForm) {
        editStudentForm.addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 방지
            saveStudent();
        });
    } else {
        console.warn("경고: 'editForm' 요소를 찾을 수 없습니다. 학생 수정 기능이 제한될 수 있습니다.");
    }

    // 프로필 이미지 변경 이벤트 리스너 (Add 모달)
    const addProfileImageInput = document.getElementById('add_profile_image');
    if (addProfileImageInput) {
        addProfileImageInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            const preview = document.getElementById('add_profile_preview');
            if (file && preview) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                };
                reader.readAsDataURL(file);
            } else if (preview) {
                preview.src = '/images/default_profile.png'; // 파일이 선택되지 않으면 기본 이미지
            }
        });
    }

    // 프로필 이미지 변경 이벤트 리스너 (Edit 모달)
    const editProfileImageInput = document.getElementById('edit_profile_image');
    if (editProfileImageInput) {
        editProfileImageInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            const preview = document.getElementById('edit_profile_preview');
            if (file && preview) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                };
                reader.readAsDataURL(file);
            } else if (preview) {
                preview.src = (currentEditingStudent && currentEditingStudent.PROFILE_IMAGE_URL && currentEditingStudent.PROFILE_IMAGE_URL !== 'null') // DTO 필드명으로 수정
                    ? currentEditingStudent.PROFILE_IMAGE_URL
                    : '/images/default_profile.png';
            }
        });
    }

    // 이메일 중복 확인 버튼 이벤트 리스너 (추가 모달)
    const checkAddEmailBtn = document.getElementById('checkAddEmailBtn');
    const addStdEmlAddrInput = document.getElementById('add_eml_addr'); // HTML ID에 맞게 수정
    if (checkAddEmailBtn && addStdEmlAddrInput) {
        checkAddEmailBtn.addEventListener('click', async () => {
            const email = addStdEmlAddrInput.value;
            if (!email) {
                alert('이메일을 입력해주세요.'); // alert() 사용
                return;
            }
            try {
                const response = await fetch(`/api/mypage/check-email?email=${encodeURIComponent(email)}`);
                const result = await response.json();
                if (response.ok) {
                    alert(result.message); // alert() 사용
                } else {
                    alert(result.message); // alert() 사용
                }
            } catch (error) {
                console.error('이메일 중복 확인 중 오류:', error);
                alert('이메일 중복 확인 중 네트워크 오류가 발생했습니다.'); // alert() 사용
            }
        });
    }

    // 이메일 중복 확인 버튼 이벤트 리스너 (수정 모달)
    const checkEditEmailBtn = document.getElementById('checkEditEmailBtn');
    const editStdEmlAddrInput = document.getElementById('edit_eml_addr'); // HTML ID에 맞게 수정
    if (checkEditEmailBtn && editStdEmlAddrInput) {
        checkEditEmailBtn.addEventListener('click', async () => {
            const email = editStdEmlAddrInput.value;
            if (!email) {
                alert('이메일을 입력해주세요.'); // alert() 사용
                return;
            }
            // 현재 수정 중인 학생의 원래 이메일과 비교하여, 변경이 없으면 중복 확인 건너뛰기
            if (currentEditingStudent && currentEditingStudent.STD_EML_ADDR === email) {
                alert('현재 이메일입니다. 변경사항이 없습니다.'); // alert() 사용
                return;
            }

            try {
                const response = await fetch(`/api/mypage/check-email?email=${encodeURIComponent(email)}`);
                const result = await response.json();
                if (response.ok) {
                    alert(result.message); // alert() 사용
                } else {
                    alert(result.message); // alert() 사용
                }
            } catch (error) {
                console.error('이메일 중복 확인 중 오류:', error);
                alert('이메일 중복 확인 중 네트워크 오류가 발생했습니다.'); // alert() 사용
            }
        });
    }

    // ⭐ 중요: HTML의 onclick 속성에서 직접 호출되는 함수들을 window 객체에 할당 ⭐
    // 이 작업이 ReferenceError를 해결하는 핵심입니다.
    window.showDetail = showDetail;
    window.openEditModal = openEditModal;
    window.saveStudent = saveStudent;
    window.openAddModal = openAddModal;
    window.addStudent = addStudent;
    window.filterStudents = filterStudents;
    window.populateDeptDropdowns = populateDeptDropdowns; // 페이지 로드 시 호출될 수 있음

}); // DOMContentLoaded 끝
