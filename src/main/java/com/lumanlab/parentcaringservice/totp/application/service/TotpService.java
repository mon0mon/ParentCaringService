package com.lumanlab.parentcaringservice.totp.application.service;

import com.lumanlab.parentcaringservice.totp.application.service.dto.GenerateTotpDto;
import com.lumanlab.parentcaringservice.totp.application.service.dto.TotpProviderGenerateTotpDto;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TotpService {

    private final QueryUser queryUser;
    private final NonceService nonceService;
    private final TotpProvider totpProvider;

    public GenerateTotpDto generateTotp(String nonce) {
        // nonce로 UserId 조회
        Long userId = nonceService.getUserIdByNonce(nonce);
        User user = queryUser.findById(userId);

        // TOTP Secret과 QR Image 생성
        TotpProviderGenerateTotpDto totpDto = totpProvider.generateTotp(userId);

        // User totpSecret 수정
        user.updateTotpSecret(totpDto.totpSecret());

        return new GenerateTotpDto(totpDto.qrUrl());
    }

    public GenerateTotpDto generateTotp(Long userId) {
        User user = queryUser.findById(userId);

        // TOTP Secret과 QR Image 생성
        TotpProviderGenerateTotpDto totpDto = totpProvider.generateTotp(userId);

        // User totpSecret 수정
        user.updateTotpSecret(totpDto.totpSecret());

        return new GenerateTotpDto(totpDto.qrUrl());
    }
}
