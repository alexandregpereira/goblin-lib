package com.bano.goblin.http;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

/**
 * This class is passed as a parameter on {@link HttpService} class
 */

public abstract class HttpBaseRequest {
    @StringDef(value = {GET_METHOD, PUT_METHOD, DELETE_METHOD,
            POST_METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MethodFlags {}

    public static final String GET_METHOD = "GET";
    public static final String PUT_METHOD = "PUT";
    public static final String POST_METHOD = "POST";
    public static final String DELETE_METHOD = "DELETE";

    public interface TokenListener{
        Token getToken(String response);
    }

    protected final String url;
    @MethodFlags
    private String method;
    final HashMap<String, String> headerMap;
    private final TokenManager tokenManager;

    HttpBaseRequest(@NonNull String url, @NonNull @MethodFlags String method, HashMap<String, String> headerMap, TokenManager tokenManager) {
        this.url = url;
        this.method = method;
        this.headerMap = headerMap;
        this.tokenManager = tokenManager;
    }

    HttpBaseRequest(@NonNull String url, HashMap<String, String> headerMap, TokenManager tokenManager) {
        this.url = url;
        this.headerMap = headerMap;
        this.tokenManager = tokenManager;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(@MethodFlags String method) {
        this.method = method;
    }

    String getUrl(){
        return url;
    }

    Token getToken() {
        return tokenManager == null ? null : tokenManager.token;
    }

    void setToken(Token token) {
        if(tokenManager == null) return;
        this.tokenManager.token = token;
    }

    TokenListener getTokenListener() {
        return tokenManager == null ? null : tokenManager.tokenListener;
    }

    HttpRequest getTokenHttpRequest() {
        return tokenManager == null ? null : tokenManager.tokenHttpRequest;
    }
}
