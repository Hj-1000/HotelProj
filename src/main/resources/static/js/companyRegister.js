//회원 선택 후 자동 회원아이디 추가
function updateMemberId() {
    var selectBox = document.getElementById('companyManager');
    var selectedOption = selectBox.options[selectBox.selectedIndex];

    // 선택된 option에서 memberId 값을 data-id 속성에서 가져오기
    var memberId = selectedOption.getAttribute('data-id');

    // 숨겨진 input에 memberId 값 설정
    document.getElementById('memberId').value = memberId;
}

// 페이지 로드 시 자동으로 셀렉트박스의 값이 하나일 경우 자동으로 input에 값 설정
$(document).ready(function() {
    console.log("들어왔니?");

    var selectBox = $('#companyManager');  // 셀렉트박스 선택

    // 셀렉트박스에 옵션이 하나만 있을 때
    if (selectBox.find('option').length === 1) {
        // 첫 번째 옵션을 자동으로 선택
        selectBox.prop('selectedIndex', 0);

        // 첫 번째 option에서 data-id 값을 찾아서 input에 설정
        var selectedOption = selectBox.find('option').first();  // 첫 번째 option 선택
        var memberId = selectedOption.attr('data-id');  // data-id 속성 값 가져오기
        console.log("selected memberId: " + memberId);  // 값 확인용 콘솔 출력
        $('#memberId').val(memberId);  // 숨겨진 input에 memberId 값 설정
    }
});
