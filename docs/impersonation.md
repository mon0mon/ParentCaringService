# 어드민 임퍼소네이션

## 기능 정의

- `ADMIN`, `MASTER` 권한을 가진 유저가, 다른 유저로 인증할 수 있도록 하는 기능
- 임퍼소네이션 기능이 사용 된 경우, 반드시 로그를 남겨야 함

## 다이어그램

```mermaid
sequenceDiagram
    actor a as Admin
    participant s as Server
    
    a ->> s: 임퍼소네이션 요청
    s ->> s: 어드민 권한을 가지고 있는 지 확인
    s ->> s: 타깃 유저에 해당하는 인증 토큰 발급
    note right of s: JWT Claim에 impersonatorId 추가
    s -->> a: 인증 토큰 전달

    a ->> s: API 요청
    s ->> s: JWT Claim에 impersonatorId가 존재하는 경우<br>임퍼소네이션 로그 추가
```

## 코드

```java
// com.lumanlab.parentcaringservice.security.filter.JwtAuthenticationFilter

// 어드민 임퍼소네이션인 경우, 저장
if (principal.isImpersonation()) {
    String ip = request.getRemoteAddr();
    String actionDetails = actionDetailsExtractor.extractActionDetails(request);

    updateImpersonationLog.register(
            principal.impersonatorId(), principal.id(), ip, ImpersonationType.ACTION, actionDetails
    );
}
```

- JWT 인증 필터에서, 임퍼소네이션 여부 체크
