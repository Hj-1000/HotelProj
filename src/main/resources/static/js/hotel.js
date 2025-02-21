//지역 설정 후 사용자가 수정 못하도록 막아두는 코드
document.addEventListener("DOMContentLoaded", function () {
    const hotelLocationInput = document.getElementById("hotelLocation");
    const form = document.querySelector("form");

    // 사용자가 직접 변경하지 못하도록 비활성화
    hotelLocationInput.disabled = true;

    // 폼 제출 시 roomStatus 값이 서버로 정상적으로 전송되도록 disabled 해제
    form.addEventListener("submit", function () {
        hotelLocationInput.removeAttribute("disabled");
    });

})


// 이미지 미리보기 함수
function previewImage(event) {

    console.log("수정 진입")

    const previewContainer = document.getElementById('imagePreviewContainer');
    previewContainer.innerHTML = '';  // 기존 미리보기 이미지 제거

    const files = event.target.files;

    if (files.length > 0) {
        Array.from(files).forEach(file => {
            const reader = new FileReader();

            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.alt = 'Image Preview';
                img.style.maxWidth = '200px';  // 미리보기 이미지 크기 조정
                img.style.marginTop = '10px';
                previewContainer.appendChild(img);
            }

            reader.readAsDataURL(file);  // 이미지 파일을 읽어서 미리보기
        });
    }
}


//이미지파일 검사
function validateImage(event) {
    var files = event.target.files;
    var maxSize = 5 * 1024 * 1024; // 10MB 제한
    var allowedExt = ["jpg", "jpeg", "gif", "png", "bmp", "jfif", "webp"];

    // 파일 개수 검사: 최대 5개까지 허용
    if (files.length > 5) {
        alert("최대 5장까지만 업로드할 수 있습니다.");
        event.target.value = ""; // 입력 필드 초기화
        return;
    } else if (files.length == 0) {
        alert("지사이미지는 최소 1장 이상 업로드해주세요")
        event.target.value = ""; // 입력 필드 초기화
        return;
    }

    for (var i = 0; i < files.length; i++) {
        var file = files[i];
        var fileName = file.name;
        var fileSize = file.size;
        var fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 확장자 검사
        if (!allowedExt.includes(fileExt)) {
            alert("이미지 파일(PNG, JPG, GIF, BMP, JFIF, WEBP)만 업로드 가능합니다.");
            event.target.value = ""; // 입력 필드 초기화
            return;
        }

        // 파일 크기 검사
        if (fileSize > maxSize) {
            alert("파일 크기가 5MB를 초과할 수 없습니다.");
            event.target.value = ""; // 입력 필드 초기화
            return;
        }
    }

    // 이미지 검사 후 미리보기 함수 호출
    previewImage(event);

}

// 기존 이미지 삭제 처리 함수
function deleteOldImage(imageId) {

    console.log(imageId)

    $.ajax({
        url: '/manager/hotel/image/delete/' + imageId,  // 기본 URL 포함
        method: 'DELETE',
        success: function(response) {
            if (response.success === "true") {
                alert("이미지가 삭제되었습니다.");
                // 페이지에서 해당 이미지를 삭제
                $(".oldImageFiles").hide()
                document.getElementById('image-' + imageId).remove();
            } else {
                alert("이미지 삭제에 실패했습니다.");
            }
        },
        error: function() {
            alert("서버 요청에 실패했습니다.");
        }
    });
}
