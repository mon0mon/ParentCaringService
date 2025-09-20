package com.lumanlab.parentcaringservice.totp.application.service.dto;

public record TotpProviderGenerateTotpDto(String totpSecret, String qrUrl) {
}
