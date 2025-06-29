// =======================
// 전역 변수
// =======================
let questions = [];       // 현재 시험용 1차원 문항 리스트
let answers = {};         // { qstId: optionId }
let currentIndex = 0;     // 현재 문항 인덱스 (0부터)
let timerInterval = null; // 타이머 setInterval ID
let startTime = null;     // 진단 시작 시각 (timestamp)
let STUDENT_ID = null;     // 로그인된 사용자 userIdx (Long)
let studentNo = null;      // 로그인된 사용자 userId (학번 문자열)
let allPrograms = [];  // 전역에 추가
// =======================
// 페이지 로드 시 상위역량 옵션 및 최신 현황 불러오기
// =======================
window.addEventListener('DOMContentLoaded', async () => {
    await loadLoginUser();             // ✅ 로그인 사용자 정보 먼저 가져오기
    loadCompetencyOptions();          // 역량 목록
    loadDiagnosisResults(studentNo);  // 진단 결과(그래프/바/코멘트)
    loadDiagnosisStatus(STUDENT_ID, studentNo);  // 진단 상태(최근 점수)
    loadRecommendedPrograms();        // 추천 비교과 프로그램 목록
});
async function loadLoginUser() {
    try {
        const res = await fetch('/api/user/me/student');  // JWT 쿠키 기반 인증
        if (!res.ok) throw new Error('로그인 사용자 정보 조회 실패');

        const user = await res.json();
        STUDENT_ID = user.userIdx;
        studentNo = user.userId;

        console.log("✅ 로그인 사용자 정보:", user);
    } catch (err) {
        alert('로그인 상태가 아닙니다. 다시 로그인해주세요.');
        location.href = "/login";
    }
}
async function loadCompetencyOptions() {
    try {
        const res = await fetch('/api/competencies/list');
        if (!res.ok) throw new Error('상위역량 로드 실패');
        const list = await res.json();

        // 기존 시험용 select
        const select = document.getElementById('competencySelect');
        select.innerHTML = '<option value="">-- 역량을 선택하세요 --</option>';

        // 필터용 select
        const filterSelect = document.getElementById('cptFilter');
        filterSelect.innerHTML = '<option value="">전체</option>';

        list.forEach(item => {
            // 시험용 select
            const o = document.createElement('option');
            o.value = item.cciId;
            o.textContent = item.cciNm;
            select.appendChild(o);

            // 필터용 select
            const f = document.createElement('option');
            f.value = item.cciNm;
            f.textContent = item.cciNm;
            filterSelect.appendChild(f);
        });
    } catch (err) {
        alert('상위역량 로드 오류: ' + err.message);
    }
}

function startNewDiagnosis() {
    const testTabLink = document.querySelector('a[href="#testTab"]');
    new bootstrap.Tab(testTabLink).show();
}

async function loadCompetencyQuestions() {
    const cciId = document.getElementById('competencySelect').value;
    if (!cciId) return alert('진단할 역량을 선택해주세요.');

    try {
        const res = await fetch(`/api/diagnosis/questions/${cciId}`);
        if (!res.ok) throw new Error('문항 로드 실패');
        const dto = await res.json();
        questions = flattenQuestions([dto]);
        currentIndex = 0;
        answers = {};
        startTimer();
        renderQuestion();
        updateProgress();
    } catch (err) {
        alert('문항 데이터를 불러오는데 실패했습니다: ' + err.message);
    }
}

function flattenQuestions(nodes) {
    let out = [];
    nodes.forEach(n => {
        if (n.questions) out = out.concat(n.questions);
        if (n.children) out = out.concat(flattenQuestions(n.children));
    });
    return out;
}

function renderQuestion() {
    const container = document.getElementById('questionContainer');
    container.innerHTML = '';
    const q = questions[currentIndex];
    const card = document.createElement('div');
    card.className = 'question-card';
    card.innerHTML = `
        <div class="question-number">${currentIndex + 1}</div>
        <div class="question-text">${q.questionText}</div>
        <div class="answer-options">
        ${q.options.map(o => `
            <label class="answer-option">
              <input type="radio" name="answer" value="${o.optionId}">
              ${o.optionText}
            </label>
        `).join('')}
        </div>`;
    container.appendChild(card);

    if (answers[q.qstId] !== undefined) {
        const prev = container.querySelector(`input[value="${answers[q.qstId]}"]`);
        if (prev) prev.checked = true;
    }

    container.querySelectorAll('input[name="answer"]').forEach(inp => {
        inp.addEventListener('change', e => {
            answers[q.qstId] = Number(e.target.value);
            updateNavButtons();
        });
    });
    updateNavButtons();
}

function updateProgress() {
    const pct = ((currentIndex + 1) / questions.length) * 100;
    document.getElementById('progressBar').style.width = pct + '%';
    document.getElementById('currentQuestion').textContent = currentIndex + 1;
    document.getElementById('totalQuestions').textContent = questions.length;
}

function updateNavButtons() {
    document.getElementById('prevBtn').disabled = (currentIndex === 0);
    document.getElementById('nextBtn').style.display = (currentIndex < questions.length - 1 ? 'inline-block' : 'none');
    document.getElementById('submitBtn').style.display = (currentIndex === questions.length - 1 ? 'inline-block' : 'none');
    const q = questions[currentIndex];
    const answered = answers[q.qstId] !== undefined;
    document.getElementById('nextBtn').disabled = !answered;
    document.getElementById('submitBtn').disabled = !answered;
}

function previousQuestion() {
    if (currentIndex > 0) {
        currentIndex--;
        renderQuestion();
        updateProgress();
    }
}

function nextQuestion() {
    if (currentIndex < questions.length - 1) {
        currentIndex++;
        renderQuestion();
        updateProgress();
    }
}

function startTimer() {
    startTime = Date.now();
    document.getElementById('timerDisplay').style.display = 'inline-flex';
    timerInterval = setInterval(() => {
        const s = Math.floor((Date.now() - startTime) / 1000);
        const m = String(Math.floor(s / 60)).padStart(2, '0');
        const ss = String(s % 60).padStart(2, '0');
        document.getElementById('timerText').textContent = `${m}:${ss}`;
    }, 1000);
}

function stopTimer() {
    clearInterval(timerInterval);
    document.getElementById('timerDisplay').style.display = 'none';
}

async function submitDiagnosis() {
    const selectedCciId = +document.getElementById('competencySelect').value;
    const elapsed = Math.floor((Date.now() - startTime) / 1000);
    const payload = { studentUserId: studentNo.toString(), answers, elapsedSeconds: elapsed };

    const res = await fetch(`/api/diagnosis/submit/${selectedCciId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        const err = await res.text();
        throw new Error('제출 실패: ' + err);
    }

    const result = await res.json();
    alert(`진단 완료! 세션 코드: ${result.evalCode}\n총점: ${result.totalScore}점`);
    await loadDiagnosisStatus(STUDENT_ID, studentNo);
}

async function loadDiagnosisStatus(studentId, studentNo) {
    try {
        // 최신 진단 API 호출
        const res = await fetch(`/api/diagnosis/latest-by-eval/${studentId}/${studentNo}`);
        if (!res.ok) throw new Error("진단 현황 로드 실패");
        const data = await res.json();

        // 종합 점수와 수준 표시
        const scoreEl = document.getElementById("totalScore");
        if (scoreEl) scoreEl.textContent = data.totalScore !== null ? data.totalScore : "-";

        const levelEl = document.querySelector(".competency-level");
        if (levelEl) levelEl.textContent = data.levelText ? data.levelText + " 수준" : "진단 미완료";

        const dateEl = document.querySelector(".competency-overview p");
        if (dateEl) dateEl.textContent = data.latestDate ? `최근 진단일: ${data.latestDate}` : "최근 진단일 정보 없음";


        // 상세 점수 표시
        const container = document.querySelector(".diagnosis-card");
        container.innerHTML = `<h5 class="mb-4">
            <i class="bi bi-bar-chart me-2"></i>역량별 상세 점수
        </h5>`;

        data.details.forEach(item => {
            container.innerHTML += `
                <div class="competency-item">
                    <div class="competency-name">
                        <span>${item.competencyName}</span>
                        <strong>${item.score}점</strong>
                    </div>
                    <div class="competency-progress">
                        <div class="progress-bar-custom" style="width: ${item.score}%;"></div>
                    </div>
                </div>
            `;
        });
    } catch (err) {
        alert("진단 현황을 불러오는데 실패했습니다: " + err.message);
    }
}


// ✅ 진단 결과 로드 및 렌더링
async function loadDiagnosisResults(studentNo) {
    try {
        const response = await fetch(`/api/diagnosis/analysis/${studentNo}`);
        if (!response.ok) throw new Error(`HTTP 오류! 상태: ${response.status}`);

        const data = await response.json();

        // ✅ Chart: 결과 분석 탭에 렌더링
        renderChart(
            data.map(d => d.competencyName),
            data.map(d => d.score),
            data.map(d => d.colorHex || '#999')
        );

        // ✅ Bar: 진단현황 탭에 렌더링
        renderBarScores(data);
        // 🔹 상세 항목 + 코멘트 렌더링
        renderDetailItems(data);
    } catch (error) {
        console.error('진단 결과 로드 실패:', error);
        alert('진단 결과를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.');
    }

}


// ✅ Chart.js 차트 렌더링
function renderChart(labels, scores, colors) {
    // 기존 차트 제거
    const canvasContainer = document.getElementById('chartContainer');
    canvasContainer.innerHTML = '<canvas id="resultChart"></canvas>';

    const ctx = document.getElementById('resultChart').getContext('2d');
    new Chart(ctx, {
        type: 'radar', // or 'bar' if preferred
        data: {
            labels: labels,
            datasets: [{
                label: '역량 점수',
                data: scores,
                backgroundColor: colors.map(c => c + '33'), // 투명도 20%
                borderColor: colors,
                borderWidth: 2
            }]
        },
        options: {
            scales: {
                r: {
                    angleLines: { display: false },
                    suggestedMin: 0,
                    suggestedMax: 100
                }
            }
        }
    });
}
function renderBarScores(data) {
    const container = document.getElementById('barScoreContainer');
    if (!container) return;  // 안전장치

    container.innerHTML = '';

    data.forEach(item => {
        const div = document.createElement('div');
        div.className = 'competency-item mb-3';

        div.innerHTML = `
            <div class="competency-name d-flex justify-content-between">
                <span>${item.competencyName}</span>
                <strong>${item.score}점</strong>
            </div>
            <div class="competency-progress">
                <div class="progress-bar-custom" 
                     style="width:${item.score / 2}%; background:${item.colorHex};"></div>
            </div>
            <small class="text-muted">${item.comment || ''}</small>
        `;
        container.appendChild(div);
    });
}
// ✅ 상세 항목 + content 출력
function renderDetailItems(data) {
    const container = document.getElementById('diagnosisResultDetails');
    container.innerHTML = '';

    data.forEach(item => {
        const div = document.createElement('div');
        div.className = 'competency-item mb-3 p-3 border rounded bg-light';

        div.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2">
                <strong>${item.competencyName}</strong>
                <span class="badge bg-primary">${item.score}점</span>
            </div>
            <div class="competency-progress mb-2">
                <div class="progress-bar-custom" 
                     style="width:${item.score / 2}%; background-color:${item.colorHex}; height: 10px; border-radius: 5px;"></div>
            </div>
            <small class="text-muted">${item.comment || '코멘트 없음'}</small>
        `;

        container.appendChild(div);
    });
}

async function loadRecommendedPrograms() {
    try {
        const res = await fetch('/api/programs/recommend');
        if (!res.ok) throw new Error('프로그램 로드 실패');
        allPrograms = await res.json();  // 전역에 저장

        renderProgramCards(allPrograms);  // 카드 렌더링 분리
    } catch (err) {
        alert('추천 프로그램 로드 실패: ' + err.message);
    }
}
function renderProgramCards(programs) {
    const container = document.getElementById('programContainer');
    container.innerHTML = '';

    programs.forEach(p => {
        const div = document.createElement('div');
        div.className = 'col-md-4 mb-3';
        div.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h6 class="card-title">${p.prgNm}</h6>
                    <p class="card-text small">${p.prgDesc}</p>
                    <div class="mb-2">
                        <span class="badge" style="background-color: ${p.coreCptColorHex};">${p.coreCptName}</span>
                        <span class="badge bg-success">+${p.mileageScore}P</span>
                    </div>
                    <div class="small text-muted mb-2">
                        신청 ${p.appliedCount} / ${p.prgCapacity} 명
                    </div>
                    <small class="text-muted">신청 마감: ${p.prgEndDate}</small>
                </div>
                <div class="card-footer">
                    <button class="btn btn-sm btn-outline-primary w-100" onclick="applyProgram(${p.prgId})">
                        신청하기
                    </button>
                </div>
            </div>
        `;
        container.appendChild(div);
    });
}
function filterProgramsByCpt() {
    const cciId = document.getElementById('cptFilter').value;
    console.log("선택된 cciId:", cciId);

    const filtered = cciId !== ""
        ? allPrograms.filter(p => {
            console.log(`프로그램 ID=${p.coreCptName}, coreCptId=${p.cciNm}`);
            return String(p.coreCptName) === cciId;
        })
        : allPrograms;

    console.log("필터링 결과", filtered);
    renderProgramCards(filtered);
}