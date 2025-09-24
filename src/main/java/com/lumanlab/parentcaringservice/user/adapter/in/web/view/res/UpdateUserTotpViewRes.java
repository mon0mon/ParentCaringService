package com.lumanlab.parentcaringservice.user.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;

public record UpdateUserTotpViewRes(String image) {

    public UpdateUserTotpViewRes(GenerateTotpDto dto) {
        this(dto.qrUrl());
    }
}
