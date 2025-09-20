package com.lumanlab.parentcaringservice.user.domain;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** 사용자 **/
@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class User {

    /** ID **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이메일 (변경 불가) **/
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    /** 비밀번호 **/
    @Column(length = 100, nullable = false)
    private String password;

    /** 유저 상태 **/
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @ColumnDefault("'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

    /** 유저 역할 목록 **/
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Set<UserRole> roles;

    /** 사용자 OAuth2 연동 관리 **/
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<OAuth2Link> oAuth2Links = new HashSet<>();

    /** TOTP 비밀키 **/
    @Column(length = 150)
    private String totpSecret;

    /** 다단계 인증 여부 **/
    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean mfaEnabled = false;

    /** 생성일 **/
    @CreatedDate
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** 최종 수정일 **/
    @LastModifiedDate
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public User(String email, String password, Collection<UserRole> roles) {
        // 사용자 정보가 유효한 값인지 확인
        validateUserData(email, password, roles);

        this.email = email;
        this.password = password;
        this.roles = new HashSet<>(roles);
    }

    public User(String email, String password, Collection<UserRole> roles, String totpSecret) {
        // 사용자 정보가 유효한 값인지 확인
        validateUserData(email, password, roles);

        this.email = email;
        this.password = password;
        this.roles = new HashSet<>(roles);

        // 주어진 TotpSecret이 유효한 경우에만, 정보를 등록
        if (isTotpSecretValid(totpSecret)) {
            this.mfaEnabled = true;
            this.totpSecret = totpSecret;
        }
    }

    /**
     * 사용자 비밀번호를 업데이트
     *
     * @param password 새로운 비밀번호. Null이거나 공백일 경우 예외가 발생
     * @throws IllegalArgumentException 비밀번호가 Null이거나 공백인 경우
     */
    public void updatePassword(String password) {
        // 주어진 비밀번호가 Null 이거나, 공백일 경우 예외 발생
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("password must not be blank");
        }

        this.password = password;
    }

    /**
     * 유저 상태를 변경
     *
     * @param status 변경하려는 유저 상태
     * @throws IllegalStateException 현재 유저 상태가 WITHDRAWN(회원 탈퇴 상태)일 경우 상태 변경 불가
     */
    public void updateStatus(UserStatus status) {
        // 이미 삭제된 유저는 상태 변경 불가
        if (this.status == UserStatus.WITHDRAWN) {
            throw new IllegalStateException("cannot change status of deleted user");
        }

        this.status = status;
    }

    /**
     * 사용자의 상태를 탈퇴 상태(WITHDRAWN)로 업데이트
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }

    /**
     * 사용자의 TOTP 비밀키를 업데이트
     *
     * @param totpSecret 새로운 TOTP 비밀키. Null이거나 공백일 경우 예외가 발생
     * @throws IllegalArgumentException totpSecret이 Null이거나 공백인 경우
     */
    public void updateTotpSecret(String totpSecret) {
        // 주어진 totpSecret이 Null 이거나, 공백일 경우 예외 발생
        if (!StringUtils.hasText(totpSecret)) {
            throw new IllegalArgumentException("totpSecret must not be blank");
        }

        this.totpSecret = totpSecret;
        this.mfaEnabled = true;
    }

    /**
     * 사용자의 TOTP 비밀키와 다단계 인증 활성화 상태를 초기화함
     * <p>
     * 이 메서드는 사용자의 TOTP 비밀키를 null로 설정하고 다단계 인증을 비활성화함
     * 그러나 사용자가 SuperUser 역할을 가지고 있다면 예외를 발생시킴
     *
     * @throws IllegalStateException 사용자가 SuperUser인 경우
     */
    public void clearTotpSecret() {
        // 만약 사용자가 SuperUser라면, 예외 발생
        boolean isSuperUser = roles.stream().anyMatch(UserRole::isSuperUser);

        if (isSuperUser) {
            throw new IllegalStateException("cannot clear totp secret of super user");
        }

        this.totpSecret = null;
        this.mfaEnabled = false;
    }

    /**
     * SuperUser인지 확인하고, 다단계 인증(MFA)이 초기화되지 않은 상태인지 판단.
     *
     * @return true인 경우 SuperUser이며 MFA가 아직 초기화되지 않은 상태
     */
    public boolean shouldInitializeMfa() {
        boolean isSuperUser = roles.stream().anyMatch(UserRole::isSuperUser);
        // MFA가 초기화 된 상태인지 확인
        boolean alreadyMfaInitialized = mfaEnabled == true && StringUtils.hasText(totpSecret);

        return isSuperUser && !alreadyMfaInitialized;
    }

    /**
     * 사용자에게 OAuth2 연동을 추가하는 메서드
     *
     * @param oAuth2Link 추가하려는 OAuth2 연동 객체
     */
    public void addOAuth2Link(OAuth2Link oAuth2Link) {
        oAuth2Links.add(oAuth2Link);
    }

    /**
     * 사용자와 연동된 특정 OAuth2 연동 객체를 제거함
     *
     * @param oAuth2Link 제거하려는 OAuth2 연동 객체
     */
    public void removeOAuth2Link(OAuth2Link oAuth2Link) {
        oAuth2Links.remove(oAuth2Link);
    }

    /**
     * 사용자 데이터를 검증.
     *
     * @param email    사용자 이메일. Null이거나 공백일 경우 예외 발생
     * @param password 사용자 비밀번호. Null이거나 공백일 경우 예외 발생
     * @param roles    사용자 역할 목록. Null이거나 비어있을 경우 예외 발생
     * @throws IllegalArgumentException 이메일 또는 비밀번호가 Null이거나 공백이거나, 역할 목록이 Null이거나 비어있는 경우
     */
    private void validateUserData(String email, String password, Collection<UserRole> roles) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User role must not be null or empty");
        }
    }

    private boolean isTotpSecretValid(String totpSecret) {
        return StringUtils.hasText(totpSecret);
    }
}
