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

// 메시지 모달 함수
function showMessageModal(message, title = '알림') {
    const modalTitle = document.getElementById('messageModalLabel');
    const modalBody = document.getElementById('messageModalBody');

    if (modalTitle && modalBody) {
        modalTitle.textContent = title;
        modalBody.textContent = message;
        new bootstrap.Modal(document.getElementById('messageModal')).show();
    } else {
        alert(`${title}: ${message}`);
    }
}

// 프로필 사진 업로드
async function handleProfileImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const studentIdEl = document.getElementById('studentId');
    if (!studentIdEl) {
        showMessageModal('학생 ID를 찾을 수 없습니다.');
        return;
    }

    const studentId = studentIdEl.value;
    const reader = new FileReader();
    reader.onload = function (e) {
        const avatarIcon = document.getElementById('avatarIcon');
        const profileAvatar = avatarIcon?.closest('.profile-avatar') || document.querySelector('.profile-avatar');
        if (profileAvatar) {
            profileAvatar.style.backgroundImage = `url(${e.target.result})`;
            profileAvatar.style.backgroundSize = 'cover';
            profileAvatar.style.backgroundPosition = 'center';
            profileAvatar.querySelector('i.bi-person')?.remove();
        }
    };
    reader.readAsDataURL(file);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('studentId', studentId);

    try {
        const response = await fetch('/api/mypage/uploadProfileImage', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        if (response.ok) {
            showMessageModal(result.message || '업로드 성공');
        } else {
            showMessageModal(result.message || '업로드 실패');
        }
    } catch (error) {
        console.error(error);
        showMessageModal('서버 오류');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('avatarInput')?.addEventListener('change', handleProfileImageUpload);

    const avatarIcon = document.getElementById('avatarIcon');
    const profileAvatar = avatarIcon?.closest('.profile-avatar') || document.querySelector('.profile-avatar');
    if (profileAvatar?.dataset.profileImageUrl) {
        const url = profileAvatar.dataset.profileImageUrl;
        if (url.startsWith('/uploads/std_profile/')) {
            profileAvatar.style.backgroundImage = `url(${url})`;
            profileAvatar.style.backgroundSize = 'cover';
            profileAvatar.style.backgroundPosition = 'center';
            if (avatarIcon) avatarIcon.style.display = 'none';
        }
    }

    // 비밀번호 관련 필드에 이벤트 리스너 연결 및 초기 유효성 검사 호출 제거
    // 항상 버튼 활성화 상태 유지하므로 아래 코드는 사용 안 함
    // document.getElementById('newPassword')?.addEventListener('input', checkPasswordStrength);
    // document.getElementById('confirmPassword')?.addEventListener('input', checkPasswordMatch);
    // validatePasswordForm(); 
});

// 개인정보 저장 (기존 유지)
document.getElementById('personalInfoForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const studentId = document.getElementById('studentId')?.value;
    if (!studentId) {
        showMessageModal('학생 ID가 없습니다.');
        return;
    }

    const getValue = (id) => document.getElementById(id)?.value || "";

	const formData = {
	    STD_NO: studentId,
	    STD_NM: getValue('name'),
	    SCSBJT_CD: getValue('department'),
	    SCH_YR: parseInt(getValue('grade')),
	    STD_EML_ADDR: getValue('email'),
	    STD_TELNO: getValue('phone'),
	    STD_ADDR: getValue('address'),
	    STD_DADDR: getValue('detailAddress'),
	    STD_ZIP: getValue('zipCode'),
	    STD_ENTR_DT: document.getElementById('entranceDate')?.value,
	    STD_STAT_CD: document.getElementById('studentStatusCode')?.value,
	    USE_YN: 'Y'
	};

    try {
        const response = await fetch('/api/mypage/updatePersonalInfo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        const result = await response.json();
        showMessageModal(result.message || '개인정보가 저장되었습니다.');
    } catch (error) {
        console.error('오류:', error);
        showMessageModal('서버 오류가 발생했습니다.');
    }
});

// 비밀번호 강도 체크 함수 (UI 피드백 용도 유지)
function checkPasswordStrength() {
    const password = document.getElementById('newPassword').value;
    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');

    strengthBar.classList.remove('strength-weak', 'strength-medium', 'strength-strong');

    let strength = 0;
    let feedback = [];

    if (password.length >= 8) strength++; else feedback.push('8자 이상');
    if (/[a-z]/.test(password)) strength++; else feedback.push('영문 소문자');
    if (/[A-Z]/.test(password)) strength++; else feedback.push('영문 대문자');
    if (/[0-9]/.test(password)) strength++; else feedback.push('숫자');
    if (/[^A-Za-z0-9\s]/.test(password)) strength++; else feedback.push('특수문자');

    if (password.length === 0) {
        strengthBar.className = 'password-strength';
        helpText.textContent = '8자 이상, 영문 대소문자, 숫자, 특수문자 포함';
        helpText.className = 'text-muted';
    } else if (strength <= 2) {
        strengthBar.classList.add('strength-weak');
        helpText.textContent = `약함: (${feedback.join(', ')}) 조건을 추가하세요.`;
        helpText.className = 'text-danger';
    } else if (strength <= 4) {
        strengthBar.classList.add('strength-medium');
        helpText.textContent = `보통: (${feedback.join(', ')}) 조건을 추가하면 더 안전합니다.`;
        helpText.className = 'text-warning';
    } else {
        strengthBar.classList.add('strength-strong');
        helpText.textContent = '강함: 안전한 비밀번호입니다.';
        helpText.className = 'text-success';
    }
}

// 비밀번호 일치 확인 함수 (UI 피드백 용도 유지)
function checkPasswordMatch() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const matchText = document.getElementById('passwordMatch');

    if (confirmPassword === '') {
        matchText.textContent = '';
    } else if (newPassword === confirmPassword) {
        matchText.textContent = '비밀번호가 일치합니다.';
        matchText.className = 'text-success';
    } else {
        matchText.textContent = '비밀번호가 일치하지 않습니다.';
        matchText.className = 'text-danger';
    }
}

// 비밀번호 변경 폼 초기화 함수 (버튼 항상 활성화 상태 유지)
function clearPasswordForm() {
    document.getElementById('passwordForm').reset();

    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');
    const matchText = document.getElementById('passwordMatch');

    strengthBar.className = 'password-strength';
    strengthBar.textContent = '';
    helpText.textContent = '8자 이상, 영문 대소문자, 숫자, 특수문자 포함';
    helpText.className = 'text-muted';
    matchText.textContent = '';

    // 버튼 비활성화 관련 제거 (항상 활성)
    // document.getElementById('passwordSubmitBtn').disabled = true; ← 삭제
}

// 개인정보 초기화
function resetPersonalInfo() {
    if (confirm('개인정보를 초기값으로 되돌리시겠습니까? 저장되지 않은 변경사항은 손실됩니다.')) {
        showMessageModal('개인정보 초기화 요청이 전달되었습니다. 페이지를 새로고침하면 최신 정보가 반영됩니다.');
        window.location.reload(); 
    }
}

// 로그아웃
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        window.location.href = '/logout';
    }
}

// 비밀번호 변경 폼 제출 이벤트 리스너 (버튼 항상 활성화 상태 유지)
document.getElementById('passwordForm')?.addEventListener('submit', async function (e) {
    e.preventDefault();

    const studentId = document.getElementById('studentId')?.value?.trim();
    const currentPassword = document.getElementById('currentPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;

    if (!studentId) {
        showMessageModal('학생 ID가 없습니다.');
        return;
    }
    if (!currentPassword || !newPassword || !confirmPassword) {
        showMessageModal('모든 비밀번호 항목을 입력해주세요.');
        return;
    }
    if (newPassword !== confirmPassword) {
        showMessageModal('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
        return;
    }

    try {
		const response = await fetch('/api/mypage/updatePassword', {
		    method: 'POST',
		    headers: { 'Content-Type': 'application/json' },
		    body: JSON.stringify({
		        studentId,
		        currentPassword,
		        newPassword
		    })
		});

        const result = await response.json();

        if (response.ok) {
            showMessageModal(result.message || '비밀번호가 변경되었습니다.');
            clearPasswordForm();
        } else {
            showMessageModal(result.message || '비밀번호 변경에 실패했습니다.');
        }
    } catch (error) {
        console.error(error);
        showMessageModal('서버 오류가 발생했습니다.');
    }
});
