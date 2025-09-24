package com.lumanlab.parentcaringservice.security.jwt.port.inp.web;

import com.lumanlab.parentcaringservice.security.jwt.application.service.JwkManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwkApi {

    private final JwkManager jwkManager;

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        Map<String, Object> jwkSet = jwkManager.getPublicJwkSet();
        return ResponseEntity.ok(jwkSet);
    }
}
