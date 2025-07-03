
    // ✅ 교직원 정보 로드
    document.addEventListener("DOMContentLoaded", async () => {
    try {
    const token = localStorage.getItem("accessToken");
    const res = await fetch("/api/user/me/employee", {
    headers: {
    "Authorization": `Bearer ${token}`
}
});

    if (!res.ok) throw new Error("인증 실패");

    const data = await res.json();

    const infoBox = document.getElementById("empl-info");
    infoBox.innerHTML = `
			<div><strong>${data.emplNm}</strong> (${data.userId})</div>
			<div class="small">${data.deptCd}</div>
		`;

    document.getElementById("user-logged-in").style.display = "flex";
    document.getElementById("user-logged-out").style.display = "none";

} catch (e) {
    console.warn("로그인되지 않음 또는 에러:", e);
    document.getElementById("user-logged-in").style.display = "none";
    document.getElementById("user-logged-out").style.display = "flex";
}
});

    // ✅ 로그아웃 버튼
    function logout() {
        fetch("/api/auth/logout", {
            method: "POST"
        })
            .then(res => {
                if (!res.ok) throw new Error("로그아웃 실패");
                return res.text(); // "로그아웃 성공"
            })
            .then(() => {
                // ✅ 쿠키 삭제 후 로그인 페이지로 이동
                location.href = "/login";
            })
            .catch(err => {
                console.error("Logout error:", err);
                alert("로그아웃 중 오류가 발생했습니다.");
            });
    }

