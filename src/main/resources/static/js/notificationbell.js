document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM 로드 완료');
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');
    let notifications = [];
    let isNotificationsFetched = false;  // 알림 목록을 가져왔는지 여부를 추적하는 변수

    // 알림 목록을 백엔드에서 가져오기
    async function fetchNotifications() {
        if (isNotificationsFetched) return;

        try {
            const response = await axios.get('/notifications');
            console.log('알림 데이터:', response.data); // 데이터 확인
            notifications = response.data;
            updateUnreadCount();
            updateNotificationList();
            isNotificationsFetched = true;
        } catch (error) {
            console.error('알림을 가져오는 데 실패했습니다.', error);
        }
    }

    // 읽지 않은 알림 개수 업데이트
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(notification => !notification.isRead);
        unreadCount.textContent = unreadNotifications.length;
    }

    // 알림 목록 업데이트
    function updateNotificationList() {
        notificationList.innerHTML = '';  // 기존 알림 내용 지우기
        if (notifications.length === 0) {
            notificationList.innerHTML = '<li><span class="dropdown-item">알림이 없습니다.</span></li>';
        } else {
            notifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('notification-item');
                if (!notification.isRead) {
                    notificationItem.classList.add('unread');
                } else {
                    notificationItem.classList.add('read');
                }
                notificationItem.textContent = notification.message;

                // 알림 클릭 시 읽음 처리
                notificationItem.addEventListener('click', async () => {
                    if (!notification.isRead) {
                        await markAsRead(notification.id);
                        notification.isRead = true;
                        updateUnreadCount();
                        updateNotificationList();
                    }
                });

                notificationList.appendChild(notificationItem);
            });
        }
    }

    // 알림을 읽음 처리하는 함수
    async function markAsRead(notificationId) {
        try {
            await axios.patch(`/notifications/${notificationId}/read`);
        } catch (error) {
            console.error('알림 읽음 처리에 실패했습니다.', error);
        }
    }

    // 벨 클릭 시 알림 리스트 보이기/숨기기
    bell.addEventListener('click', function () {
        if (notificationList.style.display === 'none' || notificationList.style.display === '') {
            notificationList.style.display = 'block';
            fetchNotifications();  // 알림 목록을 불러오기
        } else {
            notificationList.style.display = 'none';
        }
    });

    // 페이지 로드 시 알림 목록 가져오기
    fetchNotifications();
});
