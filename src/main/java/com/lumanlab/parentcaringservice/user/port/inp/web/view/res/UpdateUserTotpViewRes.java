package com.lumanlab.parentcaringservice.user.port.inp.web.view.res;

import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;

public record UpdateUserTotpViewRes(String image) {

    public UpdateUserTotpViewRes(GenerateTotpDto dto) {
        this(dto.qrUrl());
    }
}
