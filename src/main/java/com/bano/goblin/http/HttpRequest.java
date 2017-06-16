package com.bano.goblin.http;

import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * This class is passed as a parameter on {@link HttpService} class
 */

public class HttpRequest extends HttpBaseRequest {

    private String rawJson;
    private String id;

    public HttpRequest(@NonNull String url, @NonNull @MethodFlags String method, HashMap<String, String> headerMap, TokenManager tokenManager) {
        super(url, method, headerMap, tokenManager);
    }

    public HttpRequest(@NonNull String url, HashMap<String, String> headerMap, TokenManager tokenManager) {
        super(url, headerMap, tokenManager);
    }

    String getRawJson() {
        return rawJson;
    }

    public HttpRequest setRawJson(String rawJson) {
        this.rawJson = rawJson;
        return this;
    }

    public HttpRequest setId(String id){
        this.id = id;
        return this;
    }

    @Override
    public String getUrl(){
        if(id != null)
            return url + "/" + id;

        return url;
    }
}
