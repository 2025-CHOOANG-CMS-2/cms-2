// 상담 채팅 기능
let chatMessages = []
let activeCounselingId = null
let isCounselingActive = false

document.addEventListener("DOMContentLoaded", () => {
  initializeCounselingChat()
  checkCounselingAvailability()
})

function initializeCounselingChat() {
  // 상담 시작 버튼 이벤트 리스너
  document.querySelectorAll(".start-counseling-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      const counselingId = this.dataset.id
      startCounseling(counselingId)
    })
  })

  // 채팅 모달 관련 이벤트 리스너
  const chatModal = document.getElementById("counselingChatModal")
  const chatCloseBtn = document.getElementById("chatCloseBtn")
  const chatMinimizeBtn = document.getElementById("chatMinimizeBtn")
  const sendBtn = document.getElementById("sendBtn")
  const messageInput = document.getElementById("messageInput")
  const floatingChatBtn = document.getElementById("floatingChatBtn")

  // 채팅 창 닫기
  chatCloseBtn.addEventListener("click", () => {
    if (confirm("상담을 종료하시겠습니까?")) {
      endCounseling()
    }
  })

  // 채팅 창 최소화 (플로팅 버튼으로 전환)
  chatMinimizeBtn.addEventListener("click", () => {
    chatModal.classList.remove("show")
    floatingChatBtn.style.display = "flex"
  })

  // 플로팅 버튼 클릭 시 채팅 창 다시 열기
  floatingChatBtn.addEventListener("click", () => {
    chatModal.classList.add("show")
    floatingChatBtn.style.display = "none"
    scrollToBottom(document.getElementById("chatMessages"))
  })

  // 메시지 전송
  sendBtn.addEventListener("click", () => sendChatMessage())

  // 엔터키로 전송 (Shift+Enter는 줄바꿈)
  messageInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      sendChatMessage()
    }
  })

  // 텍스트 영역 자동 높이 조절
  messageInput.addEventListener("input", function () {
    this.style.height = "auto"
    this.style.height = Math.min(this.scrollHeight, 100) + "px"
  })

  // 모바일 관련 이벤트 리스너
  const mobileChatOverlay = document.getElementById("mobileChatOverlay")
  const mobileChatCloseBtn = document.getElementById("mobileChatCloseBtn")
  const mobileSendBtn = document.getElementById("mobileSendBtn")
  const mobileMessageInput = document.getElementById("mobileMessageInput")

  mobileChatCloseBtn.addEventListener("click", () => {
    if (confirm("상담을 종료하시겠습니까?")) {
      endCounseling()
    } else {
      mobileChatOverlay.classList.remove("show")
      floatingChatBtn.style.display = "flex"
    }
  })

  mobileSendBtn.addEventListener("click", () => sendChatMessage("mobile"))

  mobileMessageInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      sendChatMessage("mobile")
    }
  })

  mobileMessageInput.addEventListener("input", function () {
    this.style.height = "auto"
    this.style.height = Math.min(this.scrollHeight, 100) + "px"
  })
}

function checkCounselingAvailability() {
  // 현재 시간과 예약된 상담 시간을 비교하여 상담 시작 버튼 활성화
  const now = new Date()

  document.querySelectorAll(".history-item[data-status='scheduled']").forEach((item) => {
    const counselingId = item.dataset.id
    const dateText = item.querySelector(".history-date").textContent
    const startBtn = item.querySelector(".start-counseling-btn")

    // 날짜 파싱 (예: "2025.06.09 (월) 19:15")
    const [datePart, timePart] = dateText.split(" (")[0].split(" ")
    const [year, month, day] = datePart.split(".").map(Number)
    const [hour, minute] = timePart.split(":").map(Number)

    const counselingTime = new Date(year, month - 1, day, hour, minute)
    const timeDiff = (counselingTime - now) / (1000 * 60) // 분 단위 차이

    // 상담 시간 10분 전부터 상담 시작 버튼 활성화
    if (timeDiff <= 10 && timeDiff > -60) {
      // 상담 시작 10분 전부터 종료 후 60분까지
      startBtn.disabled = false
    } else {
      startBtn.disabled = true
    }
  })
}

function startCounseling(counselingId) {
  activeCounselingId = counselingId
  isCounselingActive = true

  // 상담 정보 가져오기
  const counselingItem = document.querySelector(`.history-item[data-id="${counselingId}"]`)
  const counselorName = counselingItem
    .querySelector(".detail-item:first-child span")
    .textContent.replace("상담사: ", "")
  const counselingType = counselingItem
    .querySelector(".detail-item:nth-child(2) span")
    .textContent.replace("유형: ", "")

  // 채팅 창 제목 설정
  document.getElementById("chatCounselorName").textContent = `${counselorName} - ${counselingType}`
  document.getElementById("mobileChatCounselorName").textContent = `${counselorName} - ${counselingType}`

  // 채팅 메시지 초기화
  chatMessages = []

  // 환영 메시지 추가
  const welcomeMessage = {
    text: `안녕하세요! ${counselorName}입니다. 오늘 ${counselingType}에 대해 상담을 진행하겠습니다. 어떤 점이 궁금하신가요?`,
    type: "received",
    time: new Date(),
  }
  chatMessages.push(welcomeMessage)

  // 채팅 창 표시
  const chatModal = document.getElementById("counselingChatModal")
  chatModal.classList.add("show")

  // 메시지 로드
  loadChatMessages()

  // 모바일 체크
  if (window.innerWidth <= 768) {
    chatModal.classList.remove("show")
    document.getElementById("mobileChatOverlay").classList.add("show")
    loadChatMessages("mobile")
  }

  // 플로팅 버튼 숨기기 (채팅 창이 표시되므로)
  document.getElementById("floatingChatBtn").style.display = "none"
}

function endCounseling() {
  isCounselingActive = false
  activeCounselingId = null

  // 채팅 창 닫기
  document.getElementById("counselingChatModal").classList.remove("show")
  document.getElementById("mobileChatOverlay").classList.remove("show")

  // 플로팅 버튼 숨기기
  document.getElementById("floatingChatBtn").style.display = "none"

  // 상담 종료 알림
  showNotification("상담이 종료되었습니다.", "success")

  // 페이지 새로고침 (상담 상태 업데이트를 위해)
  setTimeout(() => {
    location.reload()
  }, 1500)
}

function loadChatMessages(platform = "desktop") {
  const messagesContainer =
    platform === "mobile" ? document.getElementById("mobileChatMessages") : document.getElementById("chatMessages")

  messagesContainer.innerHTML = ""

  chatMessages.forEach((message) => {
    addMessageToContainer(message, messagesContainer)
  })

  scrollToBottom(messagesContainer)
}

function addMessageToContainer(message, container) {
  const messageEl = document.createElement("div")
  messageEl.className = `message ${message.type}`

  const timeStr = message.time.toLocaleTimeString("ko-KR", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  })

  const avatar = message.type === "sent" ? "나" : "상담"

  messageEl.innerHTML = `
    <div class="message-avatar">${avatar}</div>
    <div class="message-content">
      <div class="message-bubble">${message.text}</div>
      <div class="message-time">${timeStr}</div>
    </div>
  `

  container.appendChild(messageEl)
}

function sendChatMessage(platform = "desktop") {
  const input =
    platform === "mobile" ? document.getElementById("mobileMessageInput") : document.getElementById("messageInput")

  const text = input.value.trim()
  if (!text) return

  // 사용자 메시지 추가
  const userMessage = {
    text: text,
    type: "sent",
    time: new Date(),
  }

  chatMessages.push(userMessage)
  input.value = ""
  input.style.height = "auto"

  // 화면 업데이트
  loadChatMessages(platform)

  // 자동 응답 시뮬레이션
  setTimeout(
    () => {
      const responses = [
        "네, 말씀해 주신 내용 잘 이해했습니다. 더 자세히 설명해 주시겠어요?",
        "좋은 질문이네요! 이 부분에 대해 함께 생각해보겠습니다.",
        "말씀해 주신 내용을 바탕으로 몇 가지 제안을 드릴 수 있을 것 같습니다.",
        "그런 고민을 하고 계시는군요. 어떤 부분이 가장 어려우신가요?",
        "충분히 이해할 수 있는 상황입니다. 단계별로 접근해보면 어떨까요?",
        "추가로 궁금한 점이 있으시면 언제든지 물어봐 주세요.",
        "그 부분에 대해서는 이런 방법도 고려해보시는 것이 좋을 것 같습니다.",
      ]

      const autoResponse = {
        text: responses[Math.floor(Math.random() * responses.length)],
        type: "received",
        time: new Date(),
      }

      chatMessages.push(autoResponse)
      loadChatMessages(platform)
    },
    1000 + Math.random() * 2000,
  )
}

function scrollToBottom(container) {
  container.scrollTop = container.scrollHeight
}

function showNotification(msg, type = "success") {
  const n = document.getElementById("notification")
  n.textContent = msg
  n.className = `notification ${type} show`
  setTimeout(() => n.classList.remove("show"), 3000)
}

function endCounselingSession() {
  if (confirm("상담을 완료하시겠습니까? 완료 후 결과를 작성해야 합니다.")) {
    isCounselingActive = false

    // 채팅 창 닫기
    document.getElementById("counselingChatModal").classList.remove("show")

    // 상담 완료 처리
    if (activeCounselingId) {
      completeCounseling(activeCounselingId)
    }

    activeCounselingId = null
  }
}

function completeCounseling(counselingId) {
  // 상담 완료 처리 로직 구현
  console.log(`상담 ${counselingId}가 완료되었습니다.`)
}

// 페이지 크기 변경 시 채팅 창 처리
window.addEventListener("resize", () => {
  if (!isCounselingActive) return

  const chatModal = document.getElementById("counselingChatModal")
  const mobileChatOverlay = document.getElementById("mobileChatOverlay")

  if (window.innerWidth <= 768) {
    // 모바일로 전환 시 데스크톱 창 닫기
    if (chatModal.classList.contains("show")) {
      chatModal.classList.remove("show")
      mobileChatOverlay.classList.add("show")
      loadChatMessages("mobile")
    }
  } else {
    // 데스크톱으로 전환 시 모바일 창 닫기
    if (mobileChatOverlay.classList.contains("show")) {
      mobileChatOverlay.classList.remove("show")
      chatModal.classList.add("show")
      loadChatMessages("desktop")
    }
  }
})

// 1분마다 상담 가능 여부 체크
setInterval(checkCounselingAvailability, 60000)
