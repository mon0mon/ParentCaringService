package com.lumanlab.parentcaringservice.oauth2.port.outp;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OAuth2LinkRepository extends JpaRepository<OAuth2Link, Long> {

    @Query("SELECT oal FROM OAuth2Link oal WHERE oal.oAuth2Id = :oAuth2Id")
    Optional<OAuth2Link> findByOAuth2Id(String oAuth2Id);
}
