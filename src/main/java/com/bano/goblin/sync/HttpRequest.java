package com.bano.goblin.sync;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.io.FileInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

/**
 *
 * Created by Alexandre on 16/05/2017.
 */

public class HttpRequest {

    @StringDef(value = {GET_METHOD, PUT_METHOD, DELETE_METHOD,
            POST_METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MethodFlags {}

    public static final String GET_METHOD = "GET";
    public static final String PUT_METHOD = "PUT";
    public static final String POST_METHOD = "POST";
    public static final String DELETE_METHOD = "DELETE";

    private final String url;
    @MethodFlags
    private String method;
    final HashMap<String, String> headerMap;
    final HashMap<String, String> paramsMap;
    private String rawJson;
    private String id;
    private FileInputStream fileInputStream;
    private String fileFieldName;
    private String fileName;

    public HttpRequest(@NonNull String url, @NonNull @MethodFlags String method, HashMap<String, String> headerMap) {
        this.url = url;
        this.method = method;
        this.headerMap = headerMap;
        paramsMap = null;
    }

    public HttpRequest(@NonNull String url, @NonNull @MethodFlags String method,
                       HashMap<String, String> headerMap, HashMap<String, String> paramsMap, String fileFieldName, String fileName, FileInputStream inputStream) {
        this.url = url;
        this.method = method;
        this.headerMap = headerMap;
        this.paramsMap = paramsMap;
        this.fileInputStream = inputStream;
        this.fileFieldName = fileFieldName;
        this.fileName = fileName;
    }

    public HttpRequest(@NonNull String url, HashMap<String, String> headerMap) {
        this.url = url;
        this.headerMap = headerMap;
        paramsMap = null;
    }

    String getRawJson() {
        return rawJson;
    }

    public HttpRequest setRawJson(String rawJson) {
        this.rawJson = rawJson;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(@MethodFlags String method) {
        this.method = method;
    }

    public FileInputStream getFileInputStream() {
        return fileInputStream;
    }

    public String getFileFieldName() {
        return fileFieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public HttpRequest setId(String id){
        this.id = id;
        return this;
    }

    public String getUrl(){
        if(id != null)
            return url + "/" + id;

        return url;
    }
}
