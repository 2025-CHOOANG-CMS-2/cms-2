
        let selectedCompetency = 'communication';

        // 역량 선택
        function selectCompetency(competencyId) {
            selectedCompetency = competencyId;
            
            // 활성 상태 변경
            document.querySelectorAll('.list-group-item').forEach(item => {
                item.classList.remove('active');
            });
            event.target.closest('.list-group-item').classList.add('active');
            
            // 역량 상세 정보 업데이트 (실제로는 서버에서 데이터 가져옴)
            updateCompetencyDetails(competencyId);
        }

        // 역량 상세 정보 업데이트
        function updateCompetencyDetails(competencyId) {
            // 실제 구현에서는 서버에서 데이터를 가져와 표시
            console.log(`${competencyId} 역량 정보를 가져옵니다.`);
            
            // 예시 데이터 (실제로는 서버에서 가져옴)
            const competencies = {
                'communication': {
                    name: '의사소통역량',
                    code: 'COMM001',
                    description: '효과적인 의사소통 및 표현 능력을 평가하는 역량으로, 자신의 생각을 명확하게 전달하고 타인의 의견을 경청하며 이해하는 능력을 포함합니다.',
                    weight: '20%',
                    questionCount: 9,
                    color: '#3498db'
                },
                'creativity': {
                    name: '창의역량',
                    code: 'CREA001',
                    description: '새로운 아이디어를 생성하고 문제를 창의적으로 해결하는 능력을 평가하는 역량입니다.',
                    weight: '20%',
                    questionCount: 8,
                    color: '#9b59b6'
                },
                'leadership': {
                    name: '리더십역량',
                    code: 'LEAD001',
                    description: '조직을 이끌고 구성원들에게 영향력을 발휘하여 목표를 달성하는 능력을 평가하는 역량입니다.',
                    weight: '20%',
                    questionCount: 10,
                    color: '#e74c3c'
                },
                'problem-solving': {
                    name: '문제해결역량',
                    code: 'PROB001',
                    description: '문제를 정확히 인식하고 효과적인 해결책을 찾아 적용하는 능력을 평가하는 역량입니다.',
                    weight: '20%',
                    questionCount: 9,
                    color: '#f39c12'
                },
                'teamwork': {
                    name: '팀워크역량',
                    code: 'TEAM001',
                    description: '팀 내에서 효과적으로 협업하고 공동의 목표를 달성하기 위해 기여하는 능력을 평가하는 역량입니다.',
                    weight: '20%',
                    questionCount: 9,
                    color: '#27ae60'
                }
            };
            
            const comp = competencies[competencyId];
            
            // 상세 정보 HTML 생성
            const detailsHtml = `
                <h5 class="text-primary mb-3">${comp.name}</h5>
                <div class="competency-detail">
                    <div class="row mb-3">
                        <div class="col-md-3 fw-bold">역량 코드</div>
                        <div class="col-md-9">${comp.code}</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-3 fw-bold">역량 설명</div>
                        <div class="col-md-9">${comp.description}</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-3 fw-bold">가중치</div>
                        <div class="col-md-9">${comp.weight}</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-3 fw-bold">문항 수</div>
                        <div class="col-md-9">${comp.questionCount}문항</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-3 fw-bold">표시 색상</div>
                        <div class="col-md-9">
                            <div style="width: 50px; height: 25px; background-color: ${comp.color}; border-radius: 5px;"></div>
                        </div>
                    </div>
                </div>

                <h6 class="mt-4 mb-3">하위 역량 항목</h6>
            `;
            
            // 하위 역량 항목은 각 역량마다 다르게 표시 (예시)
            let subCompetenciesHtml = '';
            
            if (competencyId === 'communication') {
                subCompetenciesHtml = `
                    <div class="sub-competency-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-1">언어적 표현력</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('sub1')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('sub1')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p class="small text-muted mb-0">자신의 생각과 의견을 명확하고 효과적으로 표현하는 능력</p>
                    </div>
                    <div class="sub-competency-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-1">경청 능력</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('sub2')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('sub2')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p class="small text-muted mb-0">타인의 의견을 주의 깊게 듣고 이해하는 능력</p>
                    </div>
                    <div class="sub-competency-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-1">비언어적 소통</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('sub3')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('sub3')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p class="small text-muted mb-0">표정, 제스처 등 비언어적 요소를 통한 소통 능력</p>
                    </div>
                `;
            } else if (competencyId === 'creativity') {
                subCompetenciesHtml = `
                    <div class="sub-competency-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-1">발산적 사고</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('sub1')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('sub1')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p class="small text-muted mb-0">다양한 아이디어를 생성하는 능력</p>
                    </div>
                    <div class="sub-competency-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-1">수렴적 사고</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-1" onclick="editSubCompetency('sub2')">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteSubCompetency('sub2')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p class="small text-muted mb-0">아이디어를 분석하고 최적의 해결책을 찾는 능력</p>
                    </div>
                `;
            } else {
                subCompetenciesHtml = `
                    <div class="text-center text-muted py-3">
                        <p>하위 역량 항목이 없습니다.</p>
                    </div>
                `;
            }
            
            // 하위 역량 추가 버튼
            const addButtonHtml = `
                <div class="text-center mt-4">
                    <button class="btn btn-outline-primary" data-bs-toggle="modal" data-bs-target="#addSubCompetencyModal">
                        <i class="fas fa-plus me-2"></i>
                        하위 역량 추가
                    </button>
                </div>
            `;
            
            // 프로그램 추천 관리 섹션
            const programRecommendationHtml = `
                <hr class="my-4">

        <!-- 프로그램 추천 관리 섹션 -->
        <div class="mt-4">
            <h6 class="mb-3">
                <i class="fas fa-calendar-alt me-2 text-primary"></i>
                추천 프로그램 관리
            </h6>
            <div class="row">
                <div class="col-md-6">
                    <div class="card border-primary">
                        <div class="card-header bg-primary text-white">
                            <h6 class="mb-0">점수별 추천 프로그램</h6>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">점수 범위</label>
                                <select class="form-select" id="programScoreRange">
                                    <option value="poor">미흡 (60점 미만)</option>
                                    <option value="average">보통 (60-69점)</option>
                                    <option value="good">양호 (70-79점)</option>
                                    <option value="excellent">우수 (80점 이상)</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">추천 프로그램</label>
                                <div class="border rounded p-2" style="max-height: 200px; overflow-y: auto;">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="prog1">
                                        <label class="form-check-label" for="prog1">
                                            효과적인 커뮤니케이션 특강
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="prog2">
                                        <label class="form-check-label" for="prog2">
                                            프레젠테이션 스킬 워크숍
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="prog3">
                                        <label class="form-check-label" for="prog3">
                                            토론 및 토의 기법 세미나
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="prog4">
                                        <label class="form-check-label" for="prog4">
                                            글쓰기 역량 강화 프로그램
                                        </label>
                                    </div>
                                </div>
                            </div>
                            <button class="btn btn-primary btn-sm w-100">
                                <i class="fas fa-save me-1"></i>
                                프로그램 설정 저장
                            </button>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card border-info">
                        <div class="card-header bg-info text-white">
                            <h6 class="mb-0">현재 설정된 추천 프로그램</h6>
                        </div>
                        <div class="card-body">
                            <div class="accordion" id="programAccordion">
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#poorPrograms">
                                            <span class="badge bg-danger me-2">미흡</span> 60점 미만
                                        </button>
                                    </h2>
                                    <div id="poorPrograms" class="accordion-collapse collapse" data-bs-parent="#programAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 프로그램이 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#averagePrograms">
                                            <span class="badge bg-warning me-2">보통</span> 60-69점
                                        </button>
                                    </h2>
                                    <div id="averagePrograms" class="accordion-collapse collapse" data-bs-parent="#programAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 프로그램이 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#goodPrograms">
                                            <span class="badge bg-primary me-2">양호</span> 70-79점
                                        </button>
                                    </h2>
                                    <div id="goodPrograms" class="accordion-collapse collapse" data-bs-parent="#programAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 프로그램이 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#excellentPrograms">
                                            <span class="badge bg-success me-2">우수</span> 80점 이상
                                        </button>
                                    </h2>
                                    <div id="excellentPrograms" class="accordion-collapse collapse" data-bs-parent="#programAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 프로그램이 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 코멘트 관리 섹션 -->
        <div class="mt-4">
            <h6 class="mb-3">
                <i class="fas fa-comment-dots me-2 text-success"></i>
                점수별 코멘트 관리
            </h6>
            <div class="row">
                <div class="col-md-6">
                    <div class="card border-success">
                        <div class="card-header bg-success text-white">
                            <h6 class="mb-0">코멘트 작성</h6>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">점수 범위</label>
                                <select class="form-select" id="commentScoreRange">
                                    <option value="poor">미흡 (60점 미만)</option>
                                    <option value="average">보통 (60-69점)</option>
                                    <option value="good">양호 (70-79점)</option>
                                    <option value="excellent">우수 (80점 이상)</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">코멘트 템플릿</label>
                                <select class="form-select" id="commentTemplate">
                                    <option value="">직접 입력</option>
                                    <option value="template1">격려형 템플릿</option>
                                    <option value="template2">개선 제안형 템플릿</option>
                                    <option value="template3">구체적 조언형 템플릿</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">코멘트 내용</label>
                                <textarea class="form-control" id="commentContent" rows="4" placeholder="학생들에게 표시될 코멘트를 입력하세요..."></textarea>
                            </div>
                            <div class="mb-3">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="includePrograms">
                                    <label class="form-check-label" for="includePrograms">
                                        추천 프로그램 목록 자동 포함
                                    </label>
                                </div>
                            </div>
                            <div class="d-grid gap-2">
                                <button class="btn btn-success btn-sm">
                                    <i class="fas fa-save me-1"></i>
                                    코멘트 저장
                                </button>
                                <button class="btn btn-outline-secondary btn-sm">
                                    <i class="fas fa-eye me-1"></i>
                                    미리보기
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card border-warning">
                        <div class="card-header bg-warning text-dark">
                            <h6 class="mb-0">설정된 코멘트</h6>
                        </div>
                        <div class="card-body">
                            <div class="accordion" id="commentAccordion">
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#poorComments">
                                            <span class="badge bg-danger me-2">미흡</span> 60점 미만
                                        </button>
                                    </h2>
                                    <div id="poorComments" class="accordion-collapse collapse" data-bs-parent="#commentAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 코멘트가 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#averageComments">
                                            <span class="badge bg-warning me-2">보통</span> 60-69점
                                        </button>
                                    </h2>
                                    <div id="averageComments" class="accordion-collapse collapse" data-bs-parent="#commentAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 코멘트가 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#goodComments">
                                            <span class="badge bg-primary me-2">양호</span> 70-79점
                                        </button>
                                    </h2>
                                    <div id="goodComments" class="accordion-collapse collapse" data-bs-parent="#commentAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 코멘트가 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                                <div class="accordion-item">
                                    <h2 class="accordion-header">
                                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#excellentComments">
                                            <span class="badge bg-success me-2">우수</span> 80점 이상
                                        </button>
                                    </h2>
                                    <div id="excellentComments" class="accordion-collapse collapse" data-bs-parent="#commentAccordion">
                                        <div class="accordion-body">
                                            <small class="text-muted">설정된 코멘트가 없습니다.</small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
            
            // 전체 HTML 업데이트
            document.getElementById('competencyDetails').innerHTML = detailsHtml + subCompetenciesHtml + addButtonHtml + programRecommendationHtml;
            
            // 이벤트 리스너 다시 등록
            registerEventListeners();
        }

        // 이벤트 리스너 등록 함수 추가
        function registerEventListeners() {
            // 코멘트 템플릿 선택 이벤트
            document.getElementById('commentTemplate').addEventListener('change', function() {
                const template = this.value;
                const range = document.getElementById('commentScoreRange').value;
                const competencyName = document.querySelector('#competencyDetails h5').textContent;
                let templateText = '';
                
                if (template === 'template1') {
                    switch(range) {
                        case 'excellent':
                            templateText = `${competencyName} 역량이 매우 우수합니다! 지속적으로 발전시켜 나가시기 바랍니다.`;
                            break;
                        case 'good':
                            templateText = `${competencyName} 역량이 양호한 수준입니다. 조금 더 노력하면 우수한 수준에 도달할 수 있습니다.`;
                            break;
                        case 'average':
                            templateText = `${competencyName} 역량이 보통 수준입니다. 관련 프로그램에 참여하여 역량을 향상시켜보세요.`;
                            break;
                        case 'poor':
                            templateText = `${competencyName} 역량 개발이 필요합니다. 기초부터 차근차근 학습해보시기 바랍니다.`;
                            break;
                    }
                } else if (template === 'template2') {
                    switch(range) {
                        case 'excellent':
                            templateText = `${competencyName} 역량이 뛰어납니다. 다른 학생들을 멘토링하는 활동에 참여해보세요.`;
                            break;
                        case 'good':
                            templateText = `${competencyName} 역량을 더욱 발전시키기 위해 심화 프로그램 참여를 권장합니다.`;
                            break;
                        case 'average':
                            templateText = `${competencyName} 역량 향상을 위해 추천 프로그램에 적극적으로 참여하시기 바랍니다.`;
                            break;
                        case 'poor':
                            templateText = `${competencyName} 역량 강화를 위해 기초 프로그램부터 시작하여 단계적으로 학습하세요.`;
                            break;
                    }
                }
                
                document.getElementById('commentContent').value = templateText;
            });

            // 점수 범위 변경 시 저장된 코멘트 로드
            document.getElementById('commentScoreRange').addEventListener('change', function() {
                // 실제 구현에서는 저장된 코멘트를 불러와서 표시
                console.log('점수 범위 변경:', this.value);
            });

            // 프로그램 점수 범위 변경 시 해당 프로그램 목록 로드
            document.getElementById('programScoreRange').addEventListener('change', function() {
                // 실제 구현에서는 해당 점수 범위의 프로그램 목록을 불러와서 표시
                console.log('프로그램 점수 범위 변경:', this.value);
            });
        }

        // 역량 추가
        function addCompetency() {
            const form = document.getElementById('addCompetencyForm');
            
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }
            
            const name = document.getElementById('competencyName').value;
            const code = document.getElementById('competencyCode').value;
            const description = document.getElementById('competencyDescription').value;
            const weight = document.getElementById('competencyWeight').value;
            const color = document.getElementById('competencyColor').value;
            
            console.log('새 역량 추가:', { name, code, description, weight, color });
            alert(`역량 항목 '${name}'이(가) 추가되었습니다.`);
            
            // 모달 닫기
            const modal = bootstrap.Modal.getInstance(document.getElementById('addCompetencyModal'));
            modal.hide();
            
            // 폼 초기화
            form.reset();
        }

        // 역량 수정
        function editCompetency() {
            if (!selectedCompetency) {
                alert('수정할 역량을 선택하세요.');
                return;
            }
            
            alert(`${selectedCompetency} 역량 수정 기능을 실행합니다.`);
            // 실제 구현에서는 수정 모달 열기
        }

        // 역량 삭제
        function deleteCompetency() {
            if (!selectedCompetency) {
                alert('삭제할 역량을 선택하세요.');
                return;
            }
            
            if (confirm('선택한 역량을 정말 삭제하시겠습니까? 관련된 모든 진단 문항과 결과가 함께 삭제됩니다.')) {
                alert(`${selectedCompetency} 역량이 삭제되었습니다.`);
                // 실제 구현에서는 서버 API 호출
            }
        }

        // 하위 역량 추가
        function addSubCompetency() {
            const form = document.getElementById('addSubCompetencyForm');
            
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }
            
            const name = document.getElementById('subCompetencyName').value;
            const description = document.getElementById('subCompetencyDescription').value;
            const weight = document.getElementById('subCompetencyWeight').value;
            
            console.log('새 하위 역량 추가:', { name, description, weight });
            alert(`하위 역량 '${name}'이(가) 추가되었습니다.`);
            
            // 모달 닫기
            const modal = bootstrap.Modal.getInstance(document.getElementById('addSubCompetencyModal'));
            modal.hide();
            
            // 폼 초기화
            form.reset();
        }

        // 하위 역량 수정
        function editSubCompetency(subId) {
            alert(`하위 역량 ${subId} 수정 기능을 실행합니다.`);
            // 실제 구현에서는 수정 모달 열기
        }

        // 하위 역량 삭제
        function deleteSubCompetency(subId) {
            if (confirm('선택한 하위 역량을 정말 삭제하시겠습니까?')) {
                alert(`하위 역량 ${subId}이(가) 삭제되었습니다.`);
                // 실제 구현에서는 서버 API 호출
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            console.log('역량 항목 관리 페이지가 로드되었습니다.');
            registerEventListeners();
        });

        // 코멘트 템플릿 선택 이벤트
        document.getElementById('commentTemplate').addEventListener('change', function() {
            const template = this.value;
            const range = document.getElementById('commentScoreRange').value;
            const competencyName = document.querySelector('#competencyDetails h5').textContent;
            let templateText = '';
            
            if (template === 'template1') {
                switch(range) {
                    case 'excellent':
                        templateText = `${competencyName} 역량이 매우 우수합니다! 지속적으로 발전시켜 나가시기 바랍니다.`;
                        break;
                    case 'good':
                        templateText = `${competencyName} 역량이 양호한 수준입니다. 조금 더 노력하면 우수한 수준에 도달할 수 있습니다.`;
                        break;
                    case 'average':
                        templateText = `${competencyName} 역량이 보통 수준입니다. 관련 프로그램에 참여하여 역량을 향상시켜보세요.`;
                        break;
                    case 'poor':
                        templateText = `${competencyName} 역량 개발이 필요합니다. 기초부터 차근차근 학습해보시기 바랍니다.`;
                        break;
                }
            } else if (template === 'template2') {
                switch(range) {
                    case 'excellent':
                        templateText = `${competencyName} 역량이 뛰어납니다. 다른 학생들을 멘토링하는 활동에 참여해보세요.`;
                        break;
                    case 'good':
                        templateText = `${competencyName} 역량을 더욱 발전시키기 위해 심화 프로그램 참여를 권장합니다.`;
                        break;
                    case 'average':
                        templateText = `${competencyName} 역량 향상을 위해 추천 프로그램에 적극적으로 참여하시기 바랍니다.`;
                        break;
                    case 'poor':
                        templateText = `${competencyName} 역량 강화를 위해 기초 프로그램부터 시작하여 단계적으로 학습하세요.`;
                        break;
                }
            }
            
            document.getElementById('commentContent').value = templateText;
        });

        // 점수 범위 변경 시 저장된 코멘트 로드
        document.getElementById('commentScoreRange').addEventListener('change', function() {
            // 실제 구현에서는 저장된 코멘트를 불러와서 표시
            console.log('점수 범위 변경:', this.value);
        });

        // 프로그램 점수 범위 변경 시 해당 프로그램 목록 로드
        document.getElementById('programScoreRange').addEventListener('change', function() {
            // 실제 구현에서는 해당 점수 범위의 프로그램 목록을 불러와서 표시
            console.log('프로그램 점수 범위 변경:', this.value);
        });
    