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

// Custom Message Modal 함수 (alert 대신 사용)
function showMessageModal(message, title = '알림') {
    const modalTitle = document.getElementById('messageModalLabel');
    const modalBody = document.getElementById('messageModalBody');

    if (modalTitle && modalBody) {
        modalTitle.textContent = title;
        modalBody.textContent = message;
        const messageModal = new bootstrap.Modal(document.getElementById('messageModal'));
        messageModal.show();
    } else {
        console.error("Error: Message modal elements not found. Cannot display modal.");
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

    const studentId = document.getElementById('studentId').value;
    if (!studentId) {
        showMessageModal('학생 ID를 찾을 수 없습니다. 페이지를 새로고침 해주세요.');
        console.error("Error: studentId input element not found or empty.");
        return;
    }

    // ⭐ 1. 파일 미리보기 즉시 적용 (UX 개선) ⭐
    // FileReader를 사용하여 선택된 파일을 Data URL로 읽어와 즉시 UI에 적용합니다.
    const reader = new FileReader();
    reader.onload = function(e) {
        const avatarIconElement = document.getElementById('avatarIcon');
        // profile-avatar div를 정확히 찾습니다. (클릭 가능한 영역)
        const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');

        if (profileAvatarDiv) {
            profileAvatarDiv.style.backgroundImage = `url(${e.target.result})`; // Data URL로 미리보기 설정
            profileAvatarDiv.style.backgroundSize = 'cover';
            profileAvatarDiv.style.backgroundPosition = 'center';
            // 기존의 기본 아이콘(bi-person)이 있다면 제거하여 이미지와 겹치지 않도록 합니다.
            profileAvatarDiv.querySelector('i.bi-person')?.remove(); 
            console.log('선택된 프로필 사진 미리보기 완료 (로컬 이미지).');
        } else {
            console.error("Error: .profile-avatar element not found for preview.");
        }
    };
    reader.readAsDataURL(file); // 파일을 Data URL로 읽기 시작

    // ⭐ 2. 백엔드로 파일 전송 ⭐
    const formData = new FormData();
    formData.append('file', file);
    formData.append('studentId', studentId); // 학생 ID도 함께 전송

    try {
        console.log('프로필 사진 업로드 요청 중...');
        const response = await fetch('/api/mypage/uploadProfileImage', {
            method: 'POST',
            body: formData, // FormData를 사용하면 'Content-Type' 헤더를 수동으로 설정할 필요가 없습니다.
        });

        const result = await response.json(); // 서버 응답을 JSON으로 파싱

        if (response.ok) { // HTTP 상태 코드가 200번대인 경우
            showMessageModal(result.message || '프로필 사진이 성공적으로 업데이트되었습니다.');
            // ⭐ 서버에서 반환된 실제 이미지 URL로 UI 업데이트 ⭐
            if (result.imageUrl) {
                const avatarIconElement = document.getElementById('avatarIcon');
                const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');
                if (profileAvatarDiv) {
                    profileAvatarDiv.style.backgroundImage = `url(${result.imageUrl})`; // 서버에서 받은 영구 URL로 최종 업데이트
                    profileAvatarDiv.style.backgroundSize = 'cover';
                    profileAvatarDiv.style.backgroundPosition = 'center';
                    // data-profile-image-url 속성도 업데이트하여 페이지 새로고침 시에도 올바른 이미지가 로드되도록 합니다.
                    profileAvatarDiv.dataset.profileImageUrl = result.imageUrl; 
                    console.log('프로필 사진 최종 업데이트 완료 (서버 URL).');
                }
            }
        } else { // HTTP 상태 코드가 200번대가 아닌 경우 (예: 4xx, 5xx)
            showMessageModal(result.message || '프로필 사진 업로드에 실패했습니다.');
            console.error('프로필 사진 업로드 실패 응답:', result);
            // 실패 시 기존 이미지로 롤백하거나 에러 메시지 표시 로직 추가 고려
        }
    } catch (error) {
        // 네트워크 오류, JSON 파싱 오류 등 예외 발생 시
        console.error('프로필 사진 업로드 중 클라이언트 측 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 처리 중 문제가 발생했습니다.');
    }
}

// ⭐ HTML input의 ID에 맞춰 'avatarInput'으로 변경하고 이벤트 리스너 연결 ⭐
document.addEventListener('DOMContentLoaded', () => {
    const avatarInput = document.getElementById('avatarInput'); // ⭐ ID가 'avatarInput'인 요소를 찾습니다. ⭐
    if (avatarInput) {
        avatarInput.addEventListener('change', handleProfileImageUpload); // 파일 선택 시 handleProfileImageUpload 함수 호출
    } else {
        console.error("Error: 'avatarInput' input element not found. Profile image upload functionality might not work."); // 에러 메시지 수정
    }

    // ⭐ 초기 프로필 이미지 로드 로직 ⭐
    const avatarIconElement = document.getElementById('avatarIcon');
    const profileAvatarDiv = avatarIconElement ? avatarIconElement.closest('.profile-avatar') : document.querySelector('.profile-avatar');

    if (profileAvatarDiv && profileAvatarDiv.dataset.profileImageUrl) { // 'data-profile-image-url' 속성이 존재하는지 확인
        const initialImageUrl = profileAvatarDiv.dataset.profileImageUrl;
        // URL이 유효하고 기본 이미지가 아닐 경우에만 배경 이미지로 설정합니다.
        // WebConfig를 통해 '/uploads/std_profile/' 경로가 서빙되므로 해당 경로인지 확인합니다.
        if (initialImageUrl && initialImageUrl.startsWith('/uploads/std_profile/')) {
            profileAvatarDiv.style.backgroundImage = `url(${initialImageUrl})`;
            profileAvatarDiv.style.backgroundSize = 'cover';
            profileAvatarDiv.style.backgroundPosition = 'center';
            if (avatarIconElement) {
                avatarIconElement.style.display = 'none'; // 기본 아이콘 숨기기
            }
        } else if (initialImageUrl === '/images/default_profile.png') {
            // DB에 기본 이미지 경로가 저장되어 있거나 이미지가 없는 경우, 아이콘을 표시합니다.
            profileAvatarDiv.style.backgroundImage = ''; // 배경 이미지 제거
            if (avatarIconElement) {
                avatarIconElement.style.display = ''; // 아이콘 다시 보이기
            }
        }
    }
});


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
            // 필요한 경우 페이지의 다른 UI 요소 업데이트 또는 새로고침
        } else {
            showMessageModal(result.message || '개인정보 업데이트에 실패했습니다.');
        }
    } catch (error) {
        console.error('개인정보 업데이트 중 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 오류가 발생했습니다.');
    }
});

// 비밀번호 강도 체크
document.getElementById('newPassword').addEventListener('input', checkPasswordStrength);
document.getElementById('confirmPassword').addEventListener('input', checkPasswordMatch);

function checkPasswordStrength() {
    const password = document.getElementById('newPassword').value;
    const strengthBar = document.getElementById('passwordStrength');
    const helpText = document.getElementById('passwordHelp');

    // 기존 클래스 제거
    strengthBar.classList.remove('strength-weak', 'strength-medium', 'strength-strong');

    let strength = 0;
    let feedback = []; // 부족한 부분을 사용자에게 안내할 메시지

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

    // 비밀번호 강도에 따른 UI 업데이트
    if (password.length === 0) {
        strengthBar.className = 'password-strength'; // 클래스 초기화
        helpText.textContent = '8자 이상, 영문/숫자/특수문자 포함';
        helpText.className = 'text-muted';
    } else if (strength < 3) {
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

    checkPasswordMatch(); // 비밀번호 강도 변경 시 일치 여부도 다시 확인
}


// 비밀번호 일치 확인
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
        // 비밀번호 강도도 강해야 버튼 활성화 (선택 사항)
        const strengthBar = document.getElementById('passwordStrength');
        const isStrong = strengthBar.classList.contains('strength-strong') || strengthBar.classList.contains('strength-medium'); // 보통 이상도 허용
        submitBtn.disabled = !(isStrong && newPassword.length > 0); // 새 비밀번호 입력이 있고, 강도가 보통 이상일 때만 활성화
    } else {
        matchText.textContent = '비밀번호가 일치하지 않습니다';
        matchText.className = 'text-danger';
        submitBtn.disabled = true;
    }
}

// 비밀번호 변경 폼 처리 (AJAX 호출)
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
            clearPasswordForm(); // 성공 시 폼 초기화
        } else {
            showMessageModal(result.message || '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인하거나 새 비밀번호가 기존 비밀번호와 다른지 확인해주세요.');
        }
    } catch (error) {
        console.error('비밀번호 변경 중 오류 발생:', error);
        showMessageModal('네트워크 오류 또는 서버 응답 오류가 발생했습니다.');
    }
});

// 비밀번호 폼 초기화
function clearPasswordForm() {
    document.getElementById('passwordForm').reset();
    document.getElementById('passwordStrength').className = 'password-strength'; // 초기 클래스로 되돌림
    document.getElementById('passwordHelp').textContent = '8자 이상, 영문/숫자/특수문자 포함';
    document.getElementById('passwordHelp').className = 'text-muted';
    document.getElementById('passwordMatch').textContent = '';
    document.getElementById('passwordSubmitBtn').disabled = true;
}

// 개인정보 초기화 (mock data 그대로) - 실제 서비스에서는 DB 데이터를 불러와야 합니다.
function resetPersonalInfo() {
    const confirmReset = confirm('개인정보를 초기값으로 되돌리시겠습니까?'); // Confirm 창 사용
    if (confirmReset) {
        // 실제로는 DB에서 초기값을 다시 로드하는 로직이 필요합니다.
        // 현재는 하드코딩된 값으로 되돌립니다.
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

// 프로필 수정 (탭 이동) - 현재 사용되지 않는 기능이지만, 남겨둠
function editProfile() {
    document.querySelector('a[href="#personalInfoTab"]').click();
}

// 데이터 다운로드 (데모)
function downloadData() {
    console.log('개인 데이터를 JSON 형태로 다운로드합니다.');
    showMessageModal('개인 데이터를 JSON 형태로 다운로드합니다 (데모).');
}

// 캐시 초기화 (데모)
function clearCache() {
    const confirmClear = confirm('캐시를 초기화하시겠습니까?');
    if (confirmClear) {
        console.log('캐시가 초기화되었습니다.');
        showMessageModal('캐시가 초기화되었습니다.');
    }
}

// 계정 비활성화 (데모)
function deactivateAccount() {
    const confirmDeactivate = confirm('계정을 비활성화하시겠습니까? 언제든지 다시 활성화할 수 있습니다.');
    if (confirmDeactivate) {
        console.log('계정이 비활성화되었습니다.');
        showMessageModal('계정이 비활성화되었습니다.');
    }
}

// 계정 삭제 (데모)
function deleteAccount() {
    const confirmation = prompt('계정을 삭제하려면 "DELETE"를 입력하세요:');
    if (confirmation === 'DELETE') {
        console.log('계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다.');
        showMessageModal('계정 삭제 요청이 접수되었습니다. 7일 후 완전히 삭제됩니다.');
    } else {
        showMessageModal('계정 삭제가 취소되었습니다.');
    }
}

// 로그아웃
function logout() {
    const confirmLogout = confirm('로그아웃 하시겠습니까?');
    if (confirmLogout) {
        window.location.href = 'login.html'; // 로그인 페이지로 이동
    }
}

// 학번 파라미터가 없으면 기본값으로 리다이렉트하는 로직
window.addEventListener('load', function() {
    const urlParams = new URLSearchParams(window.location.search);
    let studentId = urlParams.get('studentId');

    if (!studentId) {
        console.warn("studentId 파라미터가 URL에 없습니다. 임시 학번을 사용합니다.");
        studentId = "2025004001"; // 기본 학번 설정
    }

    // 현재 URL에 studentId 파라미터가 없거나, 경로가 /student_mypage.do 가 아니면 리다이렉트
    if (!window.location.pathname.includes('/student_mypage.do') || !urlParams.has('studentId')) {
        window.location.href = '/student_mypage.do?studentId=' + studentId;
    }
});
