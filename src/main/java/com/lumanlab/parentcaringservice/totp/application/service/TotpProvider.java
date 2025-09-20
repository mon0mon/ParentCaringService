package com.lumanlab.parentcaringservice.totp.application.service;

import com.lumanlab.parentcaringservice.totp.application.service.dto.TotpProviderGenerateTotpDto;

public interface TotpProvider {
    TotpProviderGenerateTotpDto generateTotp(Long userId);

    boolean verifyTotp(String totpSecret, Integer totpCode);
}
