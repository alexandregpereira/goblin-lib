package com.bano.goblin.http;

import android.support.annotation.NonNull;

/**
 * Class manager the token in {@link HttpBaseRequest} class
 */

public class TokenManager {

    Token token;
    final HttpBaseRequest.TokenListener tokenListener;
    final HttpRequest tokenHttpRequest;

    private TokenManager(Token token, @NonNull HttpRequest tokenHttpRequest, @NonNull HttpBaseRequest.TokenListener tokenListener) {
        this.token = token;
        this.tokenListener = tokenListener;
        this.tokenHttpRequest = tokenHttpRequest;
    }

    public static TokenManager build(Token token, @NonNull HttpRequest tokenHttpRequest, @NonNull HttpBaseRequest.TokenListener tokenListener){
        return new TokenManager(token, tokenHttpRequest, tokenListener);
    }
}
