# 7️⃣ Spring Security & JWT — 학습 정리 & 과제

## 📌 핵심 개념 한 줄 정리

- **인증(Authentication)**: "너 누구야?" — 로그인
- **인가(Authorization)**: "너 이거 해도 돼?" — 권한 검사
- **Spring Security**: 경비 시스템 전체 (필터, 인증 매니저, 암호화 다 포함)
- **JWT**: 그 경비 시스템에서 쓰는 출입증 한 장 (토큰 자체)

---

## 1. 로그인 흐름 — 본인 언어로

```text
[Client]
   ↓ POST /login (username, password)
[LoginFilter] - 우리가 만든 커스텀 필터
   ↓ 토큰에 담아 검증 요청
[AuthenticationManager]
   ↓
[CustomUserDetailsService] - DB 조회
   ↓
[비밀번호 비교 - BCrypt]
   ↓ 성공
[LoginFilter.successfulAuthentication]
   ↓ JWT 발급
[Response Header: Authorization: Bearer xxx]
```

### 단계별로 다시 보기

**① Client → LoginFilter**
사용자가 `POST /login`으로 username/password를 보내면, 컨트롤러까지 가기 전에 **LoginFilter가 가로챈다**. 그래서 `LoginController`는 사실 텅 빈 껍데기 — Swagger 노출용으로만 존재함. 진짜 로직은 필터에 있음.

**② LoginFilter → AuthenticationManager**
필터가 username/password를 그대로 매니저에 못 넘긴다. Spring Security의 규칙이 **"검증은 토큰 형태로 주고받자"** 이기 때문. 그래서 `UsernamePasswordAuthenticationToken`에 담아서 `authenticationManager.authenticate(token)` 호출.

**③ AuthenticationManager → CustomUserDetailsService**
매니저는 우리 DB 구조를 모름. 그래서 "사용자 정보는 여기서 가져와" 라고 알려주는 통로가 필요한데, 그게 `UserDetailsService` 인터페이스. 우리는 `CustomUserDetailsService`로 구현해서 `userRepository.findByUsername()`으로 DB에서 user를 꺼낸 뒤 `CustomUserDetails`로 감싸 반환한다.
→ 왜 User를 그대로 안 쓰고 감싸지? **Spring Security는 자기 규격(`UserDetails`)만 인식**하기 때문. 일종의 어댑터 패턴.

**④ BCrypt 비밀번호 비교**
DB에 저장된 비번은 평문이 아니라 BCrypt로 해싱된 값. `DaoAuthenticationProvider`가 사용자가 입력한 평문을 같은 방식으로 해싱한 뒤 DB 해시와 매칭. BCrypt는 단방향이라 복호화는 불가능하지만, **같은 입력은 같은 해시**가 나오니까 비교는 가능.

**⑤ successfulAuthentication → JWT 발급**
검증 성공하면 `LoginFilter.successfulAuthentication()`이 실행됨. 여기서 `JWTUtil.createJwt(username, role, 만료ms)`로 토큰 만들고, **응답 헤더 `Authorization: Bearer xxx`** 에 담아서 클라이언트에 돌려준다.
→ `Bearer` 뒤의 공백은 HTTP 표준이라 절대 빼면 안 됨.
→ 실패 시엔 `unsuccessfulAuthentication()`에서 401 반환.

---

## 2. 인증된 API 요청 흐름 (보너스)

로그인 이후 JWT를 받았다면, 이후 모든 요청은 이 흐름을 탄다:

```text
[Client]
   ↓ GET /boards (Authorization: Bearer xxx)
[JWTFilter] - OncePerRequestFilter 상속, 매 요청마다 1회 실행
   ↓ 토큰 추출 → 만료 검증 → username/role 꺼냄
   ↓ User 객체를 즉석에서 만들어 SecurityContext에 등록 (DB 안 감!)
[BoardController]
   ↓ 정상 응답
```

**핵심 깨달음**: JWTFilter는 **DB에 가지 않는다.** 서명 검증을 통과했다는 것 = "우리 서버가 발급한 토큰이 맞다"는 증명. 매 요청마다 DB 가면 부담만 커지니까, 토큰 자체를 신뢰 증거로 쓰는 게 JWT의 핵심 철학이다.

**왜 JWTFilter를 LoginFilter `Before`에 끼우는가?**
일반 API 요청은 JWT가 있으니까 JWTFilter가 먼저 검증해야 하고, 로그인 요청은 JWT가 없으니까 JWTFilter는 통과시키고 LoginFilter가 처리해야 한다. 두 시나리오를 모두 만족시키려면 **JWTFilter가 더 앞**에 있어야 함.

---

## 3. 실습 결과

### 로그인 성공 → JWT 발급

![로그인 응답 헤더에 JWT 담겨옴](./화면%20캡처%202026-05-24%20225140.png)

응답 본문은 비어있고 **Response Headers의 `authorization`** 에 토큰이 담긴다. (처음엔 Body만 보고 "토큰 어디 있지?" 했음)

### Authorize에 토큰 넣고 게시글 생성

![JWT 인증 통과 후 게시글 생성 성공](./화면%20캡처%202026-05-24%20225929.png)

Swagger 우상단 `Authorize` 버튼에 토큰 붙여넣고 `POST /boards` 호출 → 200 OK.

---

## 4. Swagger 테스트하면서 만난 에러 (디버깅 기록)

깔끔하게 끝났으면 좋았는데 두 번 막혔다. 이 과정에서 오히려 Spring Security 동작이 더 잘 이해됨.

### 🚨 에러 1: `/login`에서 401 — `NonUniqueResultException`

**증상**: 비번 맞는데도 401.

**원인**: DB에 `boo` 유저가 **3명**이나 쌓여 있었음. `findByUsername`은 단일 User 반환이라 예외 발생 → 매니저 내부에서 던져진 예외 → `unsuccessfulAuthentication()` → 401.

왜 3명이나? `DataInitializer`에 버그가 있었음:
```java
if (userRepository.existsByUsername("test")) return; // ← "test" 체크
user.setUsername("boo");                              // ← 실제로는 "boo" 생성
```
체크 이름과 생성 이름이 달라서 재시작할 때마다 boo가 새로 생성됨. 게다가 username에 unique 제약도 없었음.

**해결**: ① 버그 수정 (체크/생성 이름 일치) ② User 엔티티에 `@Column(unique = true)` 추가 ③ MySQL에서 중복 row DELETE.

**배운 점**: **401이 무조건 "비번 틀림"이 아니다.** 인증 단계에서 어떤 예외든 던져지면 401로 떨어짐. 서버 로그를 봐야 진짜 원인이 보임.

### 🚨 에러 2: `/boards`에서 403 — JWT가 36초만에 만료

**증상**: 로그인 성공해서 토큰 넣었는데 403.

**원인**: 토큰 디코드해보니 `exp - iat = 36초`. `LoginFilter`에:
```java
String token = jwtUtil.createJwt(username, role, 60*60*10L); //36000, 10시간
```
`60*60*10 = 36,000` 인데 `expiredMs`는 **밀리초(ms) 단위**라서 실제론 **36초**. 주석은 "10시간"이라 적혀있지만 `1000`을 빠뜨린 단위 변환 실수.

**왜 401이 아니라 403?**: JJWT 0.12부터 만료된 토큰을 만나면 `parseSignedClaims()`가 `ExpiredJwtException`을 throw → 필터 밖으로 던져져서 Spring Security가 **"토큰은 있는데 유효하지 않음"** = 인가 거부 = **403**으로 처리.

**해결**: `60*60*10*1000L`로 수정 (= 36,000,000 ms = 10시간).

**배운 점**: **401(인증 실패) vs 403(인가 실패) 차이를 실전으로 체득.**
- 401: 너 누군지 모르겠어 → 로그인부터 다시
- 403: 너 누군진 알겠는데 이거 하면 안 돼 → 권한 부족 / 토큰 만료
