package com.bano.goblin.http;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpService {

    public static final int SUCCESS_RESPONSE = 200;
    private static final int NO_CONTENT_RESPONSE = 204;
    private static final int NO_UPDATE_RESPONSE = 304;
    private static final int UNKNOWN_ERROR_RESPONSE = -1;
    private static final int INVALID_RETURN_RESPONSE = -2;

    private static final int TIME_OUT = 1000 * 30;
    private static final String TAG = "HttpService";

    public static String consume(@NonNull HttpRequest httpRequest){
        try {
            return consumeWithException(httpRequest);
        } catch (UnknownHostException e){
            e.printStackTrace();
            Log.e(TAG, "Http get error " + e.getMessage());
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static String consumeWithException(@NonNull HttpRequest httpRequest) throws IOException  {
        long init = System.currentTimeMillis();
        if(!checkToken(httpRequest)) return null;
        HttpURLConnection conn = buildConnection(httpRequest);
        Log.d(TAG, httpRequest.getUrl() + ": response: " + conn.getResponseCode());
        String line = getResponseBody(conn);
        long end = System.currentTimeMillis();
        Log.d(TAG, httpRequest.getUrl() + ": " + (end - init));

        if(line == null) return null;
        Log.d("HttService", line);

        return line;
    }

    public static String postFile(@NonNull HttpFileRequest httpFileRequest) {
        try {
            long e = System.currentTimeMillis();
            if(!checkToken(httpFileRequest)) return null;
            HttpURLConnection conn = buildFileConnection(httpFileRequest);
            Log.d(TAG, httpFileRequest.getUrl() + ": response: " + conn.getResponseCode());
            String line = getResponseBody(conn);
            long end = System.currentTimeMillis();
            Log.d(TAG, httpFileRequest.getUrl() + ": " + (end - e));
            return line;
        } catch (Exception var8) {
            var8.printStackTrace();
            Log.e(TAG, "Http get error " + var8.getMessage());
            return null;
        }
    }

    private static boolean checkToken(@NonNull HttpBaseRequest httpBaseRequest) {
        Token token = httpBaseRequest.getToken();
        if(token != null && token.isValidToken()) return true;
        if(httpBaseRequest.getTokenHttpRequest() == null) return true;
        String response = consume(httpBaseRequest.getTokenHttpRequest());
        if(response == null) return false;
        Token tokenTmp = httpBaseRequest.getTokenListener().getToken(response);
        if(tokenTmp == null) return false;
        httpBaseRequest.setToken(tokenTmp);
        return true;
    }

    private static String getResponseBody(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        String lineTmp;
        while((lineTmp = reader.readLine()) != null){
            line += lineTmp;
        }
        is.close();
        reader.close();

        return line;
    }

    private static HttpURLConnection buildConnection(@NonNull HttpRequest httpRequest) throws IOException {
        URL url = new URL(httpRequest.getUrl());
        HttpURLConnection conn;
        if(httpRequest.getUrl().contains("https:"))
            conn = (HttpsURLConnection) url.openConnection();
        else conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIME_OUT);
        conn.setReadTimeout(TIME_OUT);
        conn.setRequestMethod(httpRequest.getMethod());

        buildTokenHeader(conn, httpRequest.getToken());
        if(httpRequest.headerMap != null) {
            for (Map.Entry<String, String> entry : httpRequest.headerMap.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        conn.setDoInput(true);
        if(httpRequest.getRawJson() != null) {
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(httpRequest.getRawJson());
            writer.flush();
            writer.close();
            os.close();
        }
        // Starts the query
        conn.connect();
        return conn;
    }

    private static void buildTokenHeader(HttpURLConnection conn, Token token) {
        if(token == null) return;
        conn.setRequestProperty(token.field, token.value);
    }

    private static HttpURLConnection buildFileConnection(HttpFileRequest httpFileRequest) throws IOException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024*1024;

        URL url = new URL(httpFileRequest.getUrl());
        HttpURLConnection connection;
        if(httpFileRequest.getUrl().contains("https:"))
            connection = (HttpsURLConnection) url.openConnection();
        else connection = (HttpURLConnection) url.openConnection();

        buildTokenHeader(connection, httpFileRequest.getToken());
        if (httpFileRequest.headerMap != null) {
            for (Map.Entry<String, String> entry : httpFileRequest.headerMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // Allow Inputs &amp; Outputs.
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        // Set HTTP method to POST.
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

        DataOutputStream outputStream = new DataOutputStream( connection.getOutputStream() );
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);

        if(httpFileRequest.paramsMap != null) {
            for (Map.Entry<String, String> entry : httpFileRequest.paramsMap.entrySet()) {
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(entry.getValue() + lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            }
        }

        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + httpFileRequest.getFileFieldName() + "\";filename=\"" + httpFileRequest.getFileName() +"\"" + lineEnd);
        outputStream.writeBytes(lineEnd);

        bytesAvailable = httpFileRequest.getFileInputStream().available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        // Read file
        bytesRead = httpFileRequest.getFileInputStream().read(buffer, 0, bufferSize);

        while (bytesRead > 0)
        {
            outputStream.write(buffer, 0, bufferSize);
            bytesAvailable = httpFileRequest.getFileInputStream().available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = httpFileRequest.getFileInputStream().read(buffer, 0, bufferSize);
        }

        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        httpFileRequest.getFileInputStream().close();
        outputStream.flush();
        outputStream.close();

        return connection;
    }

//    private static HttpsURLConnection buildImageConnection(String urlString, String field, String fileName,
//                                                           FileInputStream inputStream,
//                                                           HashMap<String, String> paramsMap) throws IOException {
//        String lineEnd = "\r\n";
//        String twoHyphens = "--";
//        String boundary =  "*****";
//
//        URL url = new URL(urlString);
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        conn.setConnectTimeout(TIME_OUT);
//        conn.setReadTimeout(TIME_OUT);
//        conn.setRequestMethod("POST");
//        conn.setDoInput(true);
//        conn.setDoOutput(true);
//        conn.setRequestProperty("Connection", "Keep-Alive");
//        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//        conn.setRequestProperty(field, fileName);
//
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1*1024*1024;
//
//        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//        os.writeBytes(twoHyphens + boundary + lineEnd);
//
//        for(Map.Entry<String, String> entry : paramsMap.entrySet()) {
//            os.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() +"\"" + lineEnd);
//            os.writeBytes(lineEnd);
//            os.writeBytes(entry.getValue() + lineEnd);
//            os.writeBytes(twoHyphens + boundary + lineEnd);
//        }
//
//        if(inputStream != null) {
//            os.writeBytes("Content-Disposition: form-data; name=\"" + field + "\";filename=\"" + fileName + "\"" + lineEnd);
//            os.writeBytes(lineEnd);
//
//            bytesAvailable = inputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//
//            // Read file
//            bytesRead = inputStream.read(buffer, 0, bufferSize);
//
//            while (bytesRead > 0) {
//                os.write(buffer, 0, bufferSize);
//                bytesAvailable = inputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = inputStream.read(buffer, 0, bufferSize);
//            }
//
//            inputStream.close();
//        }
//
//        os.writeBytes(lineEnd);
//        os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//        os.flush();
//        os.close();
//        // Starts the query
//        conn.connect();
//        return conn;
//    }

//    private static String buildParams(HashMap<String, String> paramsMap, String field) {
//        String params = "";
//        boolean first = true;
//
//        for(Map.Entry<String, String> entry : paramsMap.entrySet()){
//            if(first){
//                first = false;
//                params = entry.getKey() + "=" + entry.getValue();
//            }
//            else{
//                params += "&" + entry.getKey() + "=" + entry.getValue();
//            }
//        }
//
//        Log.d(TAG + " params", field + " = " + params);
//        return params;
//    }
}
