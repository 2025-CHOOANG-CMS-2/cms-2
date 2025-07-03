// 현재 시간 표시
function updateTime() {
    const now = new Date();
    const timeString = now.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    document.getElementById('currentTime').textContent = timeString;
}

setInterval(updateTime, 1000);
updateTime();

// 사이드바 토글
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('show');
}

// Custom Message Modal 함수 (alert 대신 사용)
function showMessageModal(message, title = '알림') {
    const modalTitle = document.getElementById('messageModalLabel');
    const modalBody = document.getElementById('messageModalBody'); 
    const messageModalElement = document.getElementById('messageModal'); 

    if (modalTitle && modalBody && messageModalElement) {
        modalTitle.textContent = title;
        modalBody.textContent = message;
        const messageModal = new bootstrap.Modal(messageModalElement);
        messageModal.show();
    } else {
        console.error("Error: Message modal elements not found. Cannot display modal. Falling back to alert.");
        alert(`${title}: ${message}`); // 폴백 (디버깅용)
    }
}

// 프로필 사진 미리보기 및 서버 업로드 로직
async function handleProfileImageUpload(event) {
    const file = event.target.files[0];
    if (!file) {
        console.log('선택된 파일이 없습니다.');
        return;
    }

    const employeeIdEl = document.getElementById('employeeId'); 
    if (!employeeIdEl || !employeeIdEl.value) {
        showMessageModal('교직원 ID를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        console.error("Error: employeeId input element not found or empty.");
        return;
    }
    const employeeId = employeeIdEl.value;

    // 1. 파일 미리보기 즉시 적용 (UX 개선)
    const reader = new FileReader();
    reader.onload = function(e) {
        const avatarIconElement = document.getElementById('avatarIcon');
        const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');

        if (profileAvatarDiv) {
            profileAvatarDiv.style.backgroundImage = `url(${e.target.result})`;
            profileAvatarDiv.style.backgroundSize = 'cover';
            profileAvatarDiv.style.backgroundPosition = 'center';
            if (avatarIconElement) {
                avatarIconElement.style.display = 'none'; 
            }
            console.log('DEBUG: 선택된 프로필 사진 미리보기 완료 (로컬 이미지).');
        } else {
            console.error("Error: .profile-avatar element not found for preview.");
        }
    };
    reader.readAsDataURL(file);

    // 2. 백엔드로 파일 전송 (가상 API 엔드포인트 사용)
    const formData = new FormData();
    formData.append('file', file);
    formData.append('employeeId', employeeId); 

    try {
        console.log('DEBUG: 프로필 사진 업로드 요청 중...');
        const response = await fetch('/api/mypage/employee/uploadProfileImage', {
            method: 'POST',
            body: formData,
        });

        const result = await response.json();

        if (response.ok) {
            showMessageModal(result.message || '프로필 사진이 성공적으로 업데이트되었습니다.');
            if (result.imageUrl) {
                const avatarIconElement = document.getElementById('avatarIcon');
                const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');
                if (profileAvatarDiv) {
                    profileAvatarDiv.dataset.profileImageUrl = result.imageUrl; 
                    console.log('DEBUG: 프로필 사진 최종 업데이트 완료 (서버 URL).');
                }
            }
        } else {
            showMessageModal(result.message || '프로필 사진 업로드에 실패했습니다.');
            console.error('ERROR: 프로필 사진 업로드 실패 응답:', result);
        }
    } catch (error) {
        console.error('ERROR: 프로필 사진 업로드 중 클라이언트 측 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 프로필 사진 업로드 이벤트 리스너
    const avatarInput = document.getElementById('avatarInput');
    if (avatarInput) {
        avatarInput.addEventListener('change', handleProfileImageUpload);
    } else {
        console.error("Error: 'avatarInput' input element not found. Profile image upload functionality might not work.");
    }

    // 초기 로드 시 프로필 이미지 설정 (서버에서 받은 URL이 있다면 적용)
    const avatarIconElement = document.getElementById('avatarIcon');
    const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');

    if (profileAvatarDiv && profileAvatarDiv.dataset.profileImageUrl) {
        const initialImageUrl = profileAvatarDiv.dataset.profileImageUrl;
        if (initialImageUrl && (initialImageUrl.startsWith('/uploads/empl_profile/') || initialImageUrl.startsWith('/images/'))) { 
            profileAvatarDiv.style.backgroundImage = `url(${initialImageUrl})`;
            profileAvatarDiv.style.backgroundSize = 'cover';
            profileAvatarDiv.style.backgroundPosition = 'center';
            if (avatarIconElement) {
                avatarIconElement.style.display = 'none';
            }
        } else if (initialImageUrl === '/images/default_profile.png') {
            profileAvatarDiv.style.backgroundImage = ''; 
            if (avatarIconElement) {
                avatarIconElement.style.display = ''; 
            }
        }
    }

    // 비밀번호 관련 필드에 이벤트 리스너 연결 및 초기 유효성 검사 호출
    document.getElementById('newPassword')?.addEventListener('input', checkPasswordStrength);
    document.getElementById('confirmPassword')?.addEventListener('input', checkPasswordMatch);
    validatePasswordForm(); 
});

// ⭐여기에 DEPT_LIST와 departmentMap을 추가해주세요.⭐
// 부서 목록 데이터 (고객님께서 제공해주신 버전)
const DEPT_LIST = [
    { code: "101", name: "교무처" }, { code: "102", name: "학생처" },
    { code: "103", name: "입학처" }, { code: "104", name: "총무처" },
    { code: "105", name: "기획처" }, { code: "106", name: "산학협력단" },
    { code: "107", name: "도서관" }, { code: "108", name: "전산정보원" },
    { code: "109", name: "국제교류처" }, { code: "110", name: "연구처" },
    { code: "111", name: "진로취창업팀" }
];

// 매핑 맵 생성 (코드를 이름으로 빠르게 찾기 위함)
const departmentMap = new Map(DEPT_LIST.map(item => [item.code, item.name]));


// 개인정보 저장 (AJAX 호출)
document.getElementById('personalInfoForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    console.log('DEBUG: 개인정보 폼 제출 이벤트 감지됨.');

    const employeeId = document.getElementById('employeeId')?.value;
    if (!employeeId) {
        showMessageModal('교직원 ID를 찾을 수 없습니다.');
        console.error('ERROR: employeeId input element not found or empty.');
        return;
    }
    console.log(`DEBUG: 교직원 ID: ${employeeId}`);

    const getValue = (id) => document.getElementById(id)?.value || "";

    // EmplInfoDto의 @JsonProperty 이름과 일치하도록 키명 변경
	const formData = {
	    STAFF_NO: employeeId, 
	    STAFF_NM: getValue('name'),
	    DEPT_CD: getValue('department'),
	    POSITION_CD: getValue('position'), 
	    STAFF_EML_ADDR: getValue('email'),
	    STAFF_TELNO: getValue('phone'),
	    HIRE_DT: getValue('hireDate'), 
	    ZIP_CD: getValue('zipCode'),
	    ADDR: getValue('address'),
	    DADDR: getValue('detailAddress'),
	};

    console.log('DEBUG: 서버로 전송할 개인정보 업데이트 데이터:', formData);

    try {
        console.log('DEBUG: /api/mypage/employee/updatePersonalInfo API 호출 시작...');
        const response = await fetch('/api/mypage/employee/updatePersonalInfo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        console.log('DEBUG: API 응답 수신됨. 상태 코드:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('ERROR: 개인정보 업데이트 API 응답 오류. 상태:', response.status, '텍스트:', errorText);
            try {
                const errorResult = JSON.parse(errorText);
                showMessageModal(errorResult.message || `개인정보 업데이트에 실패했습니다. 오류: ${response.status} ${response.statusText}`);
            } catch (jsonError) {
                showMessageModal(`개인정보 업데이트에 실패했습니다. 오류: ${response.status} ${response.statusText}. 서버 응답: ${errorText.substring(0, 100)}...`);
            }
            return;
        }

        const result = await response.json();
        console.log('DEBUG: API 응답 JSON 파싱 완료:', result);
        showMessageModal(result.message || '개인정보가 성공적으로 업데이트되었습니다.');

        // 프로필 영역 실시간 업데이트
        document.getElementById('profileName').textContent = formData.STAFF_NM;
        // ⭐ 아래 두 줄의 departmentName을 departmentMap을 사용하여 변경합니다. ⭐
        const departmentName = departmentMap.get(formData.DEPT_CD) || formData.DEPT_CD; // DEPT_CD를 이름으로 변환
        const positionName = document.querySelector('#position option:checked')?.textContent || formData.POSITION_CD; // 이 부분은 그대로 둡니다.

        document.getElementById('profileInfo').textContent = `${departmentName} ${positionName} • ${formData.STAFF_NO}`;
        document.getElementById('headerUserName').textContent = formData.STAFF_NM;
        document.getElementById('headerUserDept').textContent = `${departmentName} ${positionName}`;


    } catch (error) {
        console.error('ERROR: 개인정보 업데이트 중 클라이언트 측 오류:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다.');
    }
});

// 비밀번호 강도 체크 함수
function checkPasswordStrength() {
    const password = document.getElementById('newPassword').value;
    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');

    strengthBar.classList.remove('strength-weak', 'strength-medium', 'strength-strong');

    let strength = 0;
    let feedback = [];

    if (password.length >= 8) {
        strength++;
    } else {
        feedback.push('길이 8자 이상');       
    }
    if (/[a-z]/.test(password)) { 
        strength++;
    } else {
        feedback.push('영문 소문자 포함');
    }
    if (/[A-Z]/.test(password)) { 
        strength++;
    } else {
        feedback.push('영문 대문자 포함');
    }
    if (/[0-9]/.test(password)) { 
        strength++;
    } else {
        feedback.push('숫자 포함');
    }
    if (/[^A-Za-z0-9\s]/.test(password)) { 
        strength++;
    } else {
        feedback.push('특수문자 포함');
    }

    if (password.length === 0) {
        strengthBar.className = 'password-strength';
        helpText.textContent = '8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.';
        helpText.className = 'text-muted';
    } else if (strength <= 2) {
        strengthBar.classList.add('strength-weak');
        helpText.textContent = `약함: (${feedback.join(', ')}) 조건을 더 추가해야 합니다.`;
        helpText.className = 'text-danger';
    } else if (strength >= 3 && strength <= 4) {
        strengthBar.classList.add('strength-medium');
        helpText.textContent = `보통 강도입니다. (${feedback.join(', ')}) 조건을 만족하면 더 안전합니다.`;
        helpText.className = 'text-warning';
    } else if (strength === 5) {
        strengthBar.classList.add('strength-strong');
        helpText.textContent = '강함: 안전한 비밀번호입니다. 이제 변경 버튼을 클릭하세요!';
        helpText.className = 'text-success';
    }

    validatePasswordForm();
}

// 비밀번호 일치 확인 함수
function checkPasswordMatch() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const matchText = document.getElementById('passwordMatch');
    
    if (confirmPassword.length === 0) {
        matchText.textContent = '';
    } else if (newPassword === confirmPassword) {
        matchText.textContent = '비밀번호가 일치합니다. 잘하셨습니다!';
        matchText.className = 'text-success';
    } else {
        matchText.textContent = '비밀번호가 일치하지 않습니다. 다시 확인해주세요.';
        matchText.className = 'text-danger';
    }

    validatePasswordForm();
}

// 비밀번호 변경 버튼 활성화 제어 함수
function validatePasswordForm() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const submitBtn = document.getElementById('passwordSubmitBtn');
    const strengthBar = document.getElementById('passwordStrength');

    const isStrongEnough = strengthBar.classList.contains('strength-strong');
    const isMatchAndNotEmpty = newPassword === confirmPassword && newPassword.length > 0;

    const shouldBeDisabled = !(isStrongEnough && isMatchAndNotEmpty);
    
    console.group('DEBUG (validatePasswordForm): Current State');
    console.log(`  - New Password Length: ${newPassword.length}`);
    console.log(`  - Confirm Password Length: ${confirmPassword.length}`);
    console.log(`  - Password Strength Bar Classes: ${Array.from(strengthBar.classList).join(', ')}`);
    console.log(`  - Is Strong Enough (needs 'strength-strong'): ${isStrongEnough}`);
    console.log(`  - Passwords Match & Not Empty: ${isMatchAndNotEmpty}`);
    console.log(`  - Final Button 'disabled' set to: ${shouldBeDisabled}`);
    console.groupEnd();

    submitBtn.disabled = shouldBeDisabled;
}

// 비밀번호 변경 폼 초기화 함수 (취소 버튼용)
function clearPasswordForm() {
    document.getElementById('passwordForm').reset();
    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');
    const matchText = document.getElementById('passwordMatch');
    const submitBtn = document.getElementById('passwordSubmitBtn');

    strengthBar.className = 'password-strength';
    strengthBar.textContent = '';
    helpText.textContent = '8자 이상, 영문 대소문자, 숫자, 특수문자 포함';
    helpText.className = 'text-muted';
    matchText.textContent = '';
    submitBtn.disabled = true;
}

// 비밀번호 변경 폼 제출 처리 (AJAX 호출)
document.getElementById('passwordForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    console.log('DEBUG: 비밀번호 변경 폼 제출 이벤트 감지됨.');

    const employeeId = document.getElementById('employeeId')?.value; 
    if (!employeeId) {
        console.error('ERROR: 교직원 ID를 찾을 수 없습니다.');
        showMessageModal('교직원 ID를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        return;
    }
    console.log(`DEBUG: 교직원 ID: ${employeeId}`);

    const currentPassword = document.getElementById('currentPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;

    console.log(`DEBUG: 현재 비밀번호: ${currentPassword ? '입력됨' : '비어있음'}`);
    console.log(`DEBUG: 새 비밀번호: ${newPassword ? '입력됨' : '비어있음'}`);
    console.log(`DEBUG: 새 비밀번호 확인: ${confirmPassword ? '입력됨' : '비어있음'}`);

    if (newPassword !== confirmPassword) {
        console.warn('WARN: 새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        showMessageModal('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        return;
    }
    if (newPassword === currentPassword) {
        console.warn('WARN: 새 비밀번호가 현재 비밀번호와 동일합니다.');
        showMessageModal('새 비밀번호는 현재 비밀번호와 달라야 합니다.');
        return;
    }

    const submitBtn = document.getElementById('passwordSubmitBtn');
    if (submitBtn && submitBtn.disabled) {
        console.warn('WARN: 비밀번호 변경 버튼이 비활성화 상태입니다. 제출 방지.');
        showMessageModal('비밀번호 변경 조건을 충족하지 못했습니다. 입력값을 확인해주세요.', '알림');
        return;
    }

    const data = {
        userId: employeeId, 
        currentPassword: currentPassword,
        newPassword: newPassword
    };

    console.log('DEBUG: 서버로 전송할 비밀번호 변경 데이터:', data);

    try {
        console.log('DEBUG: /api/mypage/employee/updatePassword API 호출 시작...');
        const response = await fetch('/api/mypage/employee/updatePassword', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });
        console.log('DEBUG: API 응답 수신됨. 상태 코드:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('ERROR: 비밀번호 변경 API 응답 오류. 상태:', response.status, '텍스트:', errorText);
            try {
                const errorResult = JSON.parse(errorText);
                showMessageModal(errorResult.message || `비밀번호 변경에 실패했습니다. 오류: ${response.status} ${response.statusText}`);
            } catch (jsonError) {
                showMessageModal(`비밀번호 변경에 실패했습니다. 오류: ${response.status} ${response.statusText}. 서버 응답: ${errorText.substring(0, 100)}...`);
            }
            return;
        }

        const result = await response.json();
        console.log('DEBUG: API 응답 JSON 파싱 완료:', result);

        if (result.success) { 
            showMessageModal(result.message || '비밀번호가 성공적으로 변경되었습니다.');
            clearPasswordForm(); 
        } else {
            showMessageModal(result.message || '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인하거나 새 비밀번호가 기존 비밀번호와 다른지 확인해주세요.');
        }
    } catch (error) {
        console.error('ERROR: 비밀번호 변경 중 클라이언트 측 네트워크 또는 처리 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다. 개발자 도구 콘솔을 확인해주세요.');
    }
});


// 개인정보 초기화 (실제 서비스에서는 DB 데이터를 불러와서 폼을 채워야 합니다.)
function resetPersonalInfo() {
    if (confirm('개인정보를 초기값으로 되돌리시겠습니까? 저장되지 않은 변경사항은 손실됩니다.')) {
        showMessageModal('개인정보 초기화 요청이 전달되었습니다. 페이지를 새로고침하면 최신 정보가 반영됩니다.');
        window.location.reload(); 
    }
}

// 프로필 수정 (탭 이동)
function editProfile() {
    document.querySelector('button[data-bs-target="#personalInfoTab"]').click();
}

// 데이터 다운로드 (데모)
function downloadData() {
    console.log('개인 데이터를 JSON 형태로 다운로드합니다.');
    showMessageModal('개인 데이터를 JSON 형태로 다운로드합니다 (데모).');
}

// 캐시 초기화 (데모)
function clearCache() {
    if (confirm('캐시를 초기화하시겠습니까? 웹사이트 성능에 일시적인 영향을 줄 수 있습니다.')) {
        console.log('캐시가 초기화되었습니다.');
        showMessageModal('캐시가 초기화되었습니다.');
    }
}

// 필터링 함수들 (데모)
document.getElementById('studentFilter')?.addEventListener('change', function() {
    const filter = this.value;
    showMessageModal(`${filter}학년 학생으로 필터링합니다 (데모).`);
});

document.getElementById('activityFilter')?.addEventListener('change', function() {
    const filter = this.value;
    showMessageModal(`${filter} 활동으로 필터링합니다 (데모).`);
});

document.getElementById('scheduleType')?.addEventListener('change', function() {
    const filter = this.value;
    showMessageModal(`${filter} 일정으로 필터링합니다 (데모).`);
});

document.getElementById('scheduleDate')?.addEventListener('change', function() {
    const date = this.value;
    showMessageModal(`${date} 날짜의 일정을 조회합니다 (데모).`);
});

// 학생 관련 함수들 (데모)
function viewStudentDetail(studentId) {
    showMessageModal(`학생 ${studentId}의 상세 정보를 조회합니다 (데모).`);
}

function scheduleCounseling(studentId) {
    showMessageModal(`학생 ${studentId}와의 상담을 예약합니다 (데모).`);
}

function sendMessage(studentId) {
    showMessageModal(`학생 ${studentId}에게 메시지를 보냅니다 (데모).`);
}

function exportStudents() {
    showMessageModal('담당 학생 목록을 Excel 파일로 다운로드합니다 (데모).');
}

function loadMoreStudents() {
    showMessageModal('더 많은 학생 정보를 불러옵니다 (데모).');
}

// 일정 관련 함수들 (데모)
function addSchedule() {
    showMessageModal('새 일정을 추가합니다 (데모).');
}

function editSchedule(scheduleId) {
    showMessageModal(`일정 ${scheduleId}를 수정합니다 (데모).`);
}

function deleteSchedule(scheduleId) {
    if (confirm('이 일정을 삭제하시겠습니까? (데모)')) {
        showMessageModal(`일정 ${scheduleId}가 삭제되었습니다 (데모).`);
    }
}

// 활동 이력 관련 함수들 (데모)
function loadMoreActivity() {
    showMessageModal('더 많은 활동 이력을 불러옵니다 (데모).');
}

function exportActivity() {
    showMessageModal('활동 이력을 Excel 파일로 다운로드합니다 (데모).');
}

// 설정 저장 (데모)
function saveSettings() {
    const settings = {
        emailNotification: document.getElementById('emailNotification').checked,
        smsNotification: document.getElementById('smsNotification').checked,
        scheduleNotification: document.getElementById('scheduleNotification').checked,
        counselingNotification: document.getElementById('counselingNotification').checked,
        profilePublic: document.getElementById('profilePublic').checked,
        contactAllow: document.getElementById('contactAllow').checked,
        schedulePublic: document.getElementById('schedulePublic').checked,
        counselingDuration: document.getElementById('counselingDuration').value,
        counselingInterval: document.getElementById('counselingInterval').value
    };

    console.log('DEBUG: 설정 저장:', settings);
    showMessageModal('설정이 저장되었습니다 (데모).');
}

// 계정 비활성화 (데모)
function deactivateAccount() {
    if (confirm('계정을 비활성화하시겠습니까? 언제든지 다시 활성화할 수 있습니다 (데모).')) {
        console.log('DEBUG: 계정이 비활성화되었습니다 (데모).');
        showMessageModal('계정이 비활성화되었습니다 (데모).');
    }
}

// 계정 삭제 (데모)
function deleteAccount() {
    const confirmation = prompt('계정을 완전히 삭제하려면 "DELETE"를 대문자로 입력하세요:');
    if (confirmation === 'DELETE') {
        console.log('DEBUG: 계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다 (데모).');
        showMessageModal('계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다 (데모).');
    } else {
        showMessageModal('계정 삭제가 취소되었습니다 (데모).');
    }
}

// 로그아웃
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        window.location.href = '/logout'; // Spring Security 기본 로그아웃 URL
    }
}