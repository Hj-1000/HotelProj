document.addEventListener('DOMContentLoaded', function () {
    const bell = document.getElementById('notification-bell');
    const notificationList = document.getElementById('notification-list');
    const unreadCount = document.getElementById('unread-count');

    let notifications = [];
    let isNotificationsFetched = false;  // 알림 목록을 가져왔는지 여부

    // 알림 목록을 가져오는 함수
    function fetchNotifications() {
        if (isNotificationsFetched) return;  // 알림이 이미 불러와졌다면 중복 호출 방지
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
        const unreadNotifications = notifications.filter(notification => !notification.isRead); // 읽지 않은 알림만 필터링
        notificationList.innerHTML = '';  // 기존 목록 초기화

        if (unreadNotifications.length === 0) {
            notificationList.innerHTML = '<li><span class="dropdown-item">알림이 없습니다.</span></li>';
        } else {
            unreadNotifications.forEach(notification => {
                const notificationItem = document.createElement('li');
                notificationItem.classList.add('dropdown-item');

                // 알림을 클릭했을 때 Q&A로 이동하는 링크 추가
                const link = document.createElement('a');
                if (notification.qnaId) {
                    link.href = `/qna/read/${notification.qnaId}`;  // Q&A의 ID로 링크 설정
                } else {
                    link.href = '#';  // qnaId가 없으면 링크를 '#'
                }
                link.textContent = notification.notificationMessage;
                link.classList.add('text-decoration-none', notification.isRead ? 'text-muted' : 'text-dark');

                // 알림 클릭 시 읽음 처리
                link.addEventListener('click', (e) => {
                    e.preventDefault();  // 기본 링크 동작 방지

                    // 알림 읽음 처리 요청
                    axios.patch(`/notifications/${notification.notificationId}/read`)
                        .then(() => {
                            // 서버에서 처리된 후 읽은 알림 상태로 변경
                            notification.isRead = true;

                            // UI 업데이트
                            updateNotificationList();  // UI 갱신
                            link.classList.remove('text-dark');  // 읽은 알림 스타일로 변경
                            link.classList.add('text-muted');   // 읽은 알림 스타일 변경

                            // Q&A 페이지로 이동
                            if (notification.qnaId) {
                                window.location.href = link.href;  // Q&A 페이지로 이동
                            }

                            // 알림 수 감소 처리
                            updateUnreadCount();  // 새 알림 수 업데이트
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

    // 읽지 않은 알림 갯수를 업데이트하는 함수
    function updateUnreadCount() {
        const unreadNotifications = notifications.filter(notification => !notification.isRead); // 읽지 않은 알림만 필터링
        unreadCount.textContent = unreadNotifications.length;  // 새 알림 수 업데이트
    }

    // 알림 벨 클릭 시 알림 목록 토글
    bell.addEventListener('click', function () {
        if (notificationList.style.display === 'none' || notificationList.style.display === '') {
            notificationList.style.display = 'block';  // 알림 목록을 보이도록 설정
            fetchNotifications();  // 알림 목록을 불러오기
        } else {
            notificationList.style.display = 'none';  // 알림 목록 숨기기
        }
    });

    // 페이지 로드 시 알림 목록을 불러오기
    fetchNotifications();

    // 새 알림 수 1초마다 확인
    setInterval(function() {
        fetchUnreadNotifications();
        updateUnreadCount();
    }, 5000);

    // 새 알림 수 가져오는 함수
    function fetchUnreadNotifications() {
        fetch('/notifications/unreadCount')
            .then(response => response.json())
            .then(data => {
                unreadCount.textContent = data;
            })
            .catch(error => console.error('Error fetching notifications:', error));
    }
});
