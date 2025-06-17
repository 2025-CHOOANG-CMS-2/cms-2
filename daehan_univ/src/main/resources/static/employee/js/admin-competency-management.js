// ✅ DOM 로드 후 역량 목록 로드
// 페이지가 완전히 로드되면 목록만 불러오도록 변경
// 하위 역량별 코멘트/프로그램 추천 로직 제거

document.addEventListener('DOMContentLoaded', () => {
    console.log('역량 항목 관리 페이지 로드됨');
    loadCompetencyList(); // 목록 로딩
});

// 전역 변수 선언
let selectedCompetency = null;    // 선택된 상위 역량 코드
let subModalParentId = null;      // 하위 역량 추가 모달용 상위 코드 저장

/**
 * 상위 역량 목록 가져오기
 */
function loadCompetencyList() {
    fetch('/api/competencies/list')
        .then(res => res.json())
        .then(data => {
            const listGroup = document.querySelector('.list-group');
            listGroup.innerHTML = '';

            data.forEach((comp, index) => {
                const item = document.createElement('a');
                item.href = '#';
                item.className = 'list-group-item list-group-item-action';
                if (index === 0) item.classList.add('active');

                item.onclick = e => {
                    e.preventDefault();
                    selectCompetency(comp.cciId);
                    document.querySelectorAll('.list-group-item')
                        .forEach(el => el.classList.remove('active'));
                    item.classList.add('active');
                };

                item.innerHTML = `
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${comp.cciNm}</h6>
                        <small>${comp.questionCount ?? 0}문항</small>
                    </div>
                    <p class="mb-1">${comp.cciDesc}</p>
                `;
                listGroup.appendChild(item);
            });

            if (data.length > 0) {
                selectedCompetency = data[0].cciId;
                selectCompetency(data[0].cciId);
            }
        })
        .catch(err => {
            console.error('역량 목록 로딩 실패', err);
            alert('역량 목록을 불러오지 못했습니다.');
        });
}

/**
 * 선택된 상위 역량 상세 조회
 */
function selectCompetency(cciId) {
    selectedCompetency = cciId;
    if (!cciId) {
        console.warn("cciId 값이 없습니다. API 호출을 생략합니다.");
        return;
    }
    fetch(`/api/competencies/${cciId}`)
        .then(res => res.json())
        .then(data => updateCompetencyDetails(data))
        .catch(err => console.error('상세 조회 에러', err));
}

/**
 * 상세 정보 렌더링 (하위 역량 목록 포함)
 */
function updateCompetencyDetails(comp) {
    let html = `
      <h5 class="text-primary mb-3">${comp.cciNm}</h5>
      <div class="competency-detail mb-4">
        <div class="row mb-2">
          <div class="col-md-3 fw-bold">역량 코드</div>
          <div class="col-md-9">${comp.cciCode}</div>
        </div>
        <div class="row mb-2">
          <div class="col-md-3 fw-bold">역량 설명</div>
          <div class="col-md-9">${comp.cciDesc}</div>
        </div>
        <div class="row mb-2">
          <div class="col-md-3 fw-bold">가중치</div>
          <div class="col-md-9">${comp.weight ?? 0}%</div>
        </div>
        <div class="row mb-2">
          <div class="col-md-3 fw-bold">문항 수</div>
          <div class="col-md-9">${comp.questionCount ?? 0}문항</div>
        </div>
        <div class="row">
          <div class="col-md-3 fw-bold">표시 색상</div>
          <div class="col-md-9">
            <div style="width:50px;height:25px;background-color:${comp.colorHex ?? '#ccc'};border-radius:5px;"></div>
          </div>
        </div>
      </div>

      <h6 class="mt-4 mb-3">하위 역량 항목</h6>
    `;

    if (Array.isArray(comp.children) && comp.children.length) {
        comp.children.forEach(child => {
            html += `
          <div class="sub-competency-item mb-2">
            <div class="d-flex justify-content-between align-items-center">
              <h6 class="mb-1">${child.cciNm} <small>(${child.weight}%)</small></h6>
              <div>
                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('${child.cciId}')">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('${child.cciId}')">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
            </div>
            <p class="small text-muted mb-0">${child.cciDesc}</p>
          </div>
        `;
        });
    } else {
        html += `<div class="text-center text-muted py-3">하위 역량이 없습니다.</div>`;
    }

    html += `
      <div class="text-center mt-4">
        <button class="btn btn-outline-primary" onclick="openSubCompetencyModal('${comp.cciId}')">
          <i class="fas fa-plus me-2"></i> 하위 역량 추가
        </button>
      </div>
    `;

    document.getElementById('competencyDetails').innerHTML = html;
}

/**
 * 최상위 역량 추가
 */
function addCompetency() {
    const form = document.getElementById('addCompetencyForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const dto = {
        cciNm: document.getElementById('competencyName').value,
        cciDesc: document.getElementById('competencyDescription').value,
        weight: parseInt(document.getElementById('competencyWeight').value, 10),
        colorHex: document.getElementById('competencyColor').value,
        regUserId: 'admin01'
    };

    fetch('/api/competencies/root', {
        method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(dto)
    })
        .then(res => res.json())
        .then(data => {
            alert(`역량 '${data.cciNm}' 등록 (ID: ${data.cciId})`);
            form.reset();
            bootstrap.Modal.getInstance(document.getElementById('addCompetencyModal')).hide();
            loadCompetencyList();
        })
        .catch(err => {
            console.error(err);
            alert('등록 오류');
        });
}

/**
 * 하위 역량 추가 모달 열기
 */
function openSubCompetencyModal(parentCciId) {
    subModalParentId = parentCciId;
    new bootstrap.Modal(document.getElementById('addSubCompetencyModal')).show();
}

/**
 * 하위 역량 등록
 */
function addSubCompetency() {
    const form = document.getElementById('addSubCompetencyForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    if (!subModalParentId) {
        return alert('상위 역량 지정 필요');
    }

    const dto = {
        cciNm: document.getElementById('subCompetencyName').value,
        cciDesc: document.getElementById('subCompetencyDescription').value,
        weight: parseInt(document.getElementById('subCompetencyWeight').value, 10),
        regUserId: 'admin01'
    };

    fetch(`/api/competencies/child/${subModalParentId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(dto)
    })
        .then(res => {
            if (!res.ok) throw new Error("등록 실패");
            return res.json(); // ❗ 서버 응답이 JSON이 아닐 경우 여기서 에러 발생
        })
        .then(added => {
            alert(`하위 역량 '${added.cciNm}' 등록 완료 (ID:${added.cciId})`);
            bootstrap.Modal.getInstance(document.getElementById('addSubCompetencyModal')).hide();
            form.reset();
            selectCompetency(subModalParentId);
        })
        .catch(err => {
            console.error("등록 실패:", err); // 여기서 에러 이유 확인
            alert("하위 역량 등록 중 오류가 발생했습니다.");
        });
}

/**
 * 하위 역량 수정(플레이스홀더)
 */
function editSubCompetency(subId) {
    alert(`하위 역량 ${subId} 수정 실행`);
}

/**
 * 하위 역량 삭제(플레이스홀더)
 */
function deleteSubCompetency(subId) {
    if (confirm('정말 삭제하시겠습니까?')) {
        alert(`하위 역량 ${subId} 삭제됨`);
    }
}

/**
 *  코멘트 관련 js---------------------------------------------------
 * */
// 모달 열기
function openCommentModal() {
    if (!selectedCompetency) {
        alert("상위 역량을 먼저 선택해주세요.");
        return;
    }
    new bootstrap.Modal(document.getElementById('addCommentModal')).show();
}

// 코멘트 저장
function addComment() {
    const min = parseInt(document.getElementById('minScore').value, 10);
    const max = parseInt(document.getElementById('maxScore').value, 10);
    const content = document.getElementById('commentContent').value;

    if (min > max) {
        alert("최소 점수는 최대 점수보다 작거나 같아야 합니다.");
        return;
    }

    const dto = {
        minScore: min,
        maxScore: max,
        content: content
    };

    // 수정 모드인 경우
    if (window.editingCommentId) {
        fetch(`/api/competencies/${selectedCompetency}/comments/${window.editingCommentId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.json())
            .then(() => {
                alert("코멘트가 수정되었습니다.");
                bootstrap.Modal.getInstance(document.getElementById('addCommentModal')).hide();
                document.getElementById('addCommentForm').reset();
                window.editingCommentId = null;
                loadComments(selectedCompetency);
            })
            .catch(err => {
                console.error("코멘트 수정 실패", err);
                alert("코멘트 수정 중 오류 발생");
            });
    } else {
        // 기존 등록 로직
        fetch(`/api/competencies/${selectedCompetency}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        })
            .then(res => res.json())
            .then(() => {
                alert("코멘트가 추가되었습니다.");
                bootstrap.Modal.getInstance(document.getElementById('addCommentModal')).hide();
                document.getElementById('addCommentForm').reset();
                loadComments(selectedCompetency);
            })
            .catch(err => {
                console.error("코멘트 추가 실패", err);
                alert("코멘트 추가 중 오류 발생");
            });
    }
}

/**
 * ✅ 점수 구간 코멘트 목록을 로드하여 화면에 출력
 * @param cciId 상위 역량 ID
 */
function loadComments(cciId) {
    fetch(`/api/competencies/comments/${cciId}`)
        .then(res => {
            console.log("코멘트 등록 api 테스트");
            if (!res.ok) throw new Error("코멘트 조회 실패");
            return res.json();
        })
        .then(list => {
            cachedCommentList = list;  // 💡 로드 시 캐시
            const area = document.getElementById("commentList");
            area.innerHTML = ""; // 기존 내용 초기화

            if (list.length === 0) {
                area.innerHTML = `<div class="text-muted small">등록된 코멘트가 없습니다.</div>`;
                return;
            }

            list.forEach(c => {
                const div = document.createElement("div");
                div.className = "border rounded p-2 mb-1 small d-flex justify-content-between align-items-center";

                div.innerHTML = `
      <div>
        <strong>${c.minScore} ~ ${c.maxScore}점</strong>: ${c.content}
      </div>
      <div class="ms-2">
        <button class="btn btn-sm btn-outline-primary me-1" onclick="editComment(${c.id})">
          <i class="fas fa-edit"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger" onclick="deleteComment(${c.id})">
          <i class="fas fa-trash"></i>
        </button>
      </div>
    `;

                area.appendChild(div);
            });
        })
        .catch(err => {
            console.error("코멘트 로드 실패", err);
            alert("코멘트 목록을 불러오는 중 오류가 발생했습니다.");
        });
}

function editComment(commentId) {
    // commentId로 기존 데이터를 fetch 하거나
    // 이미 로드된 list에서 해당 데이터를 찾아서 모달에 채움 (여기서는 간단하게 list를 유지한다고 가정)

    const comment = cachedCommentList.find(c => c.id === commentId);
    if (!comment) {
        alert("코멘트 데이터를 찾을 수 없습니다.");
        return;
    }

    // 모달 input에 기존 값 채우기
    document.getElementById("minScore").value = comment.minScore;
    document.getElementById("maxScore").value = comment.maxScore;
    document.getElementById("commentContent").value = comment.content;

    // 수정 모드 표시용으로 전역변수에 ID 보관
    window.editingCommentId = commentId;

    // 모달 열기
    new bootstrap.Modal(document.getElementById('addCommentModal')).show();
}

function deleteComment(commentId) {
    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/api/competencies/comments/${commentId}`, {
            method: 'DELETE'
        })
            .then(res => {
                if (!res.ok) throw new Error("삭제 실패");
                alert("삭제 완료");
                loadComments(selectedCompetency);
            })
            .catch(err => {
                console.error("코멘트 삭제 실패", err);
                alert("삭제 중 오류 발생");
            });
    }
}
/**
 *  코멘트 관련  끝 js---------------------------------------------------
 * */

/**
 * 상위 하위 역량 수정/삭제 시작 ---------------------------------------------------------
 */
// ✅ 전역 변수 - 수정 대상 ID
let editingCompetencyId = null;

/**
 * ✅ 상위 역량 수정 버튼 클릭 시 실행
 */
function editCompetency() {
    if (!selectedCompetency) {
        alert("수정할 상위 역량이 선택되지 않았습니다.");
        return;
    }

    // 선택된 상세 정보를 fetch (이미 로드되어 있다면 캐시 활용 가능)
    fetch(`/api/competencies/${selectedCompetency}`)
        .then(res => res.json())
        .then(data => {
            // 모달 input에 값 채우기
            document.getElementById('editCompetencyName').value = data.cciNm;
            document.getElementById('editCompetencyDescription').value = data.cciDesc;
            document.getElementById('editCompetencyWeight').value = data.weight ?? 0;
            document.getElementById('editCompetencyColor').value = data.colorHex ?? '#000000';

            // 수정 대상 ID 보관
            editingCompetencyId = selectedCompetency;

            // 모달 표시
            new bootstrap.Modal(document.getElementById('editCompetencyModal')).show();
        })
        .catch(err => {
            console.error("상세 조회 실패", err);
            alert("상세 정보를 불러오지 못했습니다.");
        });
}

/**
 * ✅ 하위 역량 수정 버튼 클릭 시 실행
 */
function editSubCompetency(subId) {
    console.log(subId);
    fetch(`/api/competencies/${subId}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('editCompetencyName').value = data.cciNm;
            document.getElementById('editCompetencyDescription').value = data.cciDesc;
            document.getElementById('editCompetencyWeight').value = data.weight ?? 0;
            document.getElementById('editCompetencyColor').value = data.colorHex ?? '#000000';

            editingCompetencyId = subId;
            new bootstrap.Modal(document.getElementById('editCompetencyModal')).show();
        })
        .catch(err => {
            console.error("하위 역량 조회 실패", err);
            alert("하위 역량 정보를 불러오지 못했습니다.");
        });
}

/**
 * ✅ 수정 내용 저장
 */
function saveEditedCompetency() {
    const form = document.getElementById('editCompetencyForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const dto = {
        cciNm: document.getElementById('editCompetencyName').value,
        cciDesc: document.getElementById('editCompetencyDescription').value,
        weight: parseInt(document.getElementById('editCompetencyWeight').value, 10),
        colorHex: document.getElementById('editCompetencyColor').value,
        regUserId: 'admin01' // 수정자 ID
    };

    fetch(`/api/competencies/${editingCompetencyId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
    })
        .then(res => {
            if (!res.ok) throw new Error("수정 실패");
            return res.json();
        })
        .then(() => {
            alert("수정이 완료되었습니다.");
            bootstrap.Modal.getInstance(document.getElementById('editCompetencyModal')).hide();
            loadCompetencyList(); // 목록 새로고침
        })
        .catch(err => {
            console.error("수정 실패", err);
            alert("수정 중 오류가 발생했습니다.");
        });
}

/**
 * ✅ 상위 역량 삭제
 */
function deleteCompetency() {
    if (!selectedCompetency) {
        alert("삭제할 상위 역량이 선택되지 않았습니다.");
        return;
    }

    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/api/competencies/${selectedCompetency}`, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) throw new Error("삭제 실패");
                alert("삭제되었습니다.");
                loadCompetencyList();
            })
            .catch(err => {
                console.error("삭제 실패", err);
                alert("삭제 중 오류가 발생했습니다.");
            });
    }
}

/**
 * ✅ 하위 역량 삭제
 */
function deleteSubCompetency(subId) {
    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/api/competencies/${subId}`, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) throw new Error("삭제 실패");
                alert("삭제되었습니다.");
                selectCompetency(selectedCompetency); // 현재 상세 재로드
            })
            .catch(err => {
                console.error("삭제 실패", err);
                alert("삭제 중 오류가 발생했습니다.");
            });
    }
}
/**
 *  상위 하위 역량 수정/삭제 끝 --------------------------------------------------------------------------------
 * */