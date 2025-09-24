package com.lumanlab.parentcaringservice.me.adapter.in.web.view.res;

import com.lumanlab.parentcaringservice.refreshtoken.domain.RefreshToken;

import java.util.List;

public record QuerySessionsViewRes(List<QuerySessionsData> sessions) {

    public static QuerySessionsViewRes create(List<RefreshToken> refreshTokens) {
        return new QuerySessionsViewRes(refreshTokens.stream().map(QuerySessionsData::new).toList());
    }
}
