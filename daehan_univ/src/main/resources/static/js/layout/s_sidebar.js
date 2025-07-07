// s_sidebar.js
console.log("sidebar_확인")
// DOMContentLoaded 시점에 실행
document.addEventListener("DOMContentLoaded", async () => {
    console.log("sidebar_확인 진입 확인")
    const token = localStorage.getItem("accessToken");
    console.log("토큰 : "+token);
    //if (!token) return;

    try {
        console.log("fetch 메소드로 /api/user/me/student 접근 전");
        // JWT 인증 기반 사용자 정보 요청
        const res = await fetch("/api/user/me/student", {
            
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        console.log("fetch 메소드로 /api/user/me/student 접근 후");
        if (!res.ok) throw new Error("학생 인증 실패");

        const data = await res.json();
        console.log("sidebar data 확인 "+data.userId);
        // 마이페이지 링크 동적 설정
        const mypageLink = document.getElementById("mypage-link");
        if (mypageLink) {
            // href 직접 설정 (이 방식이 가장 안전함)
            mypageLink.href = `/students/mypage?studentId=${data.userId}`;
        }

    } catch (err) {
        console.error("❌ 마이페이지 링크 설정 실패:", err);
    }
});
