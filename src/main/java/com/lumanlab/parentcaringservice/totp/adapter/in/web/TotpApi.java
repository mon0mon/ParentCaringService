package com.lumanlab.parentcaringservice.totp.adapter.in.web;

import com.lumanlab.parentcaringservice.totp.adapter.in.web.view.req.GenerateTotpViewReq;
import com.lumanlab.parentcaringservice.totp.adapter.in.web.view.res.GenerateTotpViewRes;
import com.lumanlab.parentcaringservice.totp.application.service.TotpService;
import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/totp")
public class TotpApi {

    private final TotpService totpService;

    @PostMapping
    public GenerateTotpViewRes generateTotp(@RequestBody GenerateTotpViewReq req) {
        GenerateTotpDto dto = totpService.generateTotp(req.nonce());

        return new GenerateTotpViewRes(dto);
    }
}
