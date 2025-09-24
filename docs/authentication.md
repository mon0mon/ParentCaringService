# 로그인 인증

## 로그인 시나리오

1. 사용자 로그인 요청 (이메일-PWD, OAuth2)
2. 로그인 가능한지 확인
3. 사용자 정보 확인 후, `AccessToken`, `RefreshToken` 발급
    - `AccessToken`: 15분간 유지
    - `RefreshToken`: 1일간 유지
        - 발급 직후, `refresh_token`테이블에 저장
4. 발급 받은 `AccessToken`으로 사용자가 서버에 요청

### 다이어그램

```mermaid
sequenceDiagram
    actor u as User
    participant s as Server
    u ->> s: 로그인 요청
    note right of u: POST /api/users/login<br>POST /api/users/oauth2/login

    opt 사용자 역할과, UserAgent가 일치하지 않는 경우
        s -->> u: 400 Bad Request
    end

    opt 사용자 인증정보가 일치하지 않는 경우
        s -->> u: 401 Unauthorized
    end

    opt 사용자 MFA 추가가 필요한 경우
        s -->> u: 428 Precondition Required
    end

    opt 사용자 MFA 설정이 활성화된 경우
        note right of s: 자세한 구현은 `TOTP` 로그인 시나리오 참고
        s -->> u: 428 Precondition Required
    end

    opt 사용자가 활성화 상태가 아닌 경우
        s -->> u: 409 Conflict
    end

    s ->> s: RefreshToken 저장
    s -->> u: AccessToken, RefreshToken 발급
```

## `Access Token` 만료 시나리오

1. `AccessToken`이 만료된 경우, `401 Unauthorized` 응답 리턴
2. 저장된 `RefreshToken`으로 신규 `AccessToken` 발급 요청
3. `RefreshToken`이 유효한 경우, 신규 `AccessToken`과 `RefreshToken`으로 재발급
4. `RefreshToken`이 유효하지 않은 경우, `401 Unauthorized` 응답 리턴

### 다이어그램

```mermaid
sequenceDiagram
    actor u as User
    participant s as Server
    u ->> s: API 요청
    s -->> u: 401 Unauthorized
    u ->> s: RefreshToken으로 AccessToken 갱신
    note right of u: POST /api/token/refresh
    alt 토큰이 유효하지 않은 경우
        s -->> u: 401 Unauthorized
    else
        s -->> u: 신규로 AccessToken, RefreshToken 발급
    end
```

## `TOTP` 로그인 시나리오

1. 사용자 로그인 요청 (이메일-PWD, OAuth2)
2. MFA 설정이 `True`인 경우, 유저 Id를 Redis에 저장 후 `428 Precondition Required` 리턴
3. TOTP 인증, 요청 API에 인증 요청
4. 인증이 완료된 경우, AccessToken, RefreshToken 발급

### 다이어그램

```mermaid
sequenceDiagram
    actor u as User
    participant s as Server
    u ->> s: 로그인 요청
    note right of u: POST /api/users/login<br>POST /api/users/oauth2/login
    s ->> s: 유저 MFA 설정 확인
    s ->> s: 유저의 ID를 담은 redis에 임시 저장<br>TTL 5분으로 설정
    s -->> u: 428 Precondition Required
    u ->> s: TOTP 인증
    note right of u: POST /api/users/totp/verify

    alt 인증이 실패하는 경우
        s -->> u: 401 Unauthorized
    else 인증 성공하는 경우
        s ->> s: RefreshToken 저장
        s -->> u: AccessToken, RefreshToken 발급
        note right of s: AccessToken : 15분간 유지<br>RefreshToken : 1일간 유지
    end
```
