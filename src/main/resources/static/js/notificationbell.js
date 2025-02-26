document.addEventListener('DOMContentLoaded', function () {
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');


    if (!bell) {
        return;
    }

    if (!notificationList) {
        return;
    }


    let notifications = [];

    // 📌 새로고침 시에도 즉시 알림 개수 가져오기
    fetchUnreadNotifications();

    // 📌 알림 목록을 가져오는 함수
    function fetchNotifications() {
        axios.get('/notifications/admin')
            .then(response => {


                if (typeof response.data === 'string') {
                    try {
                        notifications = JSON.parse(response.data);
                    } catch (error) {
                        console.error("🚨 JSON 변환 실패!", response.data);
                        return;
                    }
                } else {
                    notifications = response.data;
                }

                if (!Array.isArray(notifications)) {
                    console.error("🚨 API 응답이 배열이 아닙니다!", notifications);
                    return;
                }

                // 🔥 댓글 알림은 제외하도록 필터링
                notifications = notifications.filter(n => !n.notificationMessage.includes("댓글"));

                updateNotificationList();

                // ✅ 알림 목록이 업데이트된 후 count를 다시 불러옴
                fetchUnreadNotifications();
            })
            .catch(error => {
                console.error("🔔 관리자 알림 로드 실패", error);
            });
    }

    // 📌 알림 목록을 업데이트하는 함수
    function updateNotificationList() {
        notificationList.innerHTML = '';

        if (notifications.length === 0) {
            displayNoNotificationsMessage();
        } else {
            notifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('dropdown-item');

                const link = document.createElement('a');
                link.href = notification.qnaId ? `/qna/read/${notification.qnaId}` : '#';
                link.textContent = notification.notificationMessage;
                link.classList.add('text-decoration-none', notification.isRead ? 'text-muted' : 'text-yellow');

                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    deleteNotification(notification, notificationItem);
                });

                notificationItem.appendChild(link);
                notificationList.appendChild(notificationItem);
            });
        }
    }

    // 📌 알림 삭제 함수
    function deleteNotification(notification, notificationItem) {
        axios.delete(`/notifications/${notification.notificationId}`)
            .then(() => {
                notificationItem.remove();
                notifications = notifications.filter(n => n.notificationId !== notification.notificationId);
                updateUnreadCount();

                if (notification.qnaId) {
                    window.location.href = `/qna/read/${notification.qnaId}`;
                }
            })
            .catch(error => {
                console.error("알림 삭제 중 오류 발생:", error);
            });
    }

    // 📌 알림이 없을 때 메시지 표시
    function displayNoNotificationsMessage() {
        const noNotificationsItem = document.createElement('li');
        noNotificationsItem.textContent = '알림이 없습니다.';
        noNotificationsItem.style.color = 'black';
        notificationList.appendChild(noNotificationsItem);
    }

    // 📌 새 알림 수 가져오기
    function fetchUnreadNotifications() {
        updateNotificationList();
        fetch('/notifications/unreadCount')
            .then(response => response.json())
            .then(data => {
                if (!isNaN(data)) {
                    // 🚨 서버에서 가져온 값 그대로 사용
                    unreadCount.textContent = data;
                    unreadCount.style.display = data > 0 ? 'inline' : 'none';

                    // 🚨 클라이언트에서 한번 더 댓글 알림 필터링
                    let filteredNotifications = notifications.filter(n => !n.notificationMessage.includes("댓글"));
                    unreadCount.textContent = filteredNotifications.length;
                    unreadCount.style.display = filteredNotifications.length > 0 ? 'inline' : 'none';

                }
            })
            .catch(error => console.error('❌ Error fetching unread notifications:', error));
    }

    // 📌 읽지 않은 알림 수 업데이트
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(n => !n.isRead);
        unreadCount.textContent = unreadNotifications.length;
    }

    // 📌 벨 아이콘 클릭 시 알림 목록 토글 & 최신 알림 가져오기
    bell.addEventListener('click', function (event) {
        event.preventDefault();
        event.stopImmediatePropagation();


        fetchNotifications(); // 🔥 벨을 누를 때마다 최신 알림 가져옴

        if (notificationList.style.display === 'none' || notificationList.style.display === '') {
            notificationList.style.display = 'block';
        } else {
            notificationList.style.display = 'none';
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        // layout에 있는 알림 벨 아이콘과 목록을 찾음
        const layoutNotificationBell = document.getElementById('layout-notification-bell');
        const layoutNotificationList = document.getElementById('layout-notification-list');

        if (layoutNotificationBell) {
            layoutNotificationBell.style.display = 'none'; // layout에서 알림 벨 숨기기
        }

        if (layoutNotificationList) {
            layoutNotificationList.style.display = 'none'; // layout에서 알림 목록 숨기기
        }
    });


    // 📌 페이지 로드 시 알림 목록을 불러오기
    fetchNotifications();
});
