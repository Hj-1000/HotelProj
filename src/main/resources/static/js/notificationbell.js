document.addEventListener('DOMContentLoaded', function () {
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');

    let notifications = [];
    let isNotificationsFetched = false;

    // 알림 목록을 가져오는 함수
    function fetchNotifications() {
        if (isNotificationsFetched) return;
        axios.get('/notifications')
            .then(response => {
                console.log("알림 데이터:", response.data);
                notifications = response.data;
                updateNotificationList(); // 알림 목록 업데이트
                isNotificationsFetched = true;
            })
            .catch(error => {
                console.error("알림 데이터 로드 실패", error);
            });
    }


    // 알림 목록을 업데이트하는 함수
    function updateNotificationList() {
        const unreadNotifications = notifications.filter(notification => !notification.isRead);
        notificationList.innerHTML = ''; // 기존 목록 초기화

        if (unreadNotifications.length === 0) {
            notificationList.style.display = "none"; // 알림이 없으면 숨김
        } else {
            unreadNotifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('dropdown-item');

                const link = document.createElement('a');
                link.href = notification.qnaId ? `/qna/read/${notification.qnaId}` : '#';
                link.textContent = notification.notificationMessage;
                link.classList.add('text-decoration-none', notification.isRead ? 'text-muted' : 'text-yellow');

                // ✅ 알림 클릭 시 삭제 처리
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    deleteNotification(notification, notificationItem);
                });

                // ✅ 알림을 삭제하는 함수
                function deleteNotification(notification, notificationItem) {
                    axios.delete(`/notifications/${notification.notificationId}`)
                        .then(() => {
                            // UI에서 해당 알림 제거
                            notificationItem.remove();

                            // 배열에서도 삭제
                            notifications = notifications.filter(n => n.notificationId !== notification.notificationId);

                            // 모든 알림을 읽으면 UI에서 숨김
                            if (notifications.length === 0) {
                                notificationList.style.display = "none";
                            }

                            updateUnreadCount(); // 새 알림 수 업데이트

                            // 해당 QnA 페이지로 이동
                            if (notification.qnaId) {
                                window.location.href = `/qna/read/${notification.qnaId}`;
                            }
                        })
                        .catch(error => {
                            console.error("알림 삭제 중 오류 발생:", error);
                        });
                }

                // 알림 클릭 시 읽음 처리
                link.addEventListener('click', (e) => {
                    e.preventDefault();

                    axios.patch(`/notifications/${notification.notificationId}/read`)
                        .then(() => {
                            notification.isRead = true;

                            // 알림 목록에서 제거
                            notificationItem.remove();

                            // 배열에서도 삭제
                            notifications = notifications.filter(n => n.notificationId !== notification.notificationId);

                            // 모든 알림을 읽으면 UI에서 사라지도록 설정
                            if (notifications.filter(n => !n.isRead).length === 0) {
                                notificationList.style.display = "none";
                            }

                            updateUnreadCount(); // 새 알림 수 업데이트
                            if (notification.qnaId) {
                                window.location.href = link.href; // 해당 QnA로 이동
                            }
                        })
                        .catch(error => {
                            console.error("알림을 읽음 처리 중 오류 발생:", error);
                        });
                });

                notificationItem.appendChild(link);
                notificationList.appendChild(notificationItem);
            });
        }
    }


    // 새 알림 수 1초마다 확인
    setInterval(function() {
        fetchUnreadNotifications();
        updateUnreadCount();
    }, 1000);

    // 새 알림 수 가져오는 함수
    function fetchUnreadNotifications() {
        fetch('/notifications/unreadCount')
            .then(response => response.json())
            .then(data => {
                unreadCount.textContent = data;
            })
            .catch(error => console.error('Error fetching notifications:', error));
    }

    // 읽지 않은 알림 갯수를 업데이트하는 함수
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(notification => !notification.isRead); // 읽지 않은 알림만 필터링
        unreadCount.textContent = unreadNotifications.length; // 새 알림 수 업데이트
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

    // 페이지 로드 시 알림 목록을 불러오기
    fetchNotifications();
});
