# TOTP

## 어드민 최초 TOTP 등록 시나리오

```mermaid
sequenceDiagram
    actor a as Admin
    participant s as Server
    a ->> s: TOTP 등록 요청
    note right of a: `POST /api/totp`
    s -->> s: 전달 받은 Nonce로 UserId 조회
    s -->> s: TOTP Secret과 QR 이미지 생성
    s -->> a: QR 이미지 리턴
```
