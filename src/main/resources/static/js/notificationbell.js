document.addEventListener('DOMContentLoaded', function () {
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');


    let notifications = [];
    let isNotificationsFetched = false;

    // 📌 새로고침 시에도 즉시 알림 개수 가져오기
    fetchUnreadNotifications();

    // 📌 알림 목록을 가져오는 함수
    function fetchNotifications() {
        if (isNotificationsFetched) return;

        axios.get('/notifications/admin')
            .then(response => {
                console.log("🔔 관리자 알림 데이터:", response.data);

                // 🔥 응답 타입 확인 및 JSON 변환
                if (typeof response.data === 'string') {
                    try {
                        notifications = JSON.parse(response.data);
                    } catch (error) {
                        console.error("🚨 JSON 변환 실패! 응답이 유효한 JSON이 아닙니다.", response.data);
                        return;
                    }
                } else {
                    notifications = response.data;
                }

                // 🔥 응답이 배열인지 확인
                if (!Array.isArray(notifications)) {
                    console.error("🚨 API 응답이 배열이 아닙니다!", notifications);
                    return;
                }

                updateNotificationList();
                isNotificationsFetched = true;
            })
            .catch(error => {
                console.error("🔔 관리자 알림 로드 실패", error);
            });
    }

    // 📌 알림 목록을 업데이트하는 함수
    function updateNotificationList() {
        if (!Array.isArray(notifications)) {
            console.error("🚨 updateNotificationList: notifications가 배열이 아닙니다!", notifications);
            return;
        }

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
                            notificationItem.remove();
                            notifications = notifications.filter(n => n.notificationId !== notification.notificationId);

                            if (notifications.length === 0) {
                                notificationList.style.display = "none";
                            }

                            updateUnreadCount();

                            if (notification.qnaId) {
                                window.location.href = `/qna/read/${notification.qnaId}`;
                            }
                        })
                        .catch(error => {
                            console.error("알림 삭제 중 오류 발생:", error);
                        });
                }

                notificationItem.appendChild(link);
                notificationList.appendChild(notificationItem);
            });
        }
    }

    // 📌 새 알림 수 가져오는 함수 (새로고침 없이 알림 숫자 고정)
    function fetchUnreadNotifications() {
        fetch('/notifications/unreadCount')
            .then(response => response.json())
            .then(data => {
                if (!isNaN(data)) {
                    unreadCount.textContent = data;  // 📌 읽지 않은 알림 개수 표시
                    unreadCount.style.display = data > 0 ? 'inline' : 'none'; // 📌 0이면 숨김
                }
            })
            .catch(error => console.error('❌ Error fetching unread notifications:', error));
    }

    // 📌 읽지 않은 알림 수 업데이트
    function updateUnreadCount() {
        if (!Array.isArray(notifications)) {
            console.error("🚨 updateUnreadCount: notifications가 배열이 아닙니다!", notifications);
            return;
        }

        const unreadNotifications = notifications.filter(notification => !notification.isRead);
        unreadCount.textContent = unreadNotifications.length;
    }

    // 📌 알림 벨 클릭 시 알림 목록 고정
    bell.addEventListener('click', function (event) {
        event.preventDefault();
        event.stopImmediatePropagation();

        // 알림 목록 토글
        if (notificationList.style.display === 'none' || notificationList.style.display === '') {
            notificationList.style.display = 'block';

            // 📌 Q&A가 내려가는 것을 방지
            qnaSection.style.position = 'fixed';
            qnaSection.style.top = '100px'; // 원하는 위치로 조정
        } else {
            notificationList.style.display = 'none';

            // 📌 Q&A 위치 원래대로 복구
            qnaSection.style.position = 'relative';
        }
    });


    // 📌 페이지 로드 시 알림 목록을 불러오기
    fetchNotifications();
});
