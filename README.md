<h1>🏨호텔/룸서비스 예약 사이트 팀 프로젝트</h1>
우리인재개발원 2조 NTT
프로젝트 기간: 2025.01.09 ~ 2025.03.18

## 프로젝트효과2

## 프로젝트 효과
- 실시간 예약 현황 확인하여 중복 예약 방지
- 객실 이용 현황 쉽게 파악
- 룸서비스 운영 최적화
- 디지털 방식으로 추적하여 음식 주문 누락 방지
- 호텔 프런트 업무 부담 줄이고 인건비 절감
- 매출 효과 파악
- 데이터 기반 의사 결정 가능

## 명명규칙
1) 명명규칙의 통일성을 위해 아래의 규칙을 따르되 Database 장 [한정호] 혹은 팀장인 [천현종]의 의견을 따른다.
2) N 이란 해당 테이블 혹은 기능의 이름이며 객체명과 메서드 명에서는 소문자로 시작한다
3) 기본적으로 단어의 풀스펠링을 적는다. 단, 흔히 쓰는 약어에 한해서는 축약명으로 작성
   - 예) 등록일 - regDate, 수정일 - modDate, Data Transfer Object - DTO 등...

### 테이블명
- 관리자 - admin
- 호텔어드민 - chief
- 지사어드민 - manager
- 유저 - user
- 매장(음식점)
- 메뉴
- 메뉴카테고리
- 방
- 주문
- 결제

### 매핑 명 규칙
- GetMapping : N + Form
- PostMapping : N + Proc

### 메소드 명 규칙
- 메소드명의 첫 글자는 소문자로 시작
- ex) ItemRegister(x) -> itemRegister(o)

### 변수명 규칙
- Pk : N + Id
- 아이디 : N + Email
- 비밀번호 : N + Password
- 연락처 : N + Tel
- 이름 : N + Name
- 등록일 : regiDate
- 수정일 : modDate
- 삭제유무 : DeleteYn
- 상대 : N + State


<a target="https://www.notion.so/17548799325c804b8284c4686e395148">
  <h3>📃notion 바로가기</h3>
</a>

<a target="https://docs.google.com/spreadsheets/d/1r3FvPDK-OPlXQAsxBs167bzD5p96dRwbL9wv-OHvfYE/edit?gid=1115838130#gid=1115838130">
  <h3>📆프로젝트 플래너 바로가기</h3>
</a>
