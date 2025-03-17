
// "표시할 호텔장이 없습니다."가 선택되면 폼 제출을 막는 함수
function checkHotelManagerSelection() {
    var companyManager = document.getElementById('companyManager');
    var selectedValue = companyManager.options[companyManager.selectedIndex].value;

    var submitButton = document.getElementById('submitBtn');

    var selectBox = $('#companyManager');  // 셀렉트박스 선택

    if (selectBox.find('option').length === 1) {
        // 첫 번째 옵션을 자동으로 선택
        selectBox.prop('selectedIndex', 0);

        // "표시할 호텔장이 없습니다."가 선택되면 제출 버튼 비활성화
        if (selectedValue === "표시할 호텔장이 없습니다.") {
            submitButton.disabled = true; // 제출 버튼 비활성화
            submitButton.title = "호텔장이 없으므로 등록할 수 없습니다."; // 툴팁 추가
        } else {
            submitButton.disabled = false; // 선택된 값이 유효하면 제출 버튼 활성화
            submitButton.title = ""; // 툴팁 제거
        }
    }
}

// 폼 제출 시 memberId 값 확인
document.querySelector('form').onsubmit = function(e) {
    var memberId = document.getElementById('memberId').value;

    // memberId가 비어 있으면 제출을 막음
    if (!memberId || memberId === "null") {
        alert("담당자가 선택되지 않았습니다.");
        e.preventDefault(); // 폼 제출 막기
    }
};

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

    // 이미지 검사 후 미리보기 함수 호출
    previewImage(event);

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

    if (confirm("정말 삭제하시겠습니까? (삭제하면 새 이미지를 업로드 해야합니다.)")) {
        // "예"를 선택했을 때 실행할 코드
        $.ajax({
            url: '/chief/company/image/delete/' + imageId,  // 기본 URL 포함
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
        deleteImage();
    } else {
        // "아니오"를 선택했을 때 실행할 코드
        console.log("이미지 삭제를 취소했습니다.");
    }

}