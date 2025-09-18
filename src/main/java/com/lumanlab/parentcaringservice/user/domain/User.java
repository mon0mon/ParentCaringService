package com.lumanlab.parentcaringservice.user.domain;

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
     * 다단계 인증 활성화 여부를 업데이트
     *
     * @param mfaEnabled 다단계 인증 활성화 여부. true면 활성화, false면 비활성화.
     */
    public void updateMfaEnabled(Boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
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
}
