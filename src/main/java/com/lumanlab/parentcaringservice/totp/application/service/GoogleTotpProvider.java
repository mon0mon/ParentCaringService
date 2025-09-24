package com.lumanlab.parentcaringservice.totp.application.service;

import com.lumanlab.parentcaringservice.totp.application.service.dto.TotpProviderGenerateTotpDto;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Component;

@Component
public class GoogleTotpProvider implements TotpProvider {

    private final GoogleAuthenticator googleAuthenticator;

    public GoogleTotpProvider() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    @Override
    public TotpProviderGenerateTotpDto generateTotp(Long userId) {
        GoogleAuthenticatorKey credentials = googleAuthenticator.createCredentials();
        String secretKey = credentials.getKey();
        String qrUrl =
                GoogleAuthenticatorQRGenerator.getOtpAuthURL("ParentCaringService", userId.toString(), credentials);

        return new TotpProviderGenerateTotpDto(secretKey, qrUrl);
    }

    @Override
    public boolean verifyTotp(String totpSecret, Integer verificationCode) {
        return googleAuthenticator.authorize(totpSecret, verificationCode);
    }
}
