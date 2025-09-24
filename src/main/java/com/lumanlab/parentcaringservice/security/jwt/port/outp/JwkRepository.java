package com.lumanlab.parentcaringservice.security.jwt.port.outp;


import com.lumanlab.parentcaringservice.security.jwt.domain.JwkData;
import com.lumanlab.parentcaringservice.security.jwt.domain.JwkMetadata;

import java.time.Duration;
import java.util.Set;

public interface JwkRepository {

    void saveCurrentKeyId(String keyId);

    String findCurrentKeyId();

    void saveKeyData(JwkData keyData, Duration expiration);

    JwkData findKeyData(String keyId);

    void deleteKeyData(String keyId);

    Set<String> findKeyIdsByPattern(String pattern);

    void cleanupExpiredKeys();

    void saveMetadata(JwkMetadata metadata);

    JwkMetadata findMetadata();
}
