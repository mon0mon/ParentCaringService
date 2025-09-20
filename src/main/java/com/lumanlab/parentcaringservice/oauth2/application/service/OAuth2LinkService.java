package com.lumanlab.parentcaringservice.oauth2.application.service;

import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.domain.OAuth2Provider;
import com.lumanlab.parentcaringservice.oauth2.port.inp.QueryOAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.port.inp.UpdateOAuth2Link;
import com.lumanlab.parentcaringservice.oauth2.port.outp.OAuth2LinkRepository;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2LinkService implements QueryOAuth2Link, UpdateOAuth2Link {

    private final OAuth2LinkRepository oAuth2LinkRepository;
    private final QueryUser queryUser;


    @Override
    public OAuth2Link findByOAuth2IdOrThrow(String oAuth2Id) {
        return oAuth2LinkRepository.findByOAuth2Id(oAuth2Id).orElseThrow();
    }

    @Override
    public void register(Long userId, OAuth2Provider provider, String oAuth2Id) {
        User user = queryUser.findById(userId);
        OAuth2Link oAuth2Link = oAuth2LinkRepository.save(new OAuth2Link(user, provider, oAuth2Id));

        user.addOAuth2Link(oAuth2Link);
    }

    @Override
    public void delete(Long userId, OAuth2Provider provider) {
        User user = queryUser.findById(userId);
        OAuth2Link oAuth2Link = user.getOAuth2Link(provider);

        user.removeOAuth2Link(oAuth2Link);
        oAuth2LinkRepository.delete(oAuth2Link);
    }
}
