package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.security.UserContext;
import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;
import com.lumanlab.parentcaringservice.user.application.service.UserAppService;
import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import com.lumanlab.parentcaringservice.user.port.inp.UpdateUser;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.*;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.res.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApi {

    private final UserContext userContext;
    private final QueryUser queryUser;
    private final UpdateUser updateUser;
    private final UserAppService userAppService;

    @GetMapping("/profile")
    public GetUserProfileViewRes getUserProfile() {
        Long userId = userContext.getCurrentUserIdOrThrow();

        return new GetUserProfileViewRes(queryUser.findById(userId));
    }

    @PostMapping("/register")
    public void registerUser(@RequestHeader("User-Agent") UserAgent userAgent, @RequestBody RegisterUserViewReq req) {
        userAppService.registerUser(req.email(), req.password(), userAgent);
    }

    @PostMapping("/login")
    public LoginUserViewRes loginUser(@RequestHeader("User-Agent") UserAgent userAgent,
                                      @RequestBody LoginUserViewReq req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        UserLoginDto dto = userAppService.loginUser(req.email(), req.password(), userAgent, ip);

        return new LoginUserViewRes(dto);
    }

    @PostMapping("/login/oauth2")
    public OAuth2LoginViewRes oAuth2LoginUser(@RequestHeader("User-Agent") UserAgent userAgent,
                                              @RequestBody OAuth2LoginViewReq req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        UserLoginDto dto = userAppService.oAuth2LoginUser(req.accessToken(), req.provider(), userAgent, ip);

        return new OAuth2LoginViewRes(dto);
    }

    @PostMapping("/totp")
    public UpdateUserTotpViewRes updateUserTotp() {
        Long userId = userContext.getCurrentUserIdOrThrow();

        GenerateTotpDto dto = userAppService.updateUserTotp(userId);

        return new UpdateUserTotpViewRes(dto);
    }

    @DeleteMapping("/totp")
    public void clearUserTotp() {
        Long userId = userContext.getCurrentUserIdOrThrow();

        updateUser.clearTotp(userId);
    }

    @PostMapping("/totp/verify")
    public VerifyUserTotpViewRes verifyUserTotp(@RequestHeader("User-Agent") UserAgent userAgent,
                                                @RequestBody VerifyUserTotpViewReq req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        UserLoginDto dto = userAppService.verifyUserTotp(req.nonce(), req.verificationCode(), userAgent, ip);

        return new VerifyUserTotpViewRes(dto);
    }

    @PostMapping("/oauth2-link/{provider}")
    public void linkOAuth2(@PathVariable("provider") OAuth2Provider provider, @RequestBody LinkOAuth2ViewReq req) {
        Long userId = userContext.getCurrentUserIdOrThrow();

        userAppService.linkOAuth2(userId, provider, req.oAuth2AccessToken());
    }

    @DeleteMapping("/oauth2-link/{provider}")
    public void unlinkOAuth2(@PathVariable("provider") OAuth2Provider provider) {
        Long userId = userContext.getCurrentUserIdOrThrow();

        userAppService.unlinkOAuth2(userId, provider);
    }
}
