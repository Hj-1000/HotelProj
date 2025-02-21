// 이미지 파일 검사
function validateImage(event) {
    var files = event.target.files;
    var maxSize = 5 * 1024 * 1024; // 10MB 제한
    var allowedExt = ["jpg", "jpeg", "gif", "png", "bmp", "jfif", "webp"];

    // 파일 개수 검사: 최대 1개까지만 허용
    if (files.length > 1) {
        alert("본사 이미지는 1장만 업로드할 수 있습니다.");
        event.target.value = ""; // 입력 필드 초기화
        return;
    } else if (files.length == 0) {
        alert("본사 이미지는 최소 1장 이상 업로드해야 합니다.");
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
}

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

// 기존 이미지 삭제 처리 함수
function deleteOldImage(imageId) {

    console.log(imageId)

    $.ajax({
        url: '/company/image/delete/' + imageId,  // 기본 URL 포함
        method: 'DELETE',
        success: function(response) {
            if (response.success === "true") {
                alert("이미지가 삭제되었습니다.");
                // 페이지에서 해당 이미지를 삭제
                $(".oldImageFiles").hide()
                $(".newImageFiles").show()
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