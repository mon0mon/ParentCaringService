package com.lumanlab.parentcaringservice.admin.port.inp.web;

import com.lumanlab.parentcaringservice.admin.application.service.AdminAppService;
import com.lumanlab.parentcaringservice.admin.port.inp.web.view.res.ImpersonateUserViewRes;
import com.lumanlab.parentcaringservice.security.UserContext;
import com.lumanlab.parentcaringservice.user.application.service.dto.UserLoginDto;
import com.lumanlab.parentcaringservice.user.domain.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminApi {

    private final UserContext userContext;
    private final AdminAppService adminAppService;

    @PostMapping("/impersonate/{impersonateUserId}")
    @PreAuthorize(value = "hasAnyRole('ADMIN', 'MASTER')")
    public ImpersonateUserViewRes impersonateUser(@RequestHeader("User-Agent") UserAgent userAgent,
                                                  @PathVariable Long impersonateUserId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Long adminUserId = userContext.getCurrentUserIdOrThrow();

        UserLoginDto dto = adminAppService.impersonateUser(adminUserId, impersonateUserId, ip, userAgent);

        return new ImpersonateUserViewRes(dto);
    }
}
