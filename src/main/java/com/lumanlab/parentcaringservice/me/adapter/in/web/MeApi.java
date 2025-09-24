package com.lumanlab.parentcaringservice.me.adapter.in.web;

import com.lumanlab.parentcaringservice.me.adapter.in.web.view.res.QuerySessionsViewRes;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshTokenStatus;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.QueryRefreshToken;
import com.lumanlab.parentcaringservice.refreshtoken.port.inp.UpdateRefreshToken;
import com.lumanlab.parentcaringservice.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeApi {

    private final UserContext userContext;
    private final QueryRefreshToken queryRefreshToken;
    private final UpdateRefreshToken updateRefreshToken;

    @GetMapping("/sessions")
    public QuerySessionsViewRes querySessions() {
        Long userId = userContext.getCurrentUserIdOrThrow();

        // 현재 유효한 상태의 RefreshToken만 조회
        List<RefreshToken> sessions =
                queryRefreshToken.findByUserAndStatus(userId, RefreshTokenStatus.ACTIVE, OffsetDateTime.now());

        return QuerySessionsViewRes.create(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public void revokeSession(@PathVariable("sessionId") Long sessionId) {
        Long userId = userContext.getCurrentUserIdOrThrow();

        updateRefreshToken.revoke(userId, sessionId);
    }
}
