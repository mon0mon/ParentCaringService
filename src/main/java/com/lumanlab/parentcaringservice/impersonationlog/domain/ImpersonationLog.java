package com.lumanlab.parentcaringservice.impersonationlog.domain;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/** 어드민 임퍼소네이션 로그 **/
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImpersonationLog {

    /** 생성일 **/
    @CreatedDate
    @Column(nullable = false)
    private final OffsetDateTime createdAt = OffsetDateTime.now();

    /** ID **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 임퍼소네이션을 요청한 어드민 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;

    /** 임퍼소네이션 대상 유저 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    /** 요청한 IP 주소 **/
    @Column(length = 45, nullable = false)
    private String ip;

    /** 임퍼소네이션 종류 **/
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private ImpersonationType type;

    /** 활동 내역 **/
    @Column(length = ACTION_DETAILS_LENGTH, nullable = false)
    private String actionDetails;

    public static final int ACTION_DETAILS_LENGTH = 3000;

    public ImpersonationLog(User admin, User targetUser, String ip, ImpersonationType type, String actionDetails) {
        this.admin = admin;
        this.targetUser = targetUser;
        this.ip = ip;
        this.type = type;
        this.actionDetails = actionDetails;

        validateImpersonationLogData();
    }

    /**
     * 임퍼소네이션 로그 데이터의 유효성을 검증하는 메서드
     * <p>
     * 다음 조건을 만족하지 않을 경우 IllegalArgumentException을 발생시킴:
     * - admin 필드가 null이면 안 됨
     * - targetUser 필드가 null이면 안 됨
     * - ip 필드가 null이면 안 됨
     * - type 필드가 null이면 안 됨
     * - actionDetails 필드가 null이면 안 됨
     * - admin 사용자의 최고 역할이 UserRole.ADMIN 미만이면 안 됨
     */
    private void validateImpersonationLogData() {
        if (admin == null) {
            throw new IllegalArgumentException("Admin user must not be null.");
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("Target user must not be null.");
        }

        if (!StringUtils.hasText(ip)) {
            throw new IllegalArgumentException("IP address must not be null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Impersonation type must not be null.");
        }

        if (!StringUtils.hasText(actionDetails)) {
            throw new IllegalArgumentException("Action details must not be null.");
        }

        if (UserRole.RANK_COMPARATOR.compare(admin.getHighestRole(), UserRole.ADMIN) < 0) {
            throw new IllegalArgumentException("Admin user must be a UserRole.ADMIN.");
        }
    }
}
