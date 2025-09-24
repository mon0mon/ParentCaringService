# 프로젝트 구조

## 패키지 구조

### 설명

- `impersonationlolg`, `oauth2`, `refresh`, `user` 도메인 단위 패키지
- `admin`, `me`, `token`, `totp` 외부에 노출되는 API 단위 패키지
- `advice`, `config`, `scheduler`, `security`, `logger` 스프링 환경 설정과 관련된 패키지
- `integration` 외부에 요청하는 기능들을 모아둔 패키지

### 패키지 구조

```text
.
src/
├── user/
│   ├── adapter/
│   │   └── in/
│   │       └── web/
│   │           └── UserApi.java
│   ├── port/
│   │   ├── inp/
│   │   │   ├── RegisterUser.java
│   │   │   ├── LoginUser.java
│   │   │   └── OAuth2LoginUser.java
│   │   └── outp/
│   │       └── UserRepository.java
│   └── domain/
│       ├── User.java
│       └── UserRole.java
├── security/
│   ├── filter/
│   │   └── JwtTokenFilter.java
│   ├── jwt/
│   │   └── JwtTokenProvider.java
│   └── oauth2/
│       ├── OAuth2AuthenticationProvider.java
│       └── GoogleOAuth2AuthenticationProvider.java
├── config/
│   └── SecurityConfig.java
└── integration/
    └── google/
        └── OAuth2GoogleProfileClient.java
```

## 아키텍쳐 구조 (Architecture Structure)

- 헥사고날 아키텍쳐로 도메인, 포트, 유즈케이스, 어뎁터 형식을 사용
- 하나의 도메인 아래 크게 4가지의 패키지로 구분

1. domain
    - 도메인의 핵심 정보를 담고 있는 엔티티 파일
    - 엔티티 내부에는 비즈니스 로직을 함수로 가지고 있음
2. port
    - 엔티티를 사용할 수 있도록 외부로 노출하는 유즈케이스
3. application
    - 유즈케이스를 실제로 구현한 서비스 파일
    - 이외에도 서비스 파일에서 사용하는 DTO나, 기타 로직 파일들을 위치
4. adapter
    - 외부로부터 전달받은 요청을 처리해주는 API 파일들을 위치
