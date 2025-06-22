

// =======================
// 전역 변수
// =======================
let questions = [];       // 현재 시험용 1차원 문항 리스트
let answers = {};         // { qstId: optionId }
let currentIndex = 0;     // 현재 문항 인덱스 (0부터)
let timerInterval = null; // 타이머 setInterval ID
let startTime = null;     // 진단 시작 시각 (timestamp)

// =======================
// 페이지 로드 시 상위역량 옵션 불러오기
// =======================
window.addEventListener('DOMContentLoaded', () => {
    loadCompetencyOptions();
    // 개발 단계에서는 임시 학번/ID 사용
    loadDiagnosisStatus(2025005001);
});

/**
 * 상위역량 목록(API: GET /api/competencies/list)을 불러와
 * #competencySelect 셀렉트 박스에 옵션으로 채웁니다.
 */
async function loadCompetencyOptions() {
    try {
        const res = await fetch('/api/competencies/list');
        if (!res.ok) throw new Error('상위역량 로드 실패');
        const list = await res.json();
        const select = document.getElementById('competencySelect');
        select.innerHTML = '<option value="">-- 역량을 선택하세요 --</option>';
        list.forEach(item => {
            const o = document.createElement('option');
            o.value = item.cciId;
            o.textContent = item.cciNm;
            select.appendChild(o);
        });
    } catch (err) {
        alert('상위역량 로드 오류: ' + err.message);
    }
}

// =======================
// 1) Overview 탭의 "진단 시작" 버튼 핸들러
// =======================
function startNewDiagnosis() {
    // Overview 탭에서 Test 탭으로 전환만 수행
    const testTabLink = document.querySelector('a[href="#testTab"]');
    new bootstrap.Tab(testTabLink).show();
}

// =======================
// 2) Test 탭에서 "시험 시작" 버튼 핸들러
// =======================
async function loadCompetencyQuestions() {
    const select = document.getElementById('competencySelect');
    const cciId = select.value;
    if (!cciId) {
        return alert('진단할 역량을 선택해주세요.');
    }

    try {
        // API: GET /api/diagnosis/questions/{cciId}
        const res = await fetch(`/api/diagnosis/questions/${cciId}`);
        if (!res.ok) throw new Error('문항 로드 실패');
        const dto = await res.json();

        // 트리 구조 → 1차원 배열(flatten)
        questions = flattenQuestions([dto]);

        // 초기화
        currentIndex = 0;
        answers = {};

        // 타이머, 첫 문항 렌더링, 진행률 초기화
        startTimer();
        renderQuestion();
        updateProgress();
    } catch (err) {
        alert('문항 데이터를 불러오는데 실패했습니다: ' + err.message);
    }
}

/**
 * 트리 구조(CoreCptInfoDTO)를 순회하며
 * 모든 질문(CoreCptQstDTO)을 1차원 배열로 반환합니다.
 */
function flattenQuestions(nodes) {
    let out = [];
    nodes.forEach(n => {
        if (n.questions) {
            out = out.concat(n.questions);
        }
        if (n.children) {
            out = out.concat(flattenQuestions(n.children));
        }
    });
    return out;
}

// =======================
// 3) 문항 렌더링
// =======================
function renderQuestion() {
    const container = document.getElementById('questionContainer');
    container.innerHTML = ''; // 기존 지우기

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
    </div>
  `;
    container.appendChild(card);

    // 이전 선택 재표시
    if (answers[q.qstId] !== undefined) {
        const prev = container.querySelector(`input[value="${answers[q.qstId]}"]`);
        if (prev) prev.checked = true;
    }

    // 선택 이벤트 바인딩
    container.querySelectorAll('input[name="answer"]').forEach(inp => {
        inp.addEventListener('change', e => {
            answers[q.qstId] = Number(e.target.value);
            updateNavButtons();
        });
    });

    updateNavButtons();
}

// =======================
// 4) 진행률 / 버튼 상태 업데이트
// =======================
function updateProgress() {
    const pct = ((currentIndex + 1) / questions.length) * 100;
    document.getElementById('progressBar').style.width = pct + '%';
    document.getElementById('currentQuestion').textContent = currentIndex + 1;
    document.getElementById('totalQuestions').textContent = questions.length;
}

function updateNavButtons() {
    document.getElementById('prevBtn').disabled = (currentIndex === 0);
    document.getElementById('nextBtn').style.display =
        (currentIndex < questions.length - 1 ? 'inline-block' : 'none');
    document.getElementById('submitBtn').style.display =
        (currentIndex === questions.length - 1 ? 'inline-block' : 'none');

    const q = questions[currentIndex];
    const answered = answers[q.qstId] !== undefined;
    document.getElementById('nextBtn').disabled = !answered;
    document.getElementById('submitBtn').disabled = !answered;
}

// =======================
// 5) 이전/다음 이동
// =======================
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

// =======================
// 6) 타이머 시작/정지
// =======================
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

// =======================
// 7) 진단 결과 제출
// =======================
async function submitDiagnosis() {
    // (1) selectedCciId, answers, elapsedSeconds 준비
    const selectedCciId = +document.getElementById('competencySelect').value;
    const elapsed = Math.floor((Date.now() - startTime) / 1000);

    // (2) 테스트용 학번 (개발 단계에서 하드코딩 혹은 로그인 후 저장한 값)
    const studentUserId = '2025005001';  // ← 실제 테스트할 학번으로 바꿔주세요

    const payload = {
        studentUserId,
        answers,
        elapsedSeconds: elapsed
    };

    // (3) POST /api/diagnosis/submit/{cciId}
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
    showResult(result);
}

/**
 * 최종 결과 화면 표시
 * @param {{ evalCode: string, totalScore: number, ... }} result
 */
function showResult(result) {
    // TODO: 실제 UI 요소에 값 바인딩, 차트 그리기 등
    alert(`진단 완료! 세션 코드: ${result.evalCode}\n총점: ${result.totalScore}점`);
}
/**
 * ✅ 진단 현황 데이터 렌더링 (DB 기반 아이콘/색상 수신)
 */
async function loadDiagnosisStatus(studentId) {
    try {
        const res = await fetch(`/api/diagnosis/latest/${studentId}`);
        if (!res.ok) throw new Error("진단 현황 로드 실패");

        const data = await res.json();

        // 종합 점수 처리
        // 종합 점수 처리
        document.getElementById("totalScore").textContent = data.totalScore !== null ? data.totalScore : "-";
        document.querySelector(".competency-level").textContent = data.levelText !== null ? data.levelText + " 수준" : "진단 미완료";
        document.querySelector(".competency-overview p").textContent =
            data.latestDate ? `최근 진단일: ${data.latestDate}` : "모든 상위역량 진단 완료 시 종합 점수가 표시됩니다.";

        // 상세 점수 렌더링
        const container = document.querySelector(".diagnosis-card");
        container.innerHTML = `<h5 class="mb-4">
            <i class="bi bi-bar-chart me-2"></i>역량별 상세 점수
        </h5>`;

        data.details.forEach(item => {
            const diff = item.score - item.avgScore;
            const diffText = (diff >= 0 ? "+" : "") + diff + "점";

            container.innerHTML += `
                <div class="competency-item">
                    <div class="competency-name">
                        <span><i class="${item.iconClass} me-2"></i>${item.competencyName}</span>
                        <strong>${item.score}점</strong>
                    </div>
                    <div class="competency-progress">
                        <div class="progress-bar-custom ${item.progressClass}" style="width: ${item.score}%"></div>
                    </div>
                    <small class="text-muted">전체 평균: ${item.avgScore}점 (${diffText})</small>
                </div>
            `;
        });
    } catch (err) {
        alert("진단 현황을 불러오는데 실패했습니다: " + err.message);
    }
}
