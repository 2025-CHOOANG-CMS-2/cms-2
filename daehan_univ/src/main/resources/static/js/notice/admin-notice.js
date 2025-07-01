// 전역 변수 선언
let newNoticeEditor; // 새 공지사항 CKEditor 인스턴스
let editNoticeEditor; // 수정 모달 CKEditor 인스턴스
let newNoticePeriodFlatpickr; // 새 공지사항 게시 기간 Flatpickr 인스턴스
let editNoticePeriodFlatpickr; // 수정 모달 게시 기간 Flatpickr 인스턴스
let periodFilterFlatpickr; // 필터 기간 Flatpickr 인스턴스

// 페이징 관련 전역 변수
let currentPage = 0; // 현재 페이지 (0부터 시작)
let pageSize = 10;   // 페이지당 항목 수
let totalPages = 0;  // 총 페이지 수
let currentSortBy = 'createdAt'; // 현재 정렬 기준 필드
let currentDirection = 'desc';   // 현재 정렬 방향 (desc/asc)
let currentSearchTitle = '';     // 현재 검색 제목 (초기값 빈 문자열)
let currentBoardId = '';         // 현재 카테고리 필터 (초기값 빈 문자열 또는 'all')
let currentIsPublished = '';     // 현재 게시 상태 필터 (초기값 빈 문자열 또는 'all')
let currentIsImportant = false;  // 현재 중요 공지사항 필터 (초기값 false)

// ⭐ 일괄 삭제를 위한 선택된 공지사항 ID 목록 ⭐
let selectedNoticeIds = new Set(); // 중복 방지를 위해 Set 사용

// ⭐ 임시 JWT 토큰 (보안 문제 디버깅용 - 실제 운영에서는 로그인 후 받아와야 함) ⭐
// 백엔드에서 JWT 토큰을 발급하는 로그인 기능이 있다면, 로그인 후 받은 토큰을 여기에 설정해야 합니다.
// 현재 "유효하지 않은 토큰" 에러를 임시로 우회하려면, 백엔드 보안 설정을 일시적으로 완화하거나
// 유효한 임시 토큰을 여기에 하드코딩하여 테스트할 수 있습니다.


// ===========================================
// 유틸리티 및 헬퍼 함수 (모든 DOMContentLoaded 이벤트 리스너보다 위에 정의)
// ===========================================

/**
 * 파일 사이즈 포맷팅 헬퍼 함수
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 카테고리 ID를 이름으로 변환하는 헬퍼 함수
 */
function getCategoryNameById(boardId) {
    switch (boardId) {
        case 1: return '학사공지';
        case 2: return '장학공지';
        case 3: return '취업공지';
        case 4: return '행사공지';
        case 5: return '일반공지';
        default: return '기타';
    }
}

/**
 * ⭐⭐⭐ 모든 모달과 백드롭을 강제로 정리하는 함수 ⭐⭐⭐
 * 이 함수는 모달이 제대로 닫히지 않아 백드롭이 남아있는 문제를 해결합니다.
 */
function cleanupModalsAndBackdrops() {
    console.log('[Cleanup] 모든 모달과 백드롭 정리 시작.');
    // 1. 열려있는 모든 Bootstrap 모달을 숨깁니다.
    document.querySelectorAll('.modal.show').forEach(modalElement => {
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
            modalInstance.hide();
        }
    });

    // 2. 모든 modal-backdrop 요소를 제거합니다.
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.remove();
        console.log('[Cleanup] modal-backdrop 제거됨.');
    });

    // 3. body 태그에서 modal-open 클래스를 제거합니다.
    // 이 클래스가 남아있으면 스크롤이 잠길 수 있습니다.
    document.body.classList.remove('modal-open');
    document.body.style.overflow = ''; // overflow 스타일도 초기화
    document.body.style.paddingRight = ''; // 패딩도 초기화 (스크롤바 공간)

    console.log('[Cleanup] 모든 모달과 백드롭 정리 완료.');
}


// ===========================================
// 파일 첨부 관련 함수
// ===========================================

/**
 * 첨부파일 선택 및 업로드 처리 함수
 * @param {Event} event - 파일 입력 요소의 change 이벤트
 */
async function handleAttachmentSelection(event) {
    console.log('[Attachment] 첨부파일 선택 이벤트 감지.');
    
    // 이 함수는 newNoticeAttachment와 editNoticeAttachment 모두에 연결될 수 있으므로,
    // event.target을 사용하여 어떤 input에서 이벤트가 발생했는지 확인합니다.
    const fileInput = event.target; 

    // ⭐⭐⭐ 이 부분이 핵심입니다! fileInput이 유효한지 먼저 확인합니다. ⭐⭐⭐
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        console.log('[Attachment] 선택된 파일 없음 또는 유효하지 않은 파일 입력 요소.');
        return;
    }

    const file = fileInput.files[0]; // 첫 번째 파일만 가져옴 (multiple 속성 있어도 일단 첫 파일)
    const mode = fileInput.id === 'newNoticeAttachment' ? 'new' : 'edit'; // 어떤 모달인지 판단

    console.log(`[Attachment] Uploading file (${mode} mode): ${file.name}`);

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/admin/attachments/upload', {
            method: 'POST',
            body: formData
            // 'Authorization': `Bearer ${AUTH_TOKEN}` // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });

        if (response.ok) {
            const attachmentResponse = await response.json(); // 응답이 JSON 형식이라고 가정
            console.log('[Attachment] Uploaded successfully:', attachmentResponse);

            // 첨부파일 목록 UI 업데이트 (모드에 따라 다른 목록 사용)
            const attachmentsListId = mode === 'new' ? 'newNoticeAttachmentsList' : 'editAttachmentsList';
            const attachmentsList = document.getElementById(attachmentsListId);
            
            if (attachmentsList) {
                renderAttachmentItem(attachmentResponse.UUID, attachmentResponse.ORIGINAL_FILE_NAME, attachmentsList, mode, attachmentResponse.FILE_SIZE);

                // 숨겨진 UUID 목록 업데이트 (JSON 문자열로 저장)
                const hiddenUuidsInputId = mode === 'new' ? 'newNoticeAttachmentUuids' : 'editNoticeAttachmentUuids';
                const hiddenUuidsInput = document.getElementById(hiddenUuidsInputId);
                if (hiddenUuidsInput) {
                    let currentUuids = [];
                    try {
                        currentUuids = JSON.parse(hiddenUuidsInput.value || '[]');
                    } catch (e) {
                        console.error("Error parsing existing UUIDs:", e);
                        currentUuids = [];
                    }
                    currentUuids.push(attachmentResponse.UUID);
                    hiddenUuidsInput.value = JSON.stringify(currentUuids);
                    console.log(`[Attachment] Updated hidden UUIDs for ${mode} mode:`, hiddenUuidsInput.value);
                }
            } else {
                console.warn(`WARNING: '${attachmentsListId}' 요소를 찾을 수 없습니다.`);
            }

            // 파일 입력 필드 초기화 (동일 파일 재선택 가능하도록)
            fileInput.value = '';

        } else {
            // 서버에서 에러 응답이 왔을 경우 (예: 400 Bad Request, 500 Internal Server Error)
            let errorData = {};
            try {
                errorData = await response.json(); // JSON 파싱 시도
            } catch (e) {
                console.error("Error parsing server error response:", e);
                errorData.message = response.statusText || "알 수 없는 서버 오류";
            }
            const errorMessage = errorData.message || `파일 업로드 실패: ${response.status} ${response.statusText}`;
            alert(`[첨부파일 오류] ${errorMessage}`);
            console.error('[Attachment Error] Error uploading file:', errorMessage, errorData);
        }
    } catch (error) {
        // 네트워크 연결 문제 등으로 API 호출 자체가 실패했을 경우
        alert(`[첨부파일 오류] 네트워크 오류 발생: ${error.message}`);
        console.error('[Attachment Error] Network error during file upload:', error);
    }
}

/**
 * 첨부파일 항목을 DOM에 렌더링하고 삭제 버튼 이벤트 리스너를 추가합니다.
 * @param {string} uuid - 첨부파일 UUID
 * @param {string} fileName - 원본 파일 이름
 * @param {HTMLElement} attachmentsContainer - 첨부파일 목록을 표시할 컨테이너 요소
 * @param {string} mode - 'new' 또는 'edit' (어떤 모달의 첨부파일인지 구분)
 * @param {number} fileSize - 파일 크기 (바이트)
 */
function renderAttachmentItem(uuid, fileName, attachmentsContainer, mode, fileSize = 0) {
    const listItem = document.createElement('div');
    listItem.className = 'd-flex align-items-center justify-content-between mb-2 p-2 border rounded';
    listItem.setAttribute('data-uuid', uuid); // UUID 저장
    listItem.innerHTML = `
        <span>
            <i class="bi bi-file-earmark-text me-2"></i>
            ${fileName} (${formatFileSize(fileSize)})
        </span>
        <button type="button" class="btn btn-sm btn-outline-danger remove-attachment-btn" data-uuid="${uuid}" data-mode="${mode}">
            <i class="bi bi-x"></i>
        </button>
    `;
    attachmentsContainer.appendChild(listItem);

    // 삭제 버튼 이벤트 리스너 추가
    listItem.querySelector('.remove-attachment-btn').addEventListener('click', (e) => {
        const uuidToRemove = e.target.closest('.remove-attachment-btn').dataset.uuid;
        const currentMode = e.target.closest('.remove-attachment-btn').dataset.mode;
        removeAttachment(uuidToRemove, currentMode); // removeAttachment 함수 호출
    });
}

/**
 * 첨부파일을 DOM에서 제거하고 숨겨진 UUID 목록을 업데이트하며, 서버에도 삭제 요청을 보냅니다.
 * @param {string} uuid - 제거할 파일의 UUID
 * @param {string} mode - 'new' 또는 'edit' (어떤 모달의 첨부파일인지 구분)
 */
async function removeAttachment(uuid, mode) {
    console.log(`[Attachment] Removing attachment with UUID: ${uuid}, Mode: ${mode}`);
    
    const attachmentsContainerId = mode === 'new' ? 'newNoticeAttachmentsList' : 'editAttachmentsList';
    const attachmentUuidsInputId = mode === 'new' ? 'newNoticeAttachmentUuids' : 'editNoticeAttachmentUuids';

    const attachmentsContainer = document.getElementById(attachmentsContainerId);
    const attachmentUuidsInput = document.getElementById(attachmentUuidsInputId);

    if (!attachmentsContainer || !attachmentUuidsInput) {
        console.error(`ERROR: 첨부 파일 컨테이너 ('${attachmentsContainerId}') 또는 UUID 입력 필드 ('${attachmentUuidsInputId}')를 찾을 수 없습니다.`);
        return;
    }

    // DOM에서 해당 파일 항목 제거
    const fileItemToRemove = attachmentsContainer.querySelector(`[data-uuid="${uuid}"]`);
    if (fileItemToRemove) {
        fileItemToRemove.remove();
        console.log(`[Attachment] Removed from DOM: ${uuid}`);
    }

    // 숨겨진 input 필드 업데이트 (JSON 문자열의 UUID 배열에서 제거)
    let currentUuids = [];
    try {
        currentUuids = JSON.parse(attachmentUuidsInput.value || '[]');
    } catch (e) {
        console.error("Error parsing existing UUIDs for removal:", e);
        currentUuids = [];
    }
    const updatedUuids = currentUuids.filter(itemUuid => itemUuid !== uuid);
    attachmentUuidsInput.value = JSON.stringify(updatedUuids);
    console.log(`[Attachment] Removed from hidden input. Current list for ${mode}:`, attachmentUuidsInput.value);


    // 서버에서 실제 파일도 삭제 (백엔드에서 Post 업데이트 시 사용되지 않으면 자동 삭제되므로 필수는 아닐 수 있음)
    // 그러나 UI에서 제거하면 서버에서도 제거하는 것이 일관성 있음
    try {
        console.log(`[Attachment] Sending DELETE request to server for UUID: ${uuid}`);
        const response = await fetch(`/api/admin/attachments/${uuid}`, { method: 'DELETE' });
        if (!response.ok) {
            // 서버에서 삭제 실패하더라도 UI에서는 이미 제거되었으므로 경고만 표시
            console.error('[Attachment Error] Failed to delete file from server:', response.statusText);
        } else {
            console.log(`[Attachment] File with UUID ${uuid} deleted from server.`);
        }
    } catch (error) {
        console.error('[Attachment Error] Network error during file upload:', error);
    }
}


// ===========================================
// 공지사항 CRUD 관련 함수
// ===========================================

/**
 * 새 공지사항을 생성하고 서버에 저장합니다.
 * @param {Event} event - 폼 제출 이벤트
 */
async function createNewNotice(event) {
    event.preventDefault(); // 기본 폼 제출 동작 방지

    console.log('2. [Function Call] createNewNotice 함수 시작.');

    const boardId = document.getElementById('newNoticeCategory').value;
    const title = document.getElementById('newNoticeTitle').value;
    const content = newNoticeEditor ? newNoticeEditor.getData() : '';
    const isImportant = document.getElementById('newNoticeImportant').checked;
    const isPublished = document.getElementById('publishImmediately').checked; // 백엔드 DTO에 맞춤

    // Flatpickr에서 노출 기간 값 가져오기
    let startDate = null;
    let endDate = null;

    if (newNoticePeriodFlatpickr && newNoticePeriodFlatpickr.selectedDates.length > 0) {
        startDate = newNoticePeriodFlatpickr.selectedDates[0];
        if (newNoticePeriodFlatpickr.selectedDates.length > 1) {
            endDate = newNoticePeriodFlatpickr.selectedDates[1];
        } else if (newNoticePeriodFlatpickr.config.mode === "range") {
            console.warn('3. [Validation Fail] 노출 기간의 종료일이 선택되지 않았습니다.');
            alert('노출 기간의 시작일과 종료일을 모두 선택해주세요.');
            return;
        }
    } else {
        console.warn('3. [Validation Fail] 노출 기간이 설정되지 않았습니다.');
        alert('노출 기간을 설정해주세요.');
        return;
    }

    // 유효성 검사
    if (!title.trim()) {
        console.warn('3. [Validation Fail] 제목이 비어있습니다.');
        alert('제목을 입력해주세요.');
        return;
    }
    if (!content.trim()) {
        console.warn('3. [Validation Fail] 내용이 비어있습니다.');
        alert('내용을 입력해주세요.');
        return;
    }
    if (!boardId || isNaN(parseInt(boardId))) {
        console.warn('3. [Validation Fail] 카테고리가 선택되지 않았습니다.');
        alert('카테고리를 선택해주세요.');
        return;
    }

    // 날짜 포맷팅 (서버로 보낼 "YYYY-MM-DD" 형식으로)
    const formattedStartDate = startDate ? startDate.toISOString().split('T')[0] : null;
    const formattedEndDate = endDate ? endDate.toISOString().split('T')[0] : null;

    // 첨부파일 UUID 목록 가져오기
    const hiddenUuidsInput = document.getElementById('newNoticeAttachmentUuids');
    let attachmentUuids = [];
    if (hiddenUuidsInput && hiddenUuidsInput.value) {
        try {
            attachmentUuids = JSON.parse(hiddenUuidsInput.value);
        } catch (e) {
            console.error("Error parsing attachment UUIDs from hidden input:", e);
            attachmentUuids = [];
        }
    }

    // 디버깅 로그
    console.log(`Debug: boardId (${typeof boardId}): "${boardId}" -> Parsed: ${parseInt(boardId)}`);
    console.log(`Debug: title (${typeof title}): "${title}"`);
    console.log(`Debug: content (${typeof content}): "${content.substring(0, Math.min(content.length, 50))}${content.length > 50 ? '...' : ''}"`);
    console.log(`Debug: isImportant (${typeof isImportant}): ${isImportant}`);
    console.log(`Debug: isPublished (${typeof isPublished}): ${isPublished}`);
    console.log(`Debug: formattedStartDate (${typeof formattedStartDate}): ${formattedStartDate}`);
    console.log(`Debug: formattedEndDate (${typeof formattedEndDate}): ${formattedEndDate}`);
    console.log(`Debug: attachmentUuids (${typeof attachmentUuids}):`, attachmentUuids);


    // 요청 본문 (JSON) 필드 이름들을 모두 SNAKE_CASE_UPPER로 변경 (백엔드 DTO에 맞춤)
    const requestBody = {
        BOARD_ID: parseInt(boardId),
        TITLE: title,
        CONTENT: content,
        IS_IMPORTANT: isImportant,
        IS_PUBLISHED: isPublished,
        START_DATE: formattedStartDate,
        END_DATE: formattedEndDate,
        ATTACHMENT_UUIDS: attachmentUuids // UUID 배열 전송
    };

    console.log('Request Body (JSON):', JSON.stringify(requestBody, null, 2));

    // API 호출
    try {
        const response = await fetch('/api/admin/posts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
                // 'Authorization': `Bearer ${AUTH_TOKEN}` // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            alert('공지사항이 성공적으로 저장되었습니다.');
            console.log("5. [API Success] 공지사항 저장 성공:", data);
            
            // 모달 닫기 및 폼 초기화
            const newNoticeModalElement = document.getElementById('newNoticeModal');
            if (newNoticeModalElement) {
                const modalInstance = bootstrap.Modal.getInstance(newNoticeModalElement);
                if (modalInstance) {
                    modalInstance.hide(); // Bootstrap hide() 호출
                }
            }
            resetNewNoticeForm(); // 폼 초기화 함수 호출
            loadNotices(); // 공지사항 목록 새로고침
            loadStatistics(); // ⭐ 통계 데이터도 새로고침 ⭐
        } else {
            const errorData = await response.json();
            alert(`공지사항 저장 실패: ${errorData.message || response.statusText}`);
            console.error("5. [API Fail] 공지사항 저장 실패:", errorData);
            console.error("API Error Details:", errorData);
        }
    } catch (error) {
        console.error("6. [Error] 공지사항 저장 중 네트워크 오류 발생:", error);
        alert('공지사항 저장 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 특정 ID의 공지사항 데이터를 불러와 수정 모달을 채웁니다.
 * @param {string} postId - 수정할 공지사항의 ID
 */
async function loadNoticeForEdit(postId) {
    console.log(`[Function Call] loadNoticeForEdit 함수 시작. Post ID: ${postId}`);
    try {
        const response = await fetch(`/api/admin/posts/${postId}`, {
            // headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` } // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });
        if (response.ok) {
            const notice = await response.json();
            console.log('[API Success] 공지사항 데이터 로드 성공:', notice);

            // 폼 필드 채우기
            document.getElementById('editNoticeId').value = notice.POST_ID;
            document.getElementById('editNoticeTitle').value = notice.TITLE;
            document.getElementById('editNoticeCategory').value = notice.BOARD_ID;
            document.getElementById('editNoticeAuthor').value = notice.WRITER_EMPL_ID || 'N/A'; // 작성자 정보
            document.getElementById('editNoticeDepartment').value = notice.MODIFIED_BY || 'N/A'; // 작성부서 (임시로 modifiedBy 사용)

            // CKEditor 내용 설정
            if (editNoticeEditor) {
                editNoticeEditor.setData(notice.CONTENT || '');
            } else {
                document.getElementById('editNoticeContent').value = notice.CONTENT || '';
            }

            // Flatpickr 게시 기간 설정
            if (editNoticePeriodFlatpickr) {
                const startDate = notice.START_DATE;
                const endDate = notice.END_DATE;
                if (startDate && endDate) {
                    editNoticePeriodFlatpickr.setDate([startDate, endDate], true);
                } else if (startDate) {
                    editNoticePeriodFlatpickr.setDate(startDate, true);
                } else {
                    editNoticePeriodFlatpickr.clear();
                }
            }

            document.getElementById('editNoticeImportant').checked = notice.IS_IMPORTANT;
            document.getElementById('editPublishImmediately').checked = notice.IS_PUBLISHED;
            // '임시저장' 체크박스는 '즉시 게시'의 반대 개념으로 사용될 수 있음
            document.getElementById('editSaveAsDraft').checked = !notice.IS_PUBLISHED;


            // 첨부파일 목록 로드 및 표시
            const editAttachmentsList = document.getElementById('editAttachmentsList');
            if (editAttachmentsList) {
                editAttachmentsList.innerHTML = ''; // 기존 목록 초기화
                const hiddenUuidsInput = document.getElementById('editNoticeAttachmentUuids');
                let currentUuids = [];

                if (notice.ATTACHMENTS && notice.ATTACHMENTS.length > 0) {
                    notice.ATTACHMENTS.forEach(attachment => {
                        renderAttachmentItem(attachment.UUID, attachment.ORIGINAL_FILE_NAME, editAttachmentsList, 'edit', attachment.FILE_SIZE);
                        currentUuids.push(attachment.UUID);
                    });
                }
                if (hiddenUuidsInput) {
                    hiddenUuidsInput.value = JSON.stringify(currentUuids);
                }
            }


            // 모달 열기
            const editNoticeModal = new bootstrap.Modal(document.getElementById('editNoticeModal'));
            editNoticeModal.show();

        } else {
            const errorData = await response.json();
            alert(`공지사항 로드 실패: ${errorData.message || response.statusText}`);
            console.error("API Fail: 공지사항 로드 실패:", errorData);
        }
    } catch (error) {
        console.error("Error loading notice for edit:", error);
        alert('공지사항 데이터를 불러오는 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 공지사항을 업데이트하고 서버에 저장합니다.
 * @param {Event} event - 폼 제출 이벤트
 */
async function updateNotice(event) {
    event.preventDefault();

    console.log('[Function Call] updateNotice 함수 시작.');

    const postId = document.getElementById('editNoticeId').value;
    const boardId = document.getElementById('editNoticeCategory').value;
    const title = document.getElementById('editNoticeTitle').value;
    const content = editNoticeEditor ? editNoticeEditor.getData() : '';
    const isImportant = document.getElementById('editNoticeImportant').checked;
    const isPublished = document.getElementById('editPublishImmediately').checked;

    let startDate = null;
    let endDate = null;

    if (editNoticePeriodFlatpickr && editNoticePeriodFlatpickr.selectedDates.length > 0) {
        startDate = editNoticePeriodFlatpickr.selectedDates[0];
        if (editNoticePeriodFlatpickr.selectedDates.length > 1) {
            endDate = editNoticePeriodFlatpickr.selectedDates[1];
        } else if (editNoticePeriodFlatpickr.config.mode === "range") {
            alert('노출 기간의 종료일이 선택되지 않았습니다.');
            return;
        }
    } else {
        alert('노출 기간을 설정해주세요.');
        return;
    }

    if (!title.trim()) {
        alert('제목을 입력해주세요.');
        return;
    }
    if (!content.trim()) {
        alert('내용을 입력해주세요.');
        return;
    }
    if (!boardId || isNaN(parseInt(boardId))) {
        alert('카테고리를 선택해주세요.');
        return;
    }

    const formattedStartDate = startDate ? startDate.toISOString().split('T')[0] : null;
    const formattedEndDate = endDate ? endDate.toISOString().split('T')[0] : null;

    // 첨부파일 UUID 목록 가져오기
    const hiddenUuidsInput = document.getElementById('editNoticeAttachmentUuids');
    let attachmentUuids = [];
    if (hiddenUuidsInput && hiddenUuidsInput.value) {
        try {
            attachmentUuids = JSON.parse(hiddenUuidsInput.value);
        } catch (e) {
            console.error("Error parsing attachment UUIDs from hidden input for edit:", e);
            attachmentUuids = [];
        }
    }


    const requestBody = {
        POST_ID: parseInt(postId),
        BOARD_ID: parseInt(boardId),
        TITLE: title,
        CONTENT: content,
        IS_IMPORTANT: isImportant,
        IS_PUBLISHED: isPublished,
        START_DATE: formattedStartDate,
        END_DATE: formattedEndDate,
        ATTACHMENT_UUIDS: attachmentUuids
    };

    console.log('Update Request Body (JSON):', JSON.stringify(requestBody, null, 2));

    try {
        const response = await fetch(`/api/admin/posts/${postId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
                // 'Authorization': `Bearer ${AUTH_TOKEN}` // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            alert('공지사항이 성공적으로 수정되었습니다.');
            console.log("API Success: 공지사항 수정 성공:", data);
            
            // ⭐ 수정 모달 닫기 (강화된 로직) ⭐
            const editNoticeModal = bootstrap.Modal.getInstance(document.getElementById('editNoticeModal'));
            if (editNoticeModal) {
                editNoticeModal.hide();
            }
            loadNotices(); // 목록 새로고침
            loadStatistics(); // ⭐ 통계 데이터도 새로고침 ⭐
        } else {
            const errorData = await response.json();
            alert(`공지사항 수정 실패: ${errorData.message || response.statusText}`);
            console.error("API Fail: 공지사항 수정 실패:", errorData);
        }
    } catch (error) {
        console.error("Error updating notice:", error);
        alert('공지사항 수정 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 특정 공지사항을 삭제합니다.
 * @param {string} postId - 삭제할 공지사항의 ID
 */
async function deleteNotice(postId) {
    console.log(`[Function Call] deleteNotice 함수 시작. Post ID: ${postId}`);
    try {
        const response = await fetch(`/api/admin/posts/${postId}`, {
            method: 'DELETE'
            // headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` } // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });

        if (response.ok) {
            alert('공지사항이 성공적으로 삭제되었습니다.');
            console.log("API Success: 공지사항 삭제 성공.");
            
            // ⭐ 삭제 확인 모달 닫기 (강화된 로직) ⭐
            const deleteConfirmModal = bootstrap.Modal.getInstance(document.getElementById('deleteNoticeConfirmModal'));
            if (deleteConfirmModal) {
                deleteConfirmModal.hide();
            }
            loadNotices(); // 목록 새로고침
            loadStatistics(); // ⭐ 통계 데이터도 새로고침 ⭐
        } else {
            const errorData = await response.json();
            alert(`공지사항 삭제 실패: ${errorData.message || response.statusText}`);
            console.error("API Fail: 공지사항 삭제 실패:", errorData);
        }
    } catch (error) {
        console.error("Error deleting notice:", error);
        alert('공지사항 삭제 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 공지사항 목록을 서버에서 불러와 화면에 표시합니다.
 */
async function loadNotices() {
    console.log("4. [Function Call] loadNotices 함수 시작.");
    // 현재 페이징 및 필터링 파라미터 가져오기
    const page = currentPage;
    const size = pageSize;
    const sortBy = currentSortBy;
    const direction = currentDirection;
    
    // 검색 및 필터링 UI 요소에서 값 가져오기
    const searchTitle = document.getElementById('searchKeywordInput').value.trim(); // 검색어 입력 필드 ID
    const categoryFilterElement = document.getElementById('categoryFilter');
    const boardId = categoryFilterElement ? categoryFilterElement.value : 'all'; // 카테고리 필터 ID
    
    const statusFilterElement = document.getElementById('statusFilter');
    const isPublished = statusFilterElement ? statusFilterElement.value : 'all'; // 게시 상태 필터 ID (true/false/all)
    
    const importantFilterElement = document.getElementById('importantFilter');
    const isImportant = importantFilterElement ? importantFilterElement.checked : false; // 중요 공지사항 체크박스 ID

    // 쿼리 스트링 구성
    let queryParams = new URLSearchParams({
        page: page,
        size: size,
        sortBy: sortBy,
        direction: direction
    });

    if (searchTitle) {
        queryParams.append('searchTitle', searchTitle);
    }
    if (boardId && boardId !== 'all') { // 'all'은 모든 카테고리
        queryParams.append('boardId', boardId);
    }
    if (isPublished !== 'all') { // 'all'은 모든 상태
        queryParams.append('isPublished', isPublished);
    }
    if (isImportant) { // 체크박스는 true일 때만 전송
        queryParams.append('isImportant', isImportant);
    }

    try {
        const response = await fetch(`/api/admin/posts?${queryParams.toString()}`, {
            // headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` } // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });
        if (response.ok) {
            const pageData = await response.json(); // Page<PostResponse> 객체로 받음
            console.log("4. [API Success] 공지사항 목록 로드 성공:", pageData);
            
            // ⭐ 실제 게시글 목록은 pageData.content에 있습니다. ⭐
            const notices = pageData.content;
            totalPages = pageData.totalPages;
            currentPage = pageData.number; // 백엔드에서 받은 실제 현재 페이지 번호

            displayNotices(notices, pageData.totalElements); // ⭐ totalElements 전달 ⭐
            renderPagination(pageData); // 페이지네이션 UI 업데이트
            
        } else {
            const errorData = await response.json();
            alert(`공지사항 목록 로드 실패: ${errorData.message || response.statusText}`);
            console.error("4. [API Fail] 공지사항 목록 로드 실패:", errorData);
        }
    } catch (error) {
        console.error("4. [Error] 공지사항 목록 로드 중 네트워크 오류 발생:", error);
        alert('공지사항 목록을 불러오는 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 특정 ID의 공지사항 데이터를 불러와 상세보기 모달을 채웁니다.
 * @param {string} postId - 조회할 공지사항의 ID
 */
async function loadNoticeForView(postId) {
    console.log(`[Function Call] loadNoticeForView 함수 시작. Post ID: ${postId}`);
    try {
        const response = await fetch(`/api/admin/posts/${postId}`, {
            // headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` } // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });
        if (response.ok) {
            const notice = await response.json();
            console.log('[API Success] 공지사항 상세 데이터 로드 성공:', notice);

            // 상세보기 모달 필드 채우기
            document.getElementById('viewNoticeTitle').textContent = notice.TITLE || '제목 없음';
            document.getElementById('viewNoticeCategory').textContent = getCategoryNameById(notice.BOARD_ID);
            document.getElementById('viewNoticeAuthor').textContent = notice.WRITER_EMPL_ID || 'N/A';
            document.getElementById('viewNoticeDepartment').textContent = notice.MODIFIED_BY || 'N/A'; // 임시로 modifiedBy 사용
            document.getElementById('viewNoticeDate').textContent = notice.CREATED_AT ? new Date(notice.CREATED_AT).toLocaleDateString('ko-KR') : 'N/A';
            document.getElementById('viewNoticeViews').textContent = notice.VIEW_COUNT || 0;
            document.getElementById('viewNoticeContent').innerHTML = notice.CONTENT || '내용 없음';

            // 첨부파일 목록 채우기
            const viewAttachmentsList = document.getElementById('viewNoticeAttachments');
            if (viewAttachmentsList) {
                viewAttachmentsList.innerHTML = ''; // 기존 목록 초기화
                if (notice.ATTACHMENTS && notice.ATTACHMENTS.length > 0) {
                    notice.ATTACHMENTS.forEach(attachment => {
                        const listItem = document.createElement('li');
                        listItem.innerHTML = `<a href="/api/attachments/download/${attachment.UUID}" target="_blank">${attachment.ORIGINAL_FILE_NAME} (${formatFileSize(attachment.FILE_SIZE)})</a>`;
                        viewAttachmentsList.appendChild(listItem);
                    });
                } else {
                    viewAttachmentsList.innerHTML = '<li>첨부파일 없음</li>';
                }
            }

            // '수정' 및 '삭제' 버튼에 postId 연결
            const viewEditNoticeBtn = document.getElementById('viewEditNoticeBtn');
            if (viewEditNoticeBtn) {
                viewEditNoticeBtn.dataset.noticeId = notice.POST_ID;
                viewEditNoticeBtn.onclick = () => { // 기존 addEventListener 대신 onclick으로 교체 (중복 방지)
                    const viewModalInstance = bootstrap.Modal.getInstance(document.getElementById('viewNoticeModal'));
                    if (viewModalInstance) viewModalInstance.hide(); // 상세보기 모달 닫기
                    loadNoticeForEdit(notice.POST_ID); // 수정 모달 열기
                };
            }

            const viewDeleteNoticeBtn = document.getElementById('viewDeleteNoticeBtn');
            if (viewDeleteNoticeBtn) {
                viewDeleteNoticeBtn.dataset.noticeId = notice.POST_ID;
                viewDeleteNoticeBtn.onclick = () => { // 기존 addEventListener 대신 onclick으로 교체 (중복 방지)
                    const viewModalInstance = bootstrap.Modal.getInstance(document.getElementById('viewNoticeModal'));
                    if (viewModalInstance) viewModalInstance.hide(); // 상세보기 모달 닫기
                    
                    // 삭제 확인 모달 열기 (postId 전달)
                    const confirmDeleteModal = new bootstrap.Modal(document.getElementById('deleteNoticeConfirmModal'));
                    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
                    if (confirmDeleteBtn) {
                        confirmDeleteBtn.dataset.postIdToDelete = notice.POST_ID; // 단일 삭제 ID 설정
                        confirmDeleteBtn.dataset.isBulkDelete = 'false'; // 일괄 삭제 아님
                    }
                    confirmDeleteModal.show();
                };
            }

            // 상세보기 모달 열기
            const viewNoticeModal = new bootstrap.Modal(document.getElementById('viewNoticeModal'));
            viewNoticeModal.show();

        } else {
            const errorData = await response.json();
            alert(`공지사항 상세 로드 실패: ${errorData.message || response.statusText}`);
            console.error("API Fail: 공지사항 상세 로드 실패:", errorData);
        }
    } catch (error) {
        console.error("Error loading notice for view:", error);
        alert('공지사항 상세 데이터를 불러오는 중 네트워크 오류가 발생했습니다.');
    }
}

/**
 * 공지사항 목록을 테이블에 표시합니다.
 * @param {Array<Object>} notices - 표시할 공지사항 객체 배열
 * @param {number} totalElements - 전체 공지사항 수 (페이징과 관계없이 전체)
 */
function displayNotices(notices, totalElements) { // ⭐ totalElements 파라미터 추가 ⭐
    console.log("X. [UI Render] 공지사항 테이블을 다시 그립니다.");
    const noticeTableBody = document.querySelector('.notice-table tbody');
    if (!noticeTableBody) {
        console.error("ERROR: 공지사항 테이블의 tbody 요소를 찾을 수 없습니다.");
        return;
    }

    noticeTableBody.innerHTML = ''; // 기존 내용 지우기
    selectedNoticeIds.clear(); // 목록 다시 그릴 때 선택된 ID 초기화
    updateBulkActionButtonState(); // 버튼 상태 업데이트

    if (!notices || notices.length === 0) { // notices가 null 또는 undefined일 경우도 처리
        noticeTableBody.innerHTML = `
            <tr>
                <td colspan="10" class="text-center py-4">
                    <i class="bi bi-info-circle me-2"></i>표시할 공지사항이 없습니다.
                </td>
            </tr>
        `;
        // ⭐ 총 공지사항 수 및 필터링된 공지사항 수 업데이트 (목록이 비어있을 때) ⭐
        const listTotalNoticesCountElement = document.getElementById('listTotalNoticesCount');
        if (listTotalNoticesCountElement) {
            listTotalNoticesCountElement.textContent = totalElements || 0; // 백엔드에서 받은 전체 수 사용
        }
        const filteredNoticesCountElement = document.getElementById('filteredNoticesCount');
        if (filteredNoticesCountElement) {
            filteredNoticesCountElement.textContent = totalElements || 0; // 백엔드에서 받은 전체 수 사용
        }
        return;
    }

    let noticeNumber = (currentPage * pageSize) + 1; // 페이지에 따른 공지사항 No. 시작점
    notices.forEach(notice => {
        // 날짜 포맷팅 (null 체크 추가)
        const createdAt = notice.CREATED_AT ? new Date(notice.CREATED_AT).toLocaleDateString('ko-KR') : 'N/A';
        // const modifiedAt = notice.MODIFIED_AT ? new Date(notice.MODIFIED_AT).toLocaleDateString('ko-KR') : 'N/A'; // 사용 안 함
        // const startDate = notice.START_DATE ? new Date(notice.START_DATE).toLocaleDateString('ko-KR') : 'N/A'; // 사용 안 함
        // const endDate = notice.END_DATE ? new Date(notice.END_DATE).toLocaleDateString('ko-KR') : 'N/A'; // 사용 안 함

        // 게시 상태 버튼 클래스 결정
        let publishButtonClass = 'btn-outline-secondary';
        let publishButtonText = '미정';
        if (notice.IS_PUBLISHED) {
            publishButtonClass = 'btn-success';
            publishButtonText = '게시';
        } else {
            publishButtonClass = 'btn-warning'; // 또는 btn-secondary
            publishButtonText = '비게시';
        }

        // 카테고리 이름 매핑 (BOARD_ID를 BOARD_NAME으로 변환)
        const categoryName = getCategoryNameById(notice.BOARD_ID);

        const row = document.createElement('tr');
        row.innerHTML = `
            <td><input type="checkbox" class="notice-checkbox" data-notice-id="${notice.POST_ID}"></td>
            <td>${noticeNumber++}</td>
            <td>
                <a href="#" data-bs-toggle="modal" data-bs-target="#viewNoticeModal" data-notice-id="${notice.POST_ID}">
                    ${notice.TITLE}
                </a>
            </td>
            <td>${categoryName}</td>
            <td>${notice.VIEW_COUNT || 0}</td>
            <td>${notice.MODIFIED_BY || 'N/A'}</td> <!-- 작성부서 정보가 없으므로 임시로 modified_by 사용 -->
            <td>${notice.WRITER_EMPL_ID || 'N/A'}</td>
            <td>${createdAt}</td>
            <td>
                <button type="button" class="btn btn-sm ${publishButtonClass}" onclick="togglePublishStatus(${notice.POST_ID}, ${!notice.IS_PUBLISHED})">
                    <i class="bi bi-eye${notice.IS_PUBLISHED ? '' : '-slash'}"></i> ${publishButtonText}
                </button>
            </td>
            <td>
                <button type="button" class="btn btn-sm btn-outline-primary edit-notice-btn" data-bs-toggle="modal" data-bs-target="#editNoticeModal" data-notice-id="${notice.POST_ID}">
                    <i class="bi bi-pencil"></i> 수정
                </button>
                <button type="button" class="btn btn-sm btn-outline-danger delete-notice-btn" data-bs-toggle="modal" data-bs-target="#deleteNoticeConfirmModal" data-notice-id="${notice.POST_ID}">
                    <i class="bi bi-trash"></i> 삭제
                </button>
            </td>
        `;
        noticeTableBody.appendChild(row);
    });

    // 모든 개별 체크박스에 이벤트 리스너 연결
    document.querySelectorAll('.notice-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', handleNoticeCheckboxChange);
    });

    // ⭐ 총 공지사항 수 및 필터링된 공지사항 수 업데이트 (목록이 비어있지 않을 때) ⭐
    const listTotalNoticesCountElement = document.getElementById('listTotalNoticesCount');
    if (listTotalNoticesCountElement) {
        listTotalNoticesCountElement.textContent = totalElements; // 백엔드에서 받은 전체 수 사용
    }
    const filteredNoticesCountElement = document.getElementById('filteredNoticesCount');
    if (filteredNoticesCountElement) {
        filteredNoticesCountElement.textContent = totalElements; // 백엔드에서 받은 전체 수 사용
    }
}

/**
 * 페이지네이션 UI를 렌더링합니다.
 * @param {Object} pageData - 백엔드에서 받은 Page 객체 데이터
 */
function renderPagination(pageData) {
    const paginationContainer = document.getElementById('pagination-container');
    if (!paginationContainer) {
        console.error("ERROR: 'pagination-container' 요소를 찾을 수 없습니다. HTML에 추가해주세요.");
        return;
    }
    paginationContainer.innerHTML = ''; // 기존 페이지네이션 초기화

    const ul = document.createElement('ul');
    ul.className = 'pagination justify-content-center';

    // 이전 페이지 버튼
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${pageData.first ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a>`;
    prevLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (!pageData.first) {
            currentPage--;
            loadNotices();
        }
    });
    ul.appendChild(prevLi);

    // 페이지 번호 버튼들
    // 최대 5개 페이지 번호만 표시 (예: 1 2 3 4 5 또는 ... 3 4 5 6 7 ...)
    let startPage = Math.max(0, pageData.number - 2);
    let endPage = Math.min(pageData.totalPages - 1, pageData.number + 2);

    if (endPage - startPage < 4) { // 표시할 페이지가 5개 미만이면 조정
        if (startPage === 0) {
            endPage = Math.min(pageData.totalPages - 1, 4);
        } else if (endPage === pageData.totalPages - 1) {
            startPage = Math.max(0, pageData.totalPages - 5);
        }
    }


    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === pageData.number ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`; // 페이지 번호는 1부터 시작
        li.addEventListener('click', (e) => {
            e.preventDefault();
            currentPage = i;
            loadNotices();
        });
        ul.appendChild(li);
    }

    // 다음 페이지 버튼
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${pageData.last ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a>`;
    nextLi.addEventListener('click', (e) => {
        e.preventDefault();
        if (!pageData.last) {
            currentPage++;
            loadNotices();
        }
    });
    ul.appendChild(nextLi);

    paginationContainer.appendChild(ul);

    // ⭐ renderPagination에서는 더 이상 totalNoticesCount와 filteredNoticesCount를 직접 업데이트하지 않습니다. ⭐
    // ⭐ 이 부분은 displayNotices 함수에서 pageData.totalElements를 받아서 처리합니다. ⭐
    // document.getElementById('totalNoticesCount').textContent = pageData.totalElements;
    // document.getElementById('filteredNoticesCount').textContent = pageData.totalElements;
}


/**
 * 게시글의 게시 상태를 토글합니다. (실제 API 호출 로직 추가)
 * @param {number} postId - 게시글 ID
 * @param {boolean} newStatus - 변경할 게시 상태 (true: 게시, false: 비게시)
 */
async function togglePublishStatus(postId, newStatus) {
    console.log(`[Function Call] togglePublishStatus: Post ID ${postId}, New Status: ${newStatus}`);
    try {
        const response = await fetch(`/api/admin/posts/${postId}/publish`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json'
                // 'Authorization': `Bearer ${AUTH_TOKEN}` // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
            },
            body: JSON.stringify(newStatus) // boolean 값을 직접 body로 전송
        });
        if (response.ok) {
            alert('게시 상태가 변경되었습니다.');
            loadNotices(); // 목록 새로고침
            loadStatistics(); // ⭐ 통계 데이터도 새로고침 ⭐
        } else {
            const errorData = await response.json();
            alert(`게시 상태 변경 실패: ${errorData.message || response.statusText}`);
            console.error("API Fail: 게시 상태 변경 실패:", errorData);
        }
    } catch (error) {
        console.error("Error toggling publish status:", error);
        alert('게시 상태 변경 중 오류가 발생했습니다.');
    }
}


/**
 * 새 공지사항 작성 폼을 초기화하고 모달을 닫습니다.
 */
function resetNewNoticeForm() {
    console.log("X. [UI Reset] 새 공지사항 폼 초기화 시작.");

    // 폼 필드 초기화
    const createNoticeForm = document.getElementById('createNoticeForm');
    if (createNoticeForm) {
        createNoticeForm.reset(); // 폼의 모든 필드를 기본값으로 초기화
    }

    // CKEditor 내용 초기화
    if (newNoticeEditor) {
        newNoticeEditor.setData('');
    }

    // Flatpickr 날짜 선택 초기화
    if (newNoticePeriodFlatpickr) {
        newNoticePeriodFlatpickr.clear();
    }

    // 첨부파일 목록 UI 초기화
    const newNoticeAttachmentsList = document.getElementById('newNoticeAttachmentsList');
    if (newNoticeAttachmentsList) {
        newNoticeAttachmentsList.innerHTML = '';
    }
    // 숨겨진 attachmentUuids input 초기화
    const attachmentUuidsInput = document.getElementById('newNoticeAttachmentUuids');
    if (attachmentUuidsInput) {
        attachmentUuidsInput.value = '[]'; // JSON 배열 문자열로 초기화
    }

    // 체크박스 초기화 (reset()으로 안되는 경우 명시적으로)
    document.getElementById('newNoticeImportant').checked = false;
    document.getElementById('publishImmediately').checked = false;
    document.getElementById('saveAsDraft').checked = false;

    console.log("X. [UI Reset] 새 공지사항 폼 초기화 완료.");
}

/**
 * 대시보드 통계 데이터를 로드하여 표시합니다.
 */
async function loadStatistics() {
    console.log("[Function Call] loadStatistics 함수 시작.");
    try {
        const response = await fetch('/api/admin/posts/statistics', {
            // headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` } // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
        });
        if (response.ok) {
            const stats = await response.json();
            console.log("[API Success] 통계 데이터 로드 성공:", stats);

            // UI 업데이트
            // HTML에 해당 ID의 요소가 있는지 확인하고 업데이트합니다.
            const totalPostsDisplay = document.getElementById('totalNoticesCountDisplay');
            if (totalPostsDisplay) totalPostsDisplay.textContent = stats.totalPosts;
            
            const activeNoticesCountDisplay = document.getElementById('activeNoticesCountDisplay');
            if (activeNoticesCountDisplay) activeNoticesCountDisplay.textContent = stats.activePosts;
            
            const importantNoticesCountDisplay = document.getElementById('importantNoticesCountDisplay');
            if (importantNoticesCountDisplay) importantNoticesCountDisplay.textContent = stats.importantPosts;
            
            const averageViewCountDisplay = document.getElementById('averageViewCountDisplay');
            if (averageViewCountDisplay) averageViewCountDisplay.textContent = stats.averageViewCount.toFixed(1); // 소수점 한 자리까지 표시

            // 최근 30일 데이터 (선택 사항) - HTML에 해당 ID의 요소가 있다면 업데이트
            const recent30DaysNewPostsDisplay = document.getElementById('recent30DaysNewPostsDisplay');
            if (recent30DaysNewPostsDisplay) recent30DaysNewPostsDisplay.textContent = `최근 30일: +${stats.recent30DaysNewPosts}`;
            
            const recent30DaysViewIncreaseRateDisplay = document.getElementById('recent30DaysViewIncreaseRateDisplay');
            if (recent30DaysViewIncreaseRateDisplay) recent30DaysViewIncreaseRateDisplay.textContent = `최근 30일: +${stats.recent30DaysViewIncreaseRate.toFixed(1)}%`;

            // 활성 및 중요 공지사항 비율 업데이트 (HTML에 해당 ID가 있다면)
            const activeNoticesPercentageDisplay = document.getElementById('activeNoticesPercentageDisplay');
            if (activeNoticesPercentageDisplay && stats.totalPosts > 0) {
                activeNoticesPercentageDisplay.textContent = ((stats.activePosts / stats.totalPosts) * 100).toFixed(1);
            }
            const importantNoticesPercentageDisplay = document.getElementById('importantNoticesPercentageDisplay');
            if (importantNoticesPercentageDisplay && stats.activePosts > 0) {
                importantNoticesPercentageDisplay.textContent = ((stats.importantPosts / stats.activePosts) * 100).toFixed(1);
            }

        } else {
            const errorData = await response.json();
            console.error("API Fail: 통계 데이터 로드 실패:", errorData);
        }
    } catch (error) {
        console.error("Error loading statistics:", error);
    }
}

/**
 * '전체 선택' 체크박스 핸들러
 */
function handleSelectAllNotices() {
    const selectAllCheckbox = document.getElementById('selectAllNotices');
    const noticeCheckboxes = document.querySelectorAll('.notice-checkbox');
    
    selectedNoticeIds.clear(); // 기존 선택 초기화

    noticeCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
        if (checkbox.checked) {
            selectedNoticeIds.add(parseInt(checkbox.dataset.noticeId));
        }
    });
    updateBulkActionButtonState();
}

/**
 * 개별 공지사항 체크박스 핸들러
 */
function handleNoticeCheckboxChange(event) {
    const checkbox = event.target;
    const postId = parseInt(checkbox.dataset.noticeId);

    if (checkbox.checked) {
        selectedNoticeIds.add(postId);
    } else {
        selectedNoticeIds.delete(postId);
    }

    // '전체 선택' 체크박스 상태 업데이트
    const selectAllCheckbox = document.getElementById('selectAllNotices');
    const allNoticeCheckboxes = document.querySelectorAll('.notice-checkbox');
    const allChecked = Array.from(allNoticeCheckboxes).every(cb => cb.checked);
    selectAllCheckbox.checked = allChecked;

    updateBulkActionButtonState();
}

/**
 * '선택 삭제' 버튼 활성화/비활성화 상태 업데이트
 */
function updateBulkActionButtonState() {
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const activateSelectedBtn = document.getElementById('activateSelectedBtn');
    const deactivateSelectedBtn = document.getElementById('deactivateSelectedBtn');

    if (deleteSelectedBtn) {
        deleteSelectedBtn.disabled = selectedNoticeIds.size === 0;
    }
    if (activateSelectedBtn) {
        activateSelectedBtn.disabled = selectedNoticeIds.size === 0;
    }
    if (deactivateSelectedBtn) {
        deactivateSelectedBtn.disabled = selectedNoticeIds.size === 0;
    }
}

/**
 * 선택된 공지사항들을 일괄 삭제합니다.
 */
async function deleteSelectedNotices() {
    if (selectedNoticeIds.size === 0) {
        alert('삭제할 공지사항을 선택해주세요.');
        return;
    }

    // 삭제 확인 모달 열기 (일괄 삭제임을 표시)
    const confirmDeleteModal = new bootstrap.Modal(document.getElementById('deleteNoticeConfirmModal'));
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const modalBody = document.querySelector('#deleteNoticeConfirmModal .modal-body');

    if (modalBody) {
        modalBody.textContent = `선택된 ${selectedNoticeIds.size}개의 공지사항을 정말 삭제하시겠습니까? 삭제된 공지사항은 복구할 수 없습니다.`;
    }
    if (confirmDeleteBtn) {
        confirmDeleteBtn.dataset.isBulkDelete = 'true'; // 일괄 삭제임을 표시
        confirmDeleteBtn.dataset.postIdToDelete = ''; // 단일 삭제 ID는 비워둠
    }
    confirmDeleteModal.show();
}


// ===========================================
// 초기화 및 이벤트 리스너 설정 (DOMContentLoaded)
// (모든 함수 정의 아래에 위치해야 합니다.)
// ===========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log("1. [Event] DOMContentLoaded 이벤트 발생.");

    // ⭐⭐⭐ 사이드바 메뉴 활성화 로직 ⭐⭐⭐
    const currentPath = window.location.pathname; // 현재 페이지의 URL 경로 (예: "/notice")

    // 모든 사이드바 nav-link에서 active 클래스 제거
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
    });

    // 현재 URL 경로에 따라 해당 메뉴에 active 클래스 추가
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        const linkHref = link.getAttribute('href');
        
        if (currentPath === linkHref) { // 정확히 일치하는 경우
            link.classList.add('active');
        } else if (linkHref !== '/' && currentPath.startsWith(linkHref + '/')) { // 현재 경로가 링크 경로로 시작하는 경우 (예: /notice/detail/123 -> /notice)
            link.classList.add('active');
        }
    });
    // ⭐⭐⭐ 사이드바 메뉴 활성화 로직 끝 ⭐⭐⭐


    // CKEditor 초기화 (새 공지사항 모달)
    if (document.getElementById('newNoticeContent')) {
        ClassicEditor
            .create(document.getElementById('newNoticeContent'))
            .then(editor => {
                newNoticeEditor = editor;
                console.log('New Notice CKEditor initialized.');
            })
            .catch(error => {
                console.error('Error initializing new notice CKEditor', error);
            });
    }

    // CKEditor 초기화 (수정 모달)
    if (document.getElementById('editNoticeContent')) {
        ClassicEditor
            .create(document.getElementById('editNoticeContent'))
            .then(editor => {
                editNoticeEditor = editor;
                console.log('Edit Notice CKEditor initialized.');
            })
            .catch(error => {
                console.error('Error initializing edit notice CKEditor', error);
            });
    }

    // Flatpickr 초기화 (새 공지사항 모달)
    const newNoticePeriodInput = document.getElementById('newNoticeExposurePeriod');
    if (newNoticePeriodInput) {
        newNoticePeriodFlatpickr = flatpickr(newNoticePeriodInput, {
            mode: "range",
            dateFormat: "Y-m-d",
            locale: "ko",
            altInput: true,
            altFormat: "Y년 n월 j일",
            onClose: function(selectedDates, dateStr, instance) {
                if (selectedDates.length === 1 && instance.config.mode === "range") {
                    // 범위 모드에서 시작일만 선택하고 닫았을 경우, 종료일을 시작일과 동일하게 설정
                    instance.setDate([selectedDates[0], selectedDates[0]], false);
                }
            }
        });
    }

    // Flatpickr 초기화 (수정 모달)
    const editNoticePeriodInput = document.getElementById('editNoticePeriod');
    if (editNoticePeriodInput) {
        editNoticePeriodFlatpickr = flatpickr(editNoticePeriodInput, {
            mode: "range",
            dateFormat: "Y-m-d",
            locale: "ko",
            altInput: true,
            altFormat: "Y년 n월 j일",
            onClose: function(selectedDates, dateStr, instance) {
                if (selectedDates.length === 1 && instance.config.mode === "range") {
                    instance.setDate([selectedDates[0], selectedDates[0]], false);
                }
            }
        });
    }

    // Flatpickr 초기화 (필터)
    const periodFilterInput = document.getElementById('periodFilter');
    if (periodFilterInput) {
        periodFilterFlatpickr = flatpickr(periodFilterInput, {
            mode: "range",
            dateFormat: "Y-m-d",
            locale: "ko",
            altInput: true,
            altFormat: "Y년 n월 j일"
        });
    }

    // 필터 초기화 버튼 이벤트 리스너
    const clearDateBtn = document.getElementById('clearDateBtn');
    if (clearDateBtn) {
        clearDateBtn.addEventListener('click', () => {
            if (periodFilterFlatpickr) {
                periodFilterFlatpickr.clear();
            }
            document.getElementById('searchKeywordInput').value = '';
            document.getElementById('categoryFilter').value = 'all';
            document.getElementById('statusFilter').value = 'all';
            document.getElementById('importantFilter').checked = false;
            currentPage = 0; // 필터 초기화 시 첫 페이지로 이동
            loadNotices();
        });
    }

    // 공지사항 저장 버튼 이벤트 리스너
    const saveNewNoticeBtn = document.getElementById('saveNewNoticeBtn');
    if (saveNewNoticeBtn) {
        saveNewNoticeBtn.addEventListener('click', createNewNotice);
    }

    // 공지사항 수정 버튼 이벤트 리스너 (테이블 내의 '수정' 버튼들) - 이벤트 위임
    document.querySelector('.notice-table tbody').addEventListener('click', (event) => {
        const editBtn = event.target.closest('.edit-notice-btn');
        if (editBtn) {
            const postId = editBtn.dataset.noticeId;
            loadNoticeForEdit(postId);
        }
    });

    // 공지사항 삭제 버튼 이벤트 리스너 (테이블 내의 '삭제' 버튼들) - 이벤트 위임
    document.querySelector('.notice-table tbody').addEventListener('click', (event) => {
        const deleteBtn = event.target.closest('.delete-notice-btn');
        if (deleteBtn) {
            const postId = deleteBtn.dataset.noticeId;
            // 삭제 확인 모달에 postId 전달
            const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
            if (confirmDeleteBtn) {
                confirmDeleteBtn.dataset.postIdToDelete = postId; // 단일 삭제 ID 설정
                confirmDeleteBtn.dataset.isBulkDelete = 'false'; // 일괄 삭제 아님
            }
        }
    });

    // ⭐⭐ 삭제 확인 모달의 '삭제' 버튼 이벤트 리스너 (단일/일괄 삭제 분기) ⭐⭐
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', async (event) => {
            const isBulkDelete = event.target.dataset.isBulkDelete === 'true';
            if (isBulkDelete) {
                // 일괄 삭제 로직
                if (selectedNoticeIds.size > 0) {
                    try {
                        const response = await fetch('/api/admin/posts/bulk-delete', {
                            method: 'DELETE',
                            headers: {
                                'Content-Type': 'application/json'
                                // 'Authorization': `Bearer ${AUTH_TOKEN}` // ⭐ 인증 토큰 추가 (필요시 주석 해제) ⭐
                            },
                            body: JSON.stringify(Array.from(selectedNoticeIds)) // Set을 배열로 변환하여 전송
                        });

                        if (response.ok) {
                            alert('선택된 공지사항이 성공적으로 삭제되었습니다.');
                            console.log("API Success: 일괄 삭제 성공.");
                            const deleteConfirmModal = bootstrap.Modal.getInstance(document.getElementById('deleteNoticeConfirmModal'));
                            if (deleteConfirmModal) {
                                deleteConfirmModal.hide();
                            }
                            loadNotices(); // 목록 새로고침
                            loadStatistics(); // ⭐ 통계 데이터도 새로고침 ⭐
                        } else {
                            const errorData = await response.json();
                            alert(`일괄 삭제 실패: ${errorData.message || response.statusText}`);
                            console.error("API Fail: 일괄 삭제 실패:", errorData);
                        }
                    } catch (error) {
                        console.error("Error during bulk delete:", error);
                        alert('일괄 삭제 중 네트워크 오류가 발생했습니다.');
                    }
                } else {
                    alert('삭제할 공지사항이 선택되지 않았습니다.');
                }
            } else {
                // 단일 삭제 로직
                const postId = event.target.dataset.postIdToDelete;
                if (postId) {
                    deleteNotice(postId);
                }
            }
        });
    }

    // 공지사항 상세보기 링크 이벤트 리스너 (테이블 내의 제목 링크) - 이벤트 위임
    document.querySelector('.notice-table tbody').addEventListener('click', (event) => {
        const viewLink = event.target.closest('a[data-bs-target="#viewNoticeModal"]');
        if (viewLink) {
            event.preventDefault(); // 기본 링크 동작 방지
            const postId = viewLink.dataset.noticeId;
            loadNoticeForView(postId); // 상세보기 데이터 로드 함수 호출
        }
    });

    // 검색 및 필터링 이벤트 리스너
    const searchButton = document.getElementById('searchButton');
    if (searchButton) {
        searchButton.addEventListener('click', () => {
            currentPage = 0; // 검색 시 첫 페이지로 이동
            loadNotices();
        });
    }
    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            currentPage = 0;
            loadNotices();
        });
    }
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            currentPage = 0;
            loadNotices();
        });
    }
    const importantFilter = document.getElementById('importantFilter');
    if (importantFilter) {
        importantFilter.addEventListener('change', () => {
            currentPage = 0;
            loadNotices();
        });
    }

    // ⭐⭐ '전체 선택' 체크박스 이벤트 리스너 ⭐⭐
    const selectAllNoticesCheckbox = document.getElementById('selectAllNotices');
    if (selectAllNoticesCheckbox) {
        selectAllNoticesCheckbox.addEventListener('change', handleSelectAllNotices);
    }

    // ⭐⭐ '선택 삭제' 버튼 이벤트 리스너 ⭐⭐
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', deleteSelectedNotices);
    }
    // 초기 상태 업데이트
    updateBulkActionButtonState();

    // 공지사항 목록 로드 (페이지 로드 시)
    loadNotices();
    // 통계 데이터 로드 (페이지 로드 시)
    loadStatistics();

    // 사이드바 토글
    const hamburger = document.getElementById('hamburger');
    const sidebar = document.getElementById('sidebar');
    if (hamburger && sidebar) {
        hamburger.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // 서브메뉴 토글
    document.querySelectorAll('.nav-link.has-submenu').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const submenuId = `submenu-${this.dataset.menu}`;
            const submenu = document.getElementById(submenuId);
            if (submenu) {
                submenu.style.display = submenu.style.display === 'block' ? 'none' : 'block';
            }
        });
    });

    // 모든 모달이 닫힐 때 폼 초기화 및 백드롭 정리
    const newNoticeModal = document.getElementById('newNoticeModal');
    if (newNoticeModal) {
        newNoticeModal.addEventListener('hidden.bs.modal', () => {
            resetNewNoticeForm();
            cleanupModalsAndBackdrops();
        });
    }
    const editNoticeModal = document.getElementById('editNoticeModal');
    if (editNoticeModal) {
        editNoticeModal.addEventListener('hidden.bs.modal', () => {
            cleanupModalsAndBackdrops();
        });
    }
    const viewNoticeModal = document.getElementById('viewNoticeModal');
    if (viewNoticeModal) {
        viewNoticeModal.addEventListener('hidden.bs.modal', () => {
            cleanupModalsAndBackdrops();
            loadNotices(); // ⭐ 상세보기 모달 닫힘 시 목록 새로고침 ⭐
        });
    }
    const deleteNoticeConfirmModal = document.getElementById('deleteNoticeConfirmModal');
    if (deleteNoticeConfirmModal) {
        deleteNoticeConfirmModal.addEventListener('hidden.bs.modal', () => {
            cleanupModalsAndBackdrops();
        });
    }

    // 첨부파일 입력 요소 이벤트 리스너 설정
    const attachmentInput = document.getElementById('newNoticeAttachment');
    if (attachmentInput) {
        attachmentInput.addEventListener('change', handleAttachmentSelection);
    } else {
        console.warn("WARNING: ID 'newNoticeAttachment'를 가진 첨부파일 입력 요소를 찾을 수 없습니다. HTML을 확인하세요.");
    }

    const editAttachmentInput = document.getElementById('editNoticeAttachment');
    if (editAttachmentInput) {
        editAttachmentInput.addEventListener('change', handleAttachmentSelection); 
    } else {
        console.warn("WARNING: ID 'editNoticeAttachment'를 가진 첨부파일 입력 요소를 찾을 수 없습니다. HTML을 확인하세요.");
    }
});



