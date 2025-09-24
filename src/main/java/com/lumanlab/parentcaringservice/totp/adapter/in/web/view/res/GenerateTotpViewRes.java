package com.lumanlab.parentcaringservice.totp.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;

public record GenerateTotpViewRes(String image) {

    public GenerateTotpViewRes(GenerateTotpDto dto) {
        this(dto.qrUrl());
    }
}
