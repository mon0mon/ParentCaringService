package com.lumanlab.parentcaringservice.token.port.inp.web;

import com.lumanlab.parentcaringservice.token.application.service.TokenAppService;
import com.lumanlab.parentcaringservice.token.application.service.dto.RefreshAccessTokenDto;
import com.lumanlab.parentcaringservice.token.port.inp.web.view.req.RefreshAccessTokenViewReq;
import com.lumanlab.parentcaringservice.token.port.inp.web.view.res.RefreshAccessTokenViewRes;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenApi {

    private final TokenAppService tokenAppService;

    @PostMapping("/refresh")
    public RefreshAccessTokenViewRes refreshAccessToken(@RequestHeader("User-Agent") UserAgent userAgent,
                                                        @RequestBody RefreshAccessTokenViewReq req,
                                                        HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        RefreshAccessTokenDto dto = tokenAppService.refreshAccessToken(req.refreshToken(), userAgent, ip);

        return new RefreshAccessTokenViewRes(dto);
    }
}
