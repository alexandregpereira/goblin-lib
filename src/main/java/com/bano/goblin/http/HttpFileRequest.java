package com.bano.goblin.http;

import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.util.HashMap;

/**
 * This class is passed as a parameter on {@link HttpService} class
 */

public class HttpFileRequest extends HttpBaseRequest{

    final HashMap<String, String> paramsMap;
    private FileInputStream fileInputStream;
    private String fileFieldName;
    private String fileName;

    public HttpFileRequest(@NonNull String url, @NonNull @MethodFlags String method,
                           HashMap<String, String> headerMap, HashMap<String, String> paramsMap, String fileFieldName, String fileName, FileInputStream inputStream, TokenManager tokenManager) {
        super(url, method, headerMap, tokenManager);
        this.paramsMap = paramsMap;
        this.fileInputStream = inputStream;
        this.fileFieldName = fileFieldName;
        this.fileName = fileName;
    }

    FileInputStream getFileInputStream() {
        return fileInputStream;
    }

    String getFileFieldName() {
        return fileFieldName;
    }

    String getFileName() {
        return fileName;
    }
}
