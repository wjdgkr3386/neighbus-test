// 커스텀 모달 함수
function showModal(type, title, message, callback) {
    // 모달이 아직 없으면 생성
    let overlay = document.getElementById('customModalOverlay');
    if (!overlay) {
        createModalHTML();
        overlay = document.getElementById('customModalOverlay');
    }

    const icon = document.getElementById('modalIcon');
    const titleEl = document.getElementById('modalTitle');
    const messageEl = document.getElementById('modalMessage');
    const buttons = document.getElementById('modalButtons');

    // 아이콘 설정
    icon.className = 'modal-icon ' + type;
    const icons = {
        'success': '✓',
        'error': '✕',
        'warning': '!',
        'confirm': '?'
    };
    icon.textContent = icons[type] || '✓';

    // 제목과 메시지 설정
    titleEl.textContent = title;
    messageEl.textContent = message;

    // 버튼 설정
    if (type === 'confirm') {
        buttons.innerHTML = `
            <button class="modal-btn secondary" onclick="closeModal()">취소</button>
            <button class="modal-btn primary" onclick="confirmAction()">확인</button>
        `;
        window.confirmCallback = callback;
    } else {
        buttons.innerHTML = `
            <button class="modal-btn primary" onclick="closeModal(${callback ? 'true' : ''})">확인</button>
        `;
        if (callback) {
            window.modalCallback = callback;
        }
    }

    overlay.classList.add('active');
}

function closeModal(executeCallback) {
    const overlay = document.getElementById('customModalOverlay');
    if (overlay) {
        overlay.classList.remove('active');
    }
    if (executeCallback && window.modalCallback) {
        window.modalCallback();
        window.modalCallback = null;
    }
}

function confirmAction() {
    closeModal();
    if (window.confirmCallback) {
        window.confirmCallback();
        window.confirmCallback = null;
    }
}

// 모달 HTML 생성 함수
function createModalHTML() {
    const modalHTML = `
        <div id="customModalOverlay" class="custom-modal-overlay" onclick="if(event.target === this) closeModal()">
            <div class="custom-modal">
                <div id="modalIcon" class="modal-icon success">✓</div>
                <div id="modalTitle" class="modal-title">알림</div>
                <div id="modalMessage" class="modal-message">메시지</div>
                <div id="modalButtons" class="modal-buttons">
                    <button class="modal-btn primary" onclick="closeModal()">확인</button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 페이지 로드 시 모달 HTML 생성
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', createModalHTML);
} else {
    createModalHTML();
}
