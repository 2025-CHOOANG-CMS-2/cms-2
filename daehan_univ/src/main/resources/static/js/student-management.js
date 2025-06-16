// /js/student-management.js 파일 내용

// ===============================================
// 사이드바 및 서브메뉴 토글 로직
// ===============================================
const sidebar = document.getElementById("sidebar");
const hamburger = document.getElementById("hamburger");
if (hamburger && sidebar) {
    hamburger.addEventListener("click", () => {
        sidebar.classList.toggle("active");
    });
}

// ===============================================
// 전역 변수 (필요시)
// ===============================================
let currentPage = 0;
const pageSize = 10;
let currentEditingStudent = null;

// ===============================================
// 도우미 함수 (Helper Functions)
// ===============================================

// ✨ 학과 코드를 이름으로 변환 (실제 백엔드 코드와 매핑되도록 업데이트 필요)
// 백엔드의 SCSBJT_CD 값과 매핑
function getDeptName(deptCode) {
    const deptMap = {
        "CSE": "컴퓨터공학과", // 예시: 실제 DB 값으로 변경
        "ME": "기계공학과",
        "LAW": "법학과",
        // ... 실제 DB에 있는 scsbjtCd 값으로 매핑해주세요.
    };
    return deptMap[deptCode] || deptCode; // 매핑되지 않으면 코드값 그대로 반환
}

// ✨ 상태 코드를 라벨로 변환 (실제 백엔드 코드와 매핑되도록 업데이트 필요)
// 백엔드의 STD_STAT_CD 값과 매핑
function getStatusLabel(statusCode) {
    const statusMap = {
        "ACTIVE": "재학", // 예시: 실제 DB 값으로 변경
        "LEAVE": "휴학",
        "GRAD": "졸업",
        // ... 실제 DB에 있는 statusCode 값으로 매핑해주세요.
    };
    return statusMap[statusCode] || statusCode;
}

// ✨ 상태에 따른 배지 클래스 반환 (CSS에 정의된 클래스와 일치해야 함)
function getStatusBadgeClass(status) {
    switch (status) {
        case "ACTIVE": // 재학 (예시: 실제 DB 값으로 변경)
            return "status-enroll";
        case "LEAVE": // 휴학
            return "status-leave";
        case "GRAD": // 졸업
            return "status-grad";
        default:
            return "status-default";
    }
}

// ===============================================
// 데이터 로드, 테이블 렌더링 및 페이지네이션
// ===============================================

document.addEventListener('DOMContentLoaded', () => {
    fetchStudents(currentPage, pageSize);
    
    const searchButton = document.getElementById("searchButton");
    if (searchButton) {
        searchButton.addEventListener("click", () => {
            fetchStudents(0, pageSize);
        });
    }

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

async function fetchStudents(page = 0, size = 10) {
    const searchName = document.getElementById("searchName") ? document.getElementById("searchName").value : '';
    const searchDept = document.getElementById("searchDept") ? document.getElementById("searchDept").value : '';
    const searchStatus = document.getElementById("searchStatus") ? document.getElementById("searchStatus").value : '';

    const apiUrl = `/admin/student?page=${page}&size=${size}`;

    try {
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
        
        const pageData = await response.json();

        const students = pageData.content;
        const totalPages = pageData.totalPages;
        const currentPageFromBackend = pageData.number;

        // 1. 테이블 데이터 업데이트
        const studentTableBody = document.querySelector('#studentTable tbody'); 
        if (!studentTableBody) {
            console.error("Error: #studentTable tbody not found.");
            return;
        }
        studentTableBody.innerHTML = '';

        if (students.length === 0) {
            studentTableBody.innerHTML = '<tr><td colspan="13">등록된 학생 정보가 없습니다.</td></tr>';
        } else {
            const rowsHtml = students.map(s => {
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

            studentTableBody.innerHTML = rowsHtml;
        }

        // 2. 페이징 UI 업데이트
        const paginationUl = document.querySelector('.pagination'); 
        if (!paginationUl) {
            console.error("Error: .pagination ul not found.");
            return;
        }
        paginationUl.innerHTML = ''; 

        // '이전' 버튼 생성
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
            if (currentPageFromBackend > 0) {
                fetchStudents(currentPageFromBackend - 1, size);
            }
        };
        prevLi.appendChild(prevLink);
        paginationUl.appendChild(prevLi);

        // 페이지 번호 버튼들 생성
        let startPage = Math.max(0, currentPageFromBackend - 2);
        let endPage = Math.min(totalPages - 1, currentPageFromBackend + 2);

        if (endPage - startPage < 4) {
            startPage = Math.max(0, endPage - 4);
            endPage = Math.min(totalPages - 1, startPage + 4);
        }
        if (startPage > 0) {
            const li = document.createElement('li');
            li.classList.add('page-item');
            const link = document.createElement('a');
            link.classList.add('page-link');
            link.href = "#";
            link.textContent = '1';
            link.onclick = (e) => { e.preventDefault(); fetchStudents(0, size); };
            li.appendChild(link);
            paginationUl.appendChild(li);
            if (startPage > 1) {
                const ellipsis = document.createElement('li');
                ellipsis.classList.add('page-item', 'disabled');
                ellipsis.innerHTML = '<span class="page-link">...</span>';
                paginationUl.appendChild(ellipsis);
            }
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
                fetchStudents(i, size); 
            };
            li.appendChild(link);
            paginationUl.appendChild(li);
        }

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
            link.onclick = (e) => { e.preventDefault(); fetchStudents(totalPages - 1, size); };
            li.appendChild(link);
            paginationUl.appendChild(li);
        }

        // '다음' 버튼 생성
        const nextLi = document.createElement('li');
        nextLi.classList.add('page-item');
        if (currentPageFromBackend === totalPages - 1) {
            nextLi.classList.add('disabled'); 
        }
        const nextLink = document.createElement('a');
        nextLink.classList.add('page-link');
        nextLink.href = "#";
        nextLink.setAttribute('aria-label', 'Next');
        nextLink.innerHTML = '<span aria-hidden="true">&raquo;</span>';
        nextLink.onclick = (e) => {
            e.preventDefault();
            if (currentPageFromBackend < totalPages - 1) {
                fetchStudents(currentPageFromBackend + 1, size); 
            }
        };
        nextLi.appendChild(nextLink);
        paginationUl.appendChild(nextLink); // 여기서 nextLink를 두 번 추가하는 오류 수정: nextLi를 추가해야 함
        // 수정: paginationUl.appendChild(nextLi); 
        // 기존 코드: paginationUl.appendChild(nextLink);
        paginationUl.appendChild(nextLi);


    } catch (error) {
        console.error('학생 목록을 가져오는 중 오류 발생:', error);
        alert('학생 목록을 불러오지 못했습니다. 서버 상태를 확인해주세요.');
    }
}

// ===============================================
// 상세 보기 모달 (`detailModal`)
// ===============================================

function showDetail(student) {
    currentEditingStudent = student;

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
              <span class="status-badge ${getStatusBadgeClass(student.STD_STAT_CD)}">
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

    const editFromDetailBtn = document.getElementById("editFromDetailBtn");
    if (editFromDetailBtn) {
        editFromDetailBtn.onclick = function () {
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

function openEditModal(student) {
    currentEditingStudent = student;

    // ✨ 폼 필드에 DTO 필드명에 맞춰서 데이터 채우기 (대문자 스네이크 케이스)
    document.getElementById("edit_id").value = student.STD_NO || '';
    document.getElementById("edit_name").value = student.STD_NM || '';
    document.getElementById("edit_dept").value = student.SCSBJT_CD || '';
    document.getElementById("edit_year").value = student.SCH_YR || '';
    document.getElementById("edit_status").value = student.STD_STAT_CD || '';
    document.getElementById("edit_admission_date").value = student.ENTR_DT || '';
    document.getElementById("edit_zipcode").value = student.STD_ZIP || '';
    document.getElementById("edit_address").value = student.STD_ADDR || '';
    document.getElementById("edit_address_detail").value = student.STD_DADDR || '';
    document.getElementById("edit_email").value = student.STD_EML_ADDR || '';
    document.getElementById("edit_phone").value = student.STD_TELNO || '';

    // USER_ID2 필드도 DTO에 따라 추가해야 할 수 있습니다.
    // document.getElementById("edit_user_id2").value = student.USER_ID2 || '';


    const detailModal = bootstrap.Modal.getInstance(document.getElementById("detailModal"));
    if (detailModal) {
        detailModal.hide();
    }

    new bootstrap.Modal(document.getElementById("editModal")).show();
}

async function saveStudent() {
    const studentStdNo = document.getElementById("edit_id").value;
    
    // 백엔드 DTO 필드명에 맞춰서 데이터 구성 (StdInfoDto)
    const updatedStudentData = {
        "STD_NO": studentStdNo,
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
        // 필요에 따라 USER_ID2, USE_YN 등 추가
        "USER_ID2": currentEditingStudent.USER_ID2, // 기존 값을 유지하거나 수정 필드를 추가해야 함
        "USE_YN": currentEditingStudent.USE_YN // 기존 값을 유지하거나 수정 필드를 추가해야 함
    };

    try {
        const response = await fetch(`/admin/student/${studentStdNo}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedStudentData)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 정보 업데이트 실패! (${response.status} ${response.statusText}): ${errorBody}`);
        }

        const result = await response.json();
        console.log("학생 정보 업데이트 성공:", result);
        alert("학생 정보가 성공적으로 수정되었습니다.");

        const editModal = bootstrap.Modal.getInstance(document.getElementById("editModal"));
        if (editModal) {
            editModal.hide();
        }

        fetchStudents(currentPage, pageSize); 

    } catch (error) {
        console.error("학생 정보 업데이트 중 오류 발생:", error);
        alert("학생 정보 업데이트 중 오류가 발생했습니다: " + error.message);
    }
}

// ===============================================
// 학생 등록 모달 (`addModal`) 관련 함수
// ===============================================

function openAddModal() {
    console.log("학생 등록 모달 열기 요청됨.");
    const addModalElement = document.getElementById('addModal');
    if (addModalElement) {
        const addModal = new bootstrap.Modal(addModalElement);
        addModal.show();

        const addForm = document.getElementById('addForm');
        if (addForm) {
            addForm.reset();
            if (document.getElementById('add_id')) document.getElementById('add_id').value = "학과 선택 시 자동 생성";
            if (document.getElementById('add_year')) document.getElementById('add_year').value = "1";
            if (document.getElementById('add_status')) document.getElementById('add_status').value = "STD_STAT_CD01";
            
            if (document.getElementById('add_email')) document.getElementById('add_email').value = "";
            if (document.getElementById('add_dept')) document.getElementById('add_dept').value = "";
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
        "USER_ID2": "user01",
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

        fetchStudents(0, pageSize);

    } catch (error) {
        console.error('학생 등록 중 오류 발생:', error);
        alert('학생 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\n상세: ' + error.message);
    }
}

// ===============================================
// 학생 삭제 (추가된 기능)
// ===============================================
async function deleteStudent(stdNo) {
    if (!confirm(`${stdNo} 학생을 정말 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(`/admin/student/${stdNo}`, { 
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`학생 삭제 실패! (${response.status} ${response.statusText}): ${errorBody}`);
        }

        console.log('학생 삭제 성공:', stdNo);
        alert(`학생 ${stdNo}이(가) 성공적으로 삭제되었습니다.`);

        fetchStudents(currentPage, pageSize);

    } catch (error) {
        console.error("학생 삭제 중 오류 발생:", error);
        alert("학생 삭제 중 오류가 발생했습니다: " + error.message);
    }
}