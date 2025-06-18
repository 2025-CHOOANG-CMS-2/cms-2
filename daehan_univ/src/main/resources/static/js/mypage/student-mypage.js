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
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('show');
}

// ⭐ Custom Message Modal 함수 (ID 확인) ⭐
function showMessageModal(message, title = '알림') {
    // messageModalLabel과 messageModalBody ID가 HTML에 정확히 존재하는지 확인
    const modalTitle = document.getElementById('messageModalLabel');
    const modalBody = document.getElementById('messageModalBody');

    if (modalTitle && modalBody) {
        modalTitle.textContent = title;
        modalBody.textContent = message;
        const messageModal = new bootstrap.Modal(document.getElementById('messageModal'));
        messageModal.show();
    } else {
        // Fallback for debugging if modal elements are not found
        console.error("Error: Message modal elements not found. Cannot display modal.");
        alert(`${title}: ${message}`); // Fallback to alert if modal elements are missing
    }
}

// 프로필 사진 업로드
function handleAvatarUpload(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const avatarIcon = document.getElementById('avatarIcon');
            avatarIcon.style.backgroundImage = `url(${e.target.result})`;
            avatarIcon.style.backgroundSize = 'cover';
            avatarIcon.style.backgroundPosition = 'center';
            avatarIcon.innerHTML = '';
        };
        reader.readAsDataURL(file);
        console.log('프로필 사진이 업로드되었습니다.');
    }
}

// 개인정보 폼 처리 (AJAX 호출)
document.getElementById('personalInfoForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const studentId = document.getElementById('studentId').value;
    if (!studentId) {
        showMessageModal('학생 ID를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        return;
    }

    const formData = {
        stdNo: studentId,
        stdNm: document.getElementById('name').value,
        scsbjtCd: document.getElementById('department').value,
        schoolYear: parseInt(document.getElementById('grade').value),
        stdEmlAddr: document.getElementById('email').value,
        stdTelno: document.getElementById('phone').value,
        stdAddr: document.getElementById('address').value,
        stdDaddr: document.getElementById('detailAddress').value,
        stdZip: document.getElementById('zipCode').value,
    };

    console.log('개인정보 업데이트 시도:', formData);

    try {
        const response = await fetch('/api/mypage/updatePersonalInfo', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData),
        });

        const result = await response.json();

        if (response.ok) {
            showMessageModal(result.message || '개인정보가 성공적으로 업데이트되었습니다.');
            // 성공 시 페이지 새로고침 또는 UI 업데이트
            // setTimeout(() => window.location.reload(), 1500); 
        } else {
            showMessageModal(result.message || '개인정보 업데이트에 실패했습니다.');
        }
    } catch (error) {
        console.error('개인정보 업데이트 중 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 오류가 발생했습니다.');
    }
});

// 비밀번호 강도 체크 (기존 코드 유지)
function checkPasswordStrength() {
    const password = document.getElementById('newPassword').value;
    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');

    let strength = 0;
    let feedback = [];

    if (password.length >= 8) strength++;
    else feedback.push('8자 이상');

    if (/[a-z]/.test(password)) strength++;
    else feedback.push('소문자');

    if (/[A-Z]/.test(password)) strength++;
    else feedback.push('대문자');

    if (/[0-9]/.test(password)) strength++;
    else feedback.push('숫자');

    if (/[^A-Za-z0-9]/.test(password)) strength++;
    else feedback.push('특수문자');

    strengthBar.className = 'password-strength';
    if (strength < 3) {
        strengthBar.classList.add('strength-weak');
        helpText.textContent = '약함: ' + feedback.join(', ') + ' 필요';
        helpText.className = 'text-danger';
    } else if (strength < 5) {
        strengthBar.classList.add('strength-medium');
        helpText.textContent = '보통: ' + feedback.join(', ') + ' 권장';
        helpText.className = 'text-warning';
    } else {
        strengthBar.classList.add('strength-strong');
        helpText.textContent = '강함: 안전한 비밀번호입니다';
        helpText.className = 'text-success';
    }

    checkPasswordMatch();
}

// 비밀번호 일치 확인 (기존 코드 유지)
function checkPasswordMatch() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const matchText = document.getElementById('passwordMatch');
    const submitBtn = document.getElementById('passwordSubmitBtn');

    if (confirmPassword === '') {
        matchText.textContent = '';
        submitBtn.disabled = true;
        return;
    }

    if (newPassword === confirmPassword) {
        matchText.textContent = '비밀번호가 일치합니다';
        matchText.className = 'text-success';
        submitBtn.disabled = false;
    } else {
        matchText.textContent = '비밀번호가 일치하지 않습니다';
        matchText.className = 'text-danger';
        submitBtn.disabled = true;
    }
}

// ⭐ 비밀번호 변경 폼 처리 (AJAX 호출로 변경) ⭐
document.getElementById('passwordForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const studentId = document.getElementById('studentId').value;
    if (!studentId) {
        showMessageModal('학생 ID를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        return;
    }

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showMessageModal('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
        return;
    }
    if (newPassword === currentPassword) {
        showMessageModal('새 비밀번호는 현재 비밀번호와 달라야 합니다.');
        return;
    }

    const data = {
        studentId: studentId,
        currentPassword: currentPassword,
        newPassword: newPassword
    };

    console.log('비밀번호 변경 시도:', data);

    try {
        const response = await fetch('/api/mypage/updatePassword', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        const result = await response.json();

        if (response.ok) {
            showMessageModal(result.message || '비밀번호가 성공적으로 변경되었습니다.');
            clearPasswordForm();
        } else {
            showMessageModal(result.message || '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인하거나 새 비밀번호가 기존 비밀번호와 다른지 확인해주세요.');
        }
    } catch (error) {
        console.error('비밀번호 변경 중 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 오류가 발생했습니다.');
    }
});

// 비밀번호 폼 초기화 (기존 코드 유지)
function clearPasswordForm() {
    document.getElementById('passwordForm').reset();
    document.getElementById('passwordStrength').className = 'password-strength';
    document.getElementById('passwordHelp').textContent = '8자 이상, 영문/숫자/특수문자 포함';
    document.getElementById('passwordHelp').className = 'text-muted';
    document.getElementById('passwordMatch').textContent = '';
    document.getElementById('passwordSubmitBtn').disabled = true;
}

// 개인정보 초기화 (기존 코드 유지, mock data 그대로)
function resetPersonalInfo() {
    const confirmReset = confirm('개인정보를 초기값으로 되돌리시겠습니까?');
    if (confirmReset) {
        document.getElementById('personalInfoForm').reset();
        document.getElementById('name').value = '김학생'; 
        document.getElementById('department').value = '011';
        document.getElementById('grade').value = '3';
        document.getElementById('email').value = 'student@university.ac.kr';
        document.getElementById('phone').value = '010-1234-5678';
        document.getElementById('address').value = '서울시 강남구 테헤란로 123';
        document.getElementById('detailAddress').value = '상세 주소 예시';
        document.getElementById('zipCode').value = '12345';
        showMessageModal('개인정보가 초기값으로 되돌려졌습니다 (임시).');
    }
}

// 활동 이력 필터링 (기존 코드 유지)
document.getElementById('activityFilter').addEventListener('change', function() {
    const filter = this.value;
    const activities = document.querySelectorAll('.activity-item');
    
    activities.forEach(activity => {
        if (filter === 'all') {
            activity.style.display = 'block';
        } else {
            activity.style.display = 'block';
        }
    });
});

// 활동 이력 더 보기 (기존 코드 유지)
function loadMoreActivity() {
    console.log('더 많은 활동 이력을 불러옵니다.');
    showMessageModal('더 많은 활동 이력을 불러옵니다.', '알림');
}

// 활동 이력 내보내기 (기존 코드 유지)
function exportActivity() {
    console.log('활동 이력을 Excel 파일로 다운로드합니다.');
    showMessageModal('활동 이력을 Excel 파일로 다운로드합니다.', '알림');
}

// 설정 저장 (기존 코드 유지, 실제 저장 로직 없음)
function saveSettings() {
    const settings = {
        emailNotification: document.getElementById('emailNotification').checked,
        smsNotification: document.getElementById('smsNotification').checked,
        programNotification: document.getElementById('programNotification').checked,
        counselingNotification: document.getElementById('counselingNotification').checked,
        profilePublic: document.getElementById('profilePublic').checked,
        activityPublic: document.getElementById('activityPublic').checked,
        contactAllow: document.getElementById('contactAllow').checked
    };

    console.log('설정 저장:', settings);
    showMessageModal('설정이 저장되었습니다 (데모).');
}

// 프로필 수정 (탭 이동) (기존 코드 유지)
function editProfile() {
    document.querySelector('a[href="#personalInfoTab"]').click();
}

// 데이터 다운로드 (기존 코드 유지)
function downloadData() {
    console.log('개인 데이터를 JSON 형태로 다운로드합니다.');
    showMessageModal('개인 데이터를 JSON 형태로 다운로드합니다 (데모).');
}

// 캐시 초기화 (confirm 대신 모달 사용)
function clearCache() {
    const confirmClear = confirm('캐시를 초기화하시겠습니까?');
    if (confirmClear) {
        console.log('캐시가 초기화되었습니다.');
        showMessageModal('캐시가 초기화되었습니다.');
    }
}

// 계정 비활성화 (confirm 대신 모달 사용)
function deactivateAccount() {
    const confirmDeactivate = confirm('계정을 비활성화하시겠습니까? 언제든지 다시 활성화할 수 있습니다.');
    if (confirmDeactivate) {
        console.log('계정이 비활성화되었습니다.');
        showMessageModal('계정이 비활성화되었습니다.');
    }
}

// 계정 삭제 (prompt 대신 모달 사용, 더 복잡한 모달 필요)
function deleteAccount() {
    const confirmation = prompt('계정을 삭제하려면 "DELETE"를 입력하세요:');
    if (confirmation === 'DELETE') {
        console.log('계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다.');
        showMessageModal('계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다.');
    } else {
        showMessageModal('계정 삭제가 취소되었습니다.');
    }
}

// 로그아웃 (confirm 대신 모달 사용)
function logout() {
    const confirmLogout = confirm('로그아웃 하시겠습니까?');
    if (confirmLogout) {
        window.location.href = 'login.html';
    }
}

// 학번 파라미터가 없으면 기본값으로 리다이렉트하는 로직 (기존 코드 유지)
window.addEventListener('load', function() {
    const urlParams = new URLSearchParams(window.location.search);
    let studentId = urlParams.get('studentId');

    if (!studentId) {
        console.warn("studentId 파라미터가 URL에 없습니다. 임시 학번을 사용합니다.");
        studentId = "2025004001";
    }

    if (!window.location.pathname.includes('/student_mypage.do') || !urlParams.has('studentId')) {
        window.location.href = '/student_mypage.do?studentId=' + studentId;
    }
});
