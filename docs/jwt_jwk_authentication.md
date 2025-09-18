## JWT Authentication

### 로그인 시나리오

1. 사용자 로그인 요청 (이메일-PWD, OAuth2)
2. 사용자 정보 확인 후, `AccessToken`, `RefreshToken` 발급
    - `AccessToken`: 15분간 유지
    - `RefreshToken`: 14일간 유지
        - 발급 직후, `refresh_token`테이블에 저장
3. 발급 받은 `AccessToken`으로 사용자가 서버에 요청
4. `AccessToken`의 expiredAt, kid를 이용해서 JWT 유효성 검증
    - 만료일이 지난 경우, `401 Unauthorized` 응답 리턴
    - 유효하지 않은 kid의 경우, `401 Unauthorized` 응답 리턴

### `Access Token` 만료 시나리오

1. `AccessToken`이 만료된 경우, `401 Unauthorized` 응답 리턴
2. 저장된 `RefreshToken`으로 신규 `AccessToken` 발급 요청
3. `RefreshToken`이 유효한 경우, 신규 `AccessToken`과 `RefreshToken`으로 재발급
4. `RefreshToken`이 유효하지 않은 경우, `401 Unauthorized` 응답 리턴
