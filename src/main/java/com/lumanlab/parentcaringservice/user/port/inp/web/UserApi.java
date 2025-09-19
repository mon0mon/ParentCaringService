package com.lumanlab.parentcaringservice.user.port.inp.web;

import com.lumanlab.parentcaringservice.security.UserContext;
import com.lumanlab.parentcaringservice.user.application.service.UserAppService;
import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import com.lumanlab.parentcaringservice.user.port.inp.UpdateUser;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.LoginUserViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.RegisterUserViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.req.UpdateUserTotpViewReq;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.res.GetUserProfileViewRes;
import com.lumanlab.parentcaringservice.user.port.inp.web.view.res.LoginUserViewRes;
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
        Long userId = userContext.getCurrentUserId().orElseThrow();

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

    @PostMapping("/totp")
    public void updateUserTotp(@RequestBody UpdateUserTotpViewReq req) {
        Long userId = userContext.getCurrentUserId().orElseThrow();

        userAppService.updateUserTotp(userId, req.totpSecret());
    }

    @DeleteMapping("/totp")
    public void clearUserTotp() {
        Long userId = userContext.getCurrentUserId().orElseThrow();

        updateUser.clearTotp(userId);
    }
}
