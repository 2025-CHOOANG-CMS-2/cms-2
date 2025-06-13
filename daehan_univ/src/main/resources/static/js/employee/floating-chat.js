// 플로팅 채팅 기능
let chatMessages = []
let unreadCount = 0
let isDragging = false
const dragOffset = { x: 0, y: 0 }

document.addEventListener("DOMContentLoaded", () => {
  initializeFloatingChat()
  initializeDraggableChat()
  simulateIncomingMessages()
})

function initializeFloatingChat() {
  const chatBtn = document.getElementById("floatingChatBtn")
  const chatWindow = document.getElementById("floatingChatWindow")
  const chatCloseBtn = document.getElementById("chatCloseBtn")
  const sendBtn = document.getElementById("floatingSendBtn")
  const messageInput = document.getElementById("floatingMessageInput")

  // 모바일 요소들
  const mobileChatOverlay = document.getElementById("mobileChatOverlay")
  const mobileChatCloseBtn = document.getElementById("mobileChatCloseBtn")
  const mobileSendBtn = document.getElementById("mobileSendBtn")
  const mobileMessageInput = document.getElementById("mobileMessageInput")

  // 채팅 버튼 클릭
  chatBtn.addEventListener("click", () => {
    if (window.innerWidth <= 768) {
      // 모바일에서는 바텀시트 표시
      mobileChatOverlay.classList.add("show")
      loadChatMessages("mobile")
    } else {
      // 데스크톱에서는 플로팅 창 표시
      chatWindow.classList.toggle("show")
      if (chatWindow.classList.contains("show")) {
        loadChatMessages("desktop")
        messageInput.focus()
      }
    }
    // 읽음 처리
    unreadCount = 0
    updateChatBadge()
  })

  // 채팅 창 닫기
  chatCloseBtn.addEventListener("click", () => {
    chatWindow.classList.remove("show")
  })

  mobileChatCloseBtn.addEventListener("click", () => {
    mobileChatOverlay.classList.remove("show")
  })

  // 오버레이 클릭으로 닫기
  mobileChatOverlay.addEventListener("click", (e) => {
    if (e.target === mobileChatOverlay) {
      mobileChatOverlay.classList.remove("show")
    }
  })

  // 메시지 전송
  sendBtn.addEventListener("click", () => sendMessage("desktop"))
  mobileSendBtn.addEventListener("click", () => sendMessage("mobile"))

  // 엔터키로 전송
  messageInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      sendMessage("desktop")
    }
  })

  mobileMessageInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      sendMessage("mobile")
    }
  })

  // 초기 메시지 로드
  loadInitialMessages()
}

function initializeDraggableChat() {
  const chatWindow = document.getElementById("floatingChatWindow")
  const chatHeader = chatWindow.querySelector(".chat-header")

  chatHeader.addEventListener("mousedown", (e) => {
    if (window.innerWidth <= 768) return // 모바일에서는 드래그 비활성화

    isDragging = true
    const rect = chatWindow.getBoundingClientRect()
    dragOffset.x = e.clientX - rect.left
    dragOffset.y = e.clientY - rect.top

    chatHeader.style.cursor = "grabbing"
    document.addEventListener("mousemove", handleDrag)
    document.addEventListener("mouseup", handleDragEnd)
    e.preventDefault()
  })

  function handleDrag(e) {
    if (!isDragging) return

    const x = e.clientX - dragOffset.x
    const y = e.clientY - dragOffset.y

    // 화면 경계 체크
    const maxX = window.innerWidth - chatWindow.offsetWidth
    const maxY = window.innerHeight - chatWindow.offsetHeight

    const boundedX = Math.max(0, Math.min(x, maxX))
    const boundedY = Math.max(0, Math.min(y, maxY))

    chatWindow.style.left = boundedX + "px"
    chatWindow.style.top = boundedY + "px"
    chatWindow.style.right = "auto"
    chatWindow.style.bottom = "auto"
  }

  function handleDragEnd() {
    isDragging = false
    chatHeader.style.cursor = "move"
    document.removeEventListener("mousemove", handleDrag)
    document.removeEventListener("mouseup", handleDragEnd)
  }
}

function loadInitialMessages() {
  chatMessages = [
    {
      text: "안녕하세요! 상담사 김상담입니다. 오늘 상담 어떠셨나요?",
      type: "received",
      time: new Date(Date.now() - 300000), // 5분 전
    },
    {
      text: "추가로 궁금한 점이나 상담받고 싶은 내용이 있으시면 언제든지 말씀해 주세요.",
      type: "received",
      time: new Date(Date.now() - 240000), // 4분 전
    },
  ]
}

function loadChatMessages(platform) {
  const messagesContainer =
    platform === "mobile"
      ? document.getElementById("mobileChatMessages")
      : document.getElementById("floatingChatMessages")

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

  messageEl.innerHTML = `
        <div class="message-avatar">${message.type === "sent" ? "나" : "상담"}</div>
        <div class="message-content">
            <div class="message-bubble">${message.text}</div>
            <div class="message-time">${timeStr}</div>
        </div>
    `

  container.appendChild(messageEl)
}

function sendMessage(platform) {
  const input =
    platform === "mobile"
      ? document.getElementById("mobileMessageInput")
      : document.getElementById("floatingMessageInput")
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

  // 화면 업데이트
  loadChatMessages(platform)

  // 자동 응답 시뮬레이션
  setTimeout(
    () => {
      const responses = [
        "네, 잘 이해했습니다. 더 자세히 설명해 주시겠어요?",
        "좋은 질문이네요! 이 부분에 대해 함께 생각해보겠습니다.",
        "말씀해 주신 내용을 바탕으로 몇 가지 제안을 드릴 수 있을 것 같습니다.",
        "그런 고민을 하고 계시는군요. 어떤 부분이 가장 어려우신가요?",
        "충분히 이해할 수 있는 상황입니다. 단계별로 접근해보면 어떨까요?",
      ]

      const autoResponse = {
        text: responses[Math.floor(Math.random() * responses.length)],
        type: "received",
        time: new Date(),
      }

      chatMessages.push(autoResponse)
      loadChatMessages(platform)

      // 창이 닫혀있으면 알림 표시
      const chatWindow = document.getElementById("floatingChatWindow")
      const mobileChatOverlay = document.getElementById("mobileChatOverlay")

      if (!chatWindow.classList.contains("show") && !mobileChatOverlay.classList.contains("show")) {
        unreadCount++
        updateChatBadge()
      }
    },
    1000 + Math.random() * 2000,
  )
}

function scrollToBottom(container) {
  container.scrollTop = container.scrollHeight
}

function updateChatBadge() {
  const badge = document.getElementById("chatBadge")
  if (unreadCount > 0) {
    badge.textContent = unreadCount > 9 ? "9+" : unreadCount
    badge.style.display = "flex"
  } else {
    badge.style.display = "none"
  }
}

function simulateIncomingMessages() {
  // 랜덤하게 새 메시지 시뮬레이션
  setInterval(() => {
    if (Math.random() < 0.05) {
      // 5% 확률로 새 메시지
      const incomingMessages = [
        "안녕하세요! 예약하신 상담 시간이 다가오고 있습니다.",
        "상담 준비를 위해 미리 생각해보고 싶은 질문이 있으시면 알려주세요.",
        "이전 상담에서 말씀드린 내용 중 궁금한 점이 있으시면 언제든지 문의해 주세요.",
        "오늘 하루는 어떠셨나요? 상담과 관련해서 새로운 고민이 생기셨나요?",
      ]

      const newMessage = {
        text: incomingMessages[Math.floor(Math.random() * incomingMessages.length)],
        type: "received",
        time: new Date(),
      }

      chatMessages.push(newMessage)

      // 창이 열려있으면 즉시 표시, 아니면 알림만
      const chatWindow = document.getElementById("floatingChatWindow")
      const mobileChatOverlay = document.getElementById("mobileChatOverlay")

      if (chatWindow.classList.contains("show")) {
        loadChatMessages("desktop")
      } else if (mobileChatOverlay.classList.contains("show")) {
        loadChatMessages("mobile")
      } else {
        unreadCount++
        updateChatBadge()
      }
    }
  }, 60000) // 60초마다 체크
}

// 페이지 크기 변경 시 채팅 창 처리
window.addEventListener("resize", () => {
  const chatWindow = document.getElementById("floatingChatWindow")
  const mobileChatOverlay = document.getElementById("mobileChatOverlay")

  if (window.innerWidth <= 768) {
    // 모바일로 전환 시 데스크톱 창 닫기
    if (chatWindow.classList.contains("show")) {
      chatWindow.classList.remove("show")
      mobileChatOverlay.classList.add("show")
      loadChatMessages("mobile")
    }
    // 드래그로 이동된 위치 초기화
    chatWindow.style.left = ""
    chatWindow.style.top = ""
    chatWindow.style.right = "30px"
    chatWindow.style.bottom = "100px"
  } else {
    // 데스크톱으로 전환 시 모바일 창 닫기
    if (mobileChatOverlay.classList.contains("show")) {
      mobileChatOverlay.classList.remove("show")
      chatWindow.classList.add("show")
      loadChatMessages("desktop")
    }
  }
})
