package com.lumanlab.parentcaringservice.oauth2.domain;

import com.lumanlab.parentcaringservice.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/** OAuth2 연동 정보 **/
@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uc_oauth2link_user_id", columnNames = {"user_id", "provider"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OAuth2Link {

    /** ID **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** OAuth2 제공자 **/
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OAuth2Provider provider;

    /** OAuth2에서 제공한 ID **/
    @Column(length = 100, nullable = false, unique = true)
    private String oAuth2Id;

    public OAuth2Link(User user, OAuth2Provider provider, String oAuth2Id) {
        validateOAuth2LinkData(provider, oAuth2Id);

        this.user = user;
        this.provider = provider;
        this.oAuth2Id = oAuth2Id;
    }

    private void validateOAuth2LinkData(OAuth2Provider provider, String oAuth2Id) {
        if (provider == null) {
            throw new IllegalArgumentException("OAuth2 provider must not be null");
        }

        if (!StringUtils.hasText(oAuth2Id)) {
            throw new IllegalArgumentException("OAuth2 id must not be blank");
        }
    }
}
