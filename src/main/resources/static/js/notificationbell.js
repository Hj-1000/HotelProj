document.addEventListener('DOMContentLoaded', function () {
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');
    const qnaDropdown = document.getElementById('qna-dropdown'); // Q&A 드롭다운을 추가

    let notifications = [];
    let qnas = []; // Q&A 목록을 담을 변수 추가
    let isNotificationsFetched = false;
    let isQnasFetched = false; // Q&A 목록을 가져왔는지 여부를 추적

    // 알림 목록을 가져오는 함수
    function fetchNotifications() {
        axios.get('/notifications')
            .then(response => {
                console.log("알림 데이터:", response.data);  // 알림 데이터를 콘솔에 확인
                notifications = response.data;  // 상태 변수 업데이트
                updateNotificationList();  // 알림 목록 업데이트
            })
            .catch(error => {
                console.error("알림 데이터 로드 실패", error);
            });
    }

    // Q&A 목록을 가져오는 함수
    async function fetchQnas() {
        if (isQnasFetched) return;
        try {
            const response = await axios.get('/qna/list'); // Q&A 목록을 가져오는 엔드포인트
            if (response.data && Array.isArray(response.data)) {
                qnas = response.data;
                console.log("Q&A 데이터:", qnas); // 로그 추가
                updateQnaDropdown();
            } else {
                console.error('Q&A 데이터를 올바르게 받지 못했습니다.', response.data);
            }
            isQnasFetched = true;
        } catch (error) {
            console.error('Q&A 목록을 가져오는 데 실패했습니다.', error);
        }
    }

    // 알림 목록을 업데이트하는 함수
    function updateNotificationList() {
        notificationList.innerHTML = ''; // 기존 목록 초기화
        if (notifications.length === 0) {
            notificationList.innerHTML = '<li><span class="dropdown-item">알림이 없습니다.</span></li>';
        } else {
            notifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('dropdown-item');
                notificationItem.textContent = notification.notificationMessage;
                notificationList.appendChild(notificationItem);
            });
        }
    }

    // 읽지 않은 알림 갯수를 업데이트하는 함수
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(notification => !notification.isRead);
        unreadCount.textContent = unreadNotifications.length;
    }

    // Q&A 드롭다운을 업데이트하는 함수
    function updateQnaDropdown() {
        qnaDropdown.innerHTML = ''; // 기존 Q&A 항목 지우기
        if (qnas.length === 0) {
            qnaDropdown.innerHTML = '<li><span class="dropdown-item">Q&A가 없습니다.</span></li>';
        } else {
            qnas.forEach(qna => {
                const qnaItem = document.createElement('li');
                qnaItem.classList.add('qna-item');
                qnaItem.textContent = qna.qna_title; // Q&A 제목 표시
                qnaDropdown.appendChild(qnaItem);
            });
        }
    }

    // 알림 벨 클릭 시 알림 목록 토글
    bell.addEventListener('click', function () {
        if (notificationList.style.display === 'none' || notificationList.style.display === '') {
            notificationList.style.display = 'block';
            fetchNotifications();
        } else {
            notificationList.style.display = 'none';
        }
    });

    // 페이지 로드 시 알림과 Q&A 목록을 불러오기
    fetchNotifications();
    fetchQnas(); // Q&A 목록도 가져오기
});
