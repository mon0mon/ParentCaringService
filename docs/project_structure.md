# 프로젝트 구조

## 아키텍쳐 구조 (Architecture Structure)

## 설명
- 헥사고날 (Hexagonal) 방식을 사용
- `User`, `Security`, `Integration`와 같이 도메인 단위로 구분
- 외부 관심사의 경우, 내부의 인터페이스를 통해 최소한만 노출

## 패키지 구조
```text
.
└── src/
    ├── user/
    │   ├── port/
    │   │   ├── inp/
    │   │   │   ├── web/
    │   │   │   │   └── UserApi.java
    │   │   │   └── usecase/
    │   │   │       ├── RegisterUser.java
    │   │   │       ├── LoginUser.java
    │   │   │       └── OAuth2LoginUser.java
    │   │   └── outp/
    │   │       └── UserRepository.java
    │   └── domain/
    │       ├── User.java
    │       └── UserRole.java
    ├── security/
    │   ├── jwt/
    │   │   ├── JwtTokenFilter.java
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
