package com.lumanlab.parentcaringservice.refreshtoken.domain;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/** RefreshToken 엔티티 **/
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class RefreshToken {

    /** ID **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RefreshToken를 할당 받은 사용자 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** RefreshToken 해쉬 **/
    @Column(length = 100, nullable = false, unique = true)
    private String tokenHash;

    /** 이전 RefreshToken 참조 값 **/
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotated_from")
    private RefreshToken rotatedFrom;

    /** 사용자 IP **/
    @Column(length = 45)
    private String ip;

    /** 발급 시간 **/
    @Column(nullable = false)
    private OffsetDateTime issuedAt;

    /** 만료 시간 **/
    @Column(nullable = false)
    private OffsetDateTime expiredAt;

    /** 취소된 시간 **/
    private OffsetDateTime revokedAt;

    /** 사용자 클라이언트의 UserAgent **/
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserAgent userAgent;

    public RefreshToken(User user, String tokenHash, String ip, UserAgent userAgent, OffsetDateTime issuedAt,
                        OffsetDateTime expiredAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.ip = ip;
        this.userAgent = userAgent;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;

        // RefreshToken 데이터 유효성 검증
        validateRefreshTokenData();
    }

    /**
     * 현재 RefreshToken의 상태를 반환하는 메서드
     * <p>
     * RefreshToken이 취소되었거나 현재 시간을 기준으로 만료되었는지 여부를 확인 후,
     * 상태를 반환하는 로직을 포함
     *
     * @return RefreshToken의 현재 상태 (ACTIVE 또는 EXPIRED)
     */
    public RefreshTokenStatus getStatus() {
        if (revokedAt != null) {
            return RefreshTokenStatus.EXPIRED;
        }

        if (expiredAt.isBefore(OffsetDateTime.now())) {
            return RefreshTokenStatus.EXPIRED;
        }

        return RefreshTokenStatus.ACTIVE;
    }

    /**
     * 주어진 정보를 기반으로 기존 RefreshToken을 새롭게 갱신하는 메서드
     * 새로운 RefreshToken 객체를 생성하며, 기존 토큰은 무효화 상태로 변경됨
     *
     * @param tokenHash 새롭게 생성될 토큰의 해시값
     * @param issuedAt  새 토큰의 발급 시간
     * @param expiredAt 새 토큰의 만료 시간
     * @param ip        새 토큰 생성 요청을 발행한 사용자 IP
     * @param userAgent 새 토큰 생성 요청을 발행한 사용자 에이전트 정보
     * @return 갱신된 RefreshToken 객체
     */
    public RefreshToken rotate(String tokenHash, OffsetDateTime issuedAt, OffsetDateTime expiredAt, String ip,
                               UserAgent userAgent) {
        var renewedToken = new RefreshToken(user, tokenHash, ip, userAgent, issuedAt, expiredAt);

        renewedToken.rotatedFrom = this;
        revoke();

        return renewedToken;
    }


    /**
     * RefreshToken을 취소(무효화)하는 메서드
     * <p>
     * 이 메서드는 RefreshToken의 `revokedAt` 필드를 현재 시간으로 설정하여
     * 해당 RefreshToken이 더 이상 유효하지 않음을 나타냄
     * 단, 이미 취소된 상태(`revokedAt`이 null이 아님)인 경우 예외를 발생시킴
     *
     * @throws IllegalStateException RefreshToken이 이미 취소된 상태인 경우 발생
     */
    public void revoke() {
        if (revokedAt != null) {
            throw new IllegalStateException("Token has already been revoked");
        }

        this.revokedAt = OffsetDateTime.now();
    }

    /**
     * RefreshToken 데이터의 유효성을 검증하는 메서드
     * <p>
     * 이 메서드는 RefreshToken에 포함된 필수 데이터(user, tokenHash, issuedAt, expiredAt)의
     * 유효성을 확인하고, 데이터가 null 또는 유효하지 않을 경우 예외를 던짐
     * <p>
     * 검증 내용:
     * - user 필드가 null인지 확인
     * - tokenHash 필드가 null 또는 비어 있는지 확인
     * - issuedAt 필드가 null인지 확인
     * - expiredAt 필드가 null인지 확인
     * - expiredAt 필드가 issuedAt 필드보다 미래 시점인지 확인
     *
     * @throws IllegalArgumentException 각각의 검증 조건이 만족되지 않을 때 발생
     */
    private void validateRefreshTokenData() {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (!StringUtils.hasText(tokenHash)) {
            throw new IllegalArgumentException("Token hash must not be blank");
        }

        if (issuedAt == null) {
            throw new IllegalArgumentException("Issued at must not be null");
        }

        if (expiredAt == null) {
            throw new IllegalArgumentException("Expired at must not be null");
        }

        if (expiredAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("Expired at must be after issued at");
        }
    }
}
