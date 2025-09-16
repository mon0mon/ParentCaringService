```mermaid
---
title: OAuth2 로그인 (OAuth2 Login)
---
sequenceDiagram
    actor c as Client
    participant s as Server
    participant o as OAuth2 Provider
    c ->> o : OAuth2 제공자 로그인
    o -->> c : OAuth2 AccessToken 제공
    c ->> s : OAuth2 로그인 요청
    s ->> o : 유효한 AccessToken 인지 검증
    o -->> s : 검증 결과 리턴
    s ->> o : 유저 프로필 조회 요청
    o -->> s : 유저 프로필 결과 리턴
    s -->> c : OAuth2 로그인 결과 리턴
```
