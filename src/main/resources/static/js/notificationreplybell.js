document.addEventListener('DOMContentLoaded', function () {
    const replybell = document.getElementById('notification-replybell'); // 🔔 댓글 알림 벨 아이콘
    const notificationreplyList = document.getElementById('notification-reply-list'); // 📜 알림 목록
    const unreadreplyCount = document.getElementById('notification-reply-count'); // 🔢 읽지 않은 댓글 알림 개수

    // 🚨 layout에서는 실행되지 않도록 차단
    if (!replybell) {
        console.warn("❌ [notificationreplybell.js] layout에서 실행되지 않도록 차단됨.");
        return;
    }


    let notifications  = [];

    // 📌 페이지 로드 시 즉시 댓글 알림 개수 가져오기
    fetchUnreadReplyNotifications();

    // 📌 Q&A 댓글 알림 목록 가져오기 (읽음 처리 X)
    function fetchReplyNotifications() {
        axios.get('/notifications/replies')
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

                updateNotificationList();
            })
            .catch(error => {
                console.error("🔔 댓글 알림 로드 실패", error);
            });
    }

    // 📌 알림 목록을 업데이트하는 함수
    function updateNotificationList() {
        notificationreplyList.innerHTML = '';

        if (notifications.length === 0) {
            displayNoNotificationsMessage();
        } else {
            notifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('dropdown-item');

                const link = document.createElement('a');

                // 🚨 qnaId가 존재할 경우만 href 설정
                if (notification.qnaId) {
                    link.href = `/qna/read/${notification.qnaId}`;
                } else {
                    link.href = '#'; // 오류 방지
                    console.error("🚨 알림에 qnaId가 없습니다!", notification);
                }

                link.textContent = notification.notificationMessage;
                link.classList.add('text-decoration-none', notification.isRead ? 'text-muted' : 'text-yellow');

                link.addEventListener('click', (e) => {
                    e.preventDefault(); // 기본 이벤트 방지
                    deleteNotification(notification, notification.qnaId); // 삭제 후 이동
                });

                notificationItem.appendChild(link);
                notificationreplyList.appendChild(notificationItem);
            });
        }
    }

    // 📌 알림 클릭 시 삭제하고 해당 Q&A로 이동
    function deleteNotification(notification, qnaId) {
        axios.delete(`/notifications/${notification.notificationId}`)
            .then(() => {
                notifications = notifications.filter(n => n.notificationId !== notification.notificationId);
                updateUnreadCount();

                // 🔥 Q&A 상세 페이지로 이동 (비동기 문제 해결)
                if (qnaId) {
                    window.location.href = `/qna/read/${qnaId}`;
                }
            })
            .catch(error => {
                console.error("알림 삭제 중 오류 발생:", error);
                // 실패해도 글 이동을 보장하고 싶다면 아래 코드 추가
                if (qnaId) {
                    window.location.href = `/qna/read/${qnaId}`;
                }
            });
    }

    // 📌 알림이 없을 때 메시지 표시
    function displayNoNotificationsMessage() {
        const noNotificationsItem = document.createElement('li');
        noNotificationsItem.textContent = '댓글 알림이 없습니다.';
        noNotificationsItem.style.color = 'black';
        notificationreplyList.appendChild(noNotificationsItem);
    }

    // 📌 새 댓글 알림 수 가져오기
    function fetchUnreadReplyNotifications() {
        fetch('/notifications/replies/unreadCount')
            .then(response => response.json())
            .then(data => {
                if (!isNaN(data)) {
                    unreadreplyCount.textContent = data;
                    unreadreplyCount.style.display = data > 0 ? 'inline' : 'none';
                }
            })
            .catch(error => console.error('❌ 댓글 알림 수 가져오기 실패:', error));
    }

    // 📌 읽지 않은 댓글 알림 수 업데이트
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(n => !n.isRead);
        unreadreplyCount.textContent = unreadNotifications.length;
        unreadreplyCount.style.display = unreadNotifications.length > 0 ? 'inline' : 'none';
    }

    // 📌 벨 아이콘 클릭 시 알림 목록 토글 & 최신 알림 가져오기
    replybell.addEventListener('click', function (event) {
        event.preventDefault();
        event.stopImmediatePropagation();

        fetchReplyNotifications(); // 🔥 벨을 누를 때마다 최신 댓글 알림 가져옴

        if (notificationreplyList.style.display === 'none' || notificationreplyList.style.display === '') {
            notificationreplyList.style.display = 'block';
        } else {
            notificationreplyList.style.display = 'none';
        }
    });

    // 📌 페이지 로드 시 알림 목록을 불러오기
    fetchReplyNotifications();
});
