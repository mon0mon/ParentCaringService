package com.lumanlab.parentcaringservice.refreshtoken.port.outp;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    List<RefreshToken> findAllByUser(User user);

    /**
     * ACTIVE 상태의 리프레시 토큰 조회
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
            "AND rt.revokedAt IS NULL AND rt.expiredAt > :currentTime")
    List<RefreshToken> findActiveTokensByUser(User user, OffsetDateTime currentTime);

    /**
     * EXPIRED 상태의 리프레시 토큰 조회
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
            "AND (rt.revokedAt IS NOT NULL OR rt.expiredAt <= :currentTime)")
    List<RefreshToken> findExpiredTokensByUser(User user, OffsetDateTime currentTime);
}
