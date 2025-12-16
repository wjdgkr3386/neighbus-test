var stompClient = null;

// 1. 웹소켓 연결 함수
function connect() {
    var socket = new SockJS('/ws-stomp');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('웹소켓 연결 성공: ' + frame);

        // 알림 구독
        stompClient.subscribe('/user/queue/notifications', function (message) {
            console.log("알림 내용: " + message.body);
            alert(message.body); // 실시간 알림 테스트 (나중에 토스트 메시지로 변경 가능)
            
            // (선택사항) 여기에 읽지 않은 알림 뱃지 카운트 갱신 로직 추가 가능
        });
    });
}

var stompClient = null;

// 1. 뱃지 숫자 갱신 함수 (새로 추가됨!)
function updateBadgeCount() {
    fetch('/api/notifications/count')
        .then(res => res.text())
        .then(count => {
            const badge = document.getElementById('alarmBadge');
            if (badge) {
                if (parseInt(count) > 0) {
                    badge.style.display = 'inline-block'; // 숫자 보이기
                    badge.innerText = count;
                } else {
                    badge.style.display = 'none'; // 0이면 숨기기
                }
            }
        })
        .catch(err => console.error("뱃지 업데이트 실패", err));
}

function connect() {
    var socket = new SockJS('/ws-stomp');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('웹소켓 연결 성공');

        // 연결되자마자 현재 안 읽은 개수 확인
        updateBadgeCount(); 

        stompClient.subscribe('/user/queue/notifications', function (message) {
            // 🌟 알림이 오면 실행되는 곳
            
            // 1. (선택) 간단한 토스트 메시지 띄우기
            // alert("새 알림: " + message.body); 

            // 2. 종 모양 아이콘의 숫자(뱃지) 갱신!
            updateBadgeCount(); 
            
            // 3. 만약 모달이 열려있다면 리스트도 바로 갱신
            var modal = document.getElementById('notificationModal');
            if (modal && modal.classList.contains('show')) {
                openNotificationModal(); // 리스트 다시 불러오기
            }
        });
    });
}

// ... (나머지 openNotificationModal, readAndDelete 함수는 그대로 유지) ...

document.addEventListener("DOMContentLoaded", function() {
    connect();
});

// 2. 알림 모달 열기 및 목록 로드 함수
function openNotificationModal() {
    // Bootstrap 5 모달 객체 생성 (HTML에 id="notificationModal"이 있어야 함)
    var modalElement = document.getElementById('notificationModal');
    if (!modalElement) {
        console.error("모달 요소를 찾을 수 없습니다.");
        return;
    }
    var myModal = new bootstrap.Modal(modalElement);
    
    // 서버에서 알림 목록 가져오기
    fetch('/api/notifications')
        .then(response => response.json())
        .then(data => {
            const listArea = document.getElementById('notificationList');
            listArea.innerHTML = ""; // 기존 목록 초기화

            if (data.length === 0) {
                listArea.innerHTML = '<li class="list-group-item text-center">새로운 알림이 없습니다.</li>';
            } else {
                data.forEach(noti => {
                    // 읽음 여부에 따라 배경색 다르게 (1:읽음-회색, 0:안읽음-흰색/볼드)
                    let bgClass = noti.isRead == 1 ? "bg-light text-muted" : "bg-white fw-bold";
                    
                    // 날짜 포맷팅
                    let dateStr = new Date(noti.createdAt).toLocaleString();

                    // 🌟 핵심 수정 부분: onclick에 readAndDelete 함수 연결 및 스타일 클래스 적용
                    let item = `
                        <li class="list-group-item ${bgClass}">
                            <a href="#" onclick="readAndDelete(${noti.id}, '${noti.url}'); return false;" class="text-decoration-none text-dark d-block">
                                <small class="text-primary">[${noti.notificationType}]</small>
                                <div class="mb-1">${noti.content}</div>
                                <small class="text-secondary" style="font-size: 0.8rem;">
                                    ${dateStr}
                                </small>
                            </a>
                        </li>
                    `;
                    listArea.innerHTML += item;
                });
            }
            // 데이터 로드 후 모달 띄우기
            myModal.show();
        })
        .catch(error => console.error('Error:', error));
}

// 3. 알림 클릭 시: DB에서 삭제(읽음처리) 후 페이지 이동
function readAndDelete(notiId, targetUrl) {
    console.log("알림 삭제 요청 ID:", notiId);

    fetch('/api/notifications/' + notiId, {
        method: 'DELETE',
    })
    .then(response => {
        // 성공하든 실패하든 사용자는 해당 페이지로 이동시켜줌
        // (실패했다고 페이지 이동을 막으면 사용자 경험이 안 좋음)
        window.location.href = targetUrl;
    })
    .catch(error => {
        console.error('Error:', error);
        window.location.href = targetUrl;
    });
}

// 4. 페이지 로드 시 웹소켓 자동 연결
document.addEventListener("DOMContentLoaded", function() {
    connect();
});