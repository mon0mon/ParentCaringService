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

    /** 유저 역할 **/
    // TODO 추후에 1:N일 경우 @ElementCollection으로 변경할 것
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserRole role;

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

    public User(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;

        // MASTER 유저 역할인 경우에만 필수적으로 다단계 인증 활성화
        if (role == UserRole.MASTER) {
            this.mfaEnabled = true;
        } else {
            this.mfaEnabled = false;
        }

        // 사용자 정보가 유효한 값인지 확인
        validateUserData();
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
     * 사용자 데이터의 유효성을 검사
     *
     * 유효하지 않은 값이 포함된 경우 IllegalArgumentException을 발생
     *
     * 검사 조건:
     * - 이메일(email)이 비어있거나 공백일 경우 예외 발생.
     * - 비밀번호(password)가 비어있거나 공백일 경우 예외 발생.
     * - 사용자 역할(role)이 null일 경우 예외 발생.
     *
     * @throws IllegalArgumentException 이메일, 비밀번호가 유효하지 않거나, 역할이 null인 경우
     */
    private void validateUserData() {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email must not be blank");
        }

        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        if (role == null) {
            throw new IllegalArgumentException("User role must not be null");
        }
    }
}
