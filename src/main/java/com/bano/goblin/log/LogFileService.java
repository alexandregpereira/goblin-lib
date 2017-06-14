package com.bano.goblin.log;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bano.goblin.sync.HttpRequest;
import com.bano.goblin.sync.HttpService;
import com.bano.goblin.util.DateUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashMap;


/**
 *
 * Created by Alexandre on 08/12/2016.
 */
public class LogFileService extends IntentService{

    private static final String TAG = "LogFileService";

    private static final String DATE_STRING_KEY = "DATE_STRING_KEY";
    private static final String URL_KEY = "URL_KEY";
    private static final String FILE_FIELD_NAME_KEY = "FILE_FIELD_NAME_KEY";
    private static final String FILE_NAME_DATE_PATTERN = "yyyyMMdd";

    private static final String LOG_FORMAT = "%s - %s - %s - %s";

    private static final String LOG_FORMAT_LN = "\n%s - %s - %s - %s";
    private static final String DEBUG_LOG_LEVEL = "Debug";
    private static final String INFO_LOG_LEVEL = "Info";
    private static final String ERROR_LOG_LEVEL = "Error";
    private static final String WARN_LOG_LEVEL = "Warn";

    private static final String SAVE_LOG = "SAVE_LOG";
    private static final String SEND_LOG = "SEND_LOG";
    private static final String SEND_DATE_LOG = "SEND_DATE_LOG";

    private static HashMap<String, String> headerMap;
    private static HashMap<String, String> paramsMap;

    public LogFileService(){super(LogFileService.class.getName());}

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(action == null) {
            throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe FileManagerService");
        }

        Bundle data = intent.getExtras();
        switch (action){
            case SAVE_LOG:
                saveLog(data);
                break;
            case SEND_LOG:
            case SEND_DATE_LOG:
                sendLog(data);
                break;
        }
    }

    private void sendLog(Bundle data){
        if(data == null || !data.containsKey(URL_KEY) || !data.containsKey(FILE_FIELD_NAME_KEY)) return;
        String fileName;
        if(!data.containsKey(DATE_STRING_KEY)) fileName = getFileName();
        else fileName = getFileName(data.getString(DATE_STRING_KEY));

        String url = data.getString(URL_KEY);
        String fileFieldName = data.getString(FILE_FIELD_NAME_KEY);
        sendLog(url, fileFieldName, fileName);
    }

    private void sendLog(String url, String fileFieldName, String fileName){
        FileInputStream inputStream = getFileInputStream(this, getFileName());
        if(inputStream != null){
            HttpRequest httpRequest = new HttpRequest(url, HttpRequest.POST_METHOD, headerMap, paramsMap,
                    fileFieldName, fileName, inputStream);
            HttpService.postFile(httpRequest);
        }
        headerMap = null;
        paramsMap = null;
    }

    private void saveLog(Bundle data) {
        if(data == null)
            throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe FileManagerService");
        String log = data.getString(SAVE_LOG);
        if(log == null)
            throw new IllegalArgumentException("Intent sem method key: use os valores statics da classe FileManagerService");

        String fileName = getFileName();
        log += "\n";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND));
            outputStreamWriter.write(log);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Erro ao gravar log: Usuario: log: " + log);
        }
    }

    private String getFileName(){
        return DateUtil.getDateString(FILE_NAME_DATE_PATTERN) + ".txt";
    }

    private String getFileName(String date){
        return date + ".txt";
    }

    private static FileInputStream getFileInputStream(Context context, String fileName){
        try {
            return context.openFileInput(fileName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void print(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT, DEBUG_LOG_LEVEL, tag, value);
    }

    public static void println(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT_LN, DEBUG_LOG_LEVEL, tag, value);
    }

    public static void iprint(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT, INFO_LOG_LEVEL, tag, value);
    }

    public static void iprintln(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT_LN, INFO_LOG_LEVEL, tag, value);
    }

    public static void eprint(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT, ERROR_LOG_LEVEL, tag, value);
    }

    public static void eprintln(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT_LN, ERROR_LOG_LEVEL, tag, value);
    }

    public static void wprintln(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT_LN, WARN_LOG_LEVEL, tag, value);
    }

    public static void wprint(Context context, String tag, String value){
        if(context == null) return;
        startSaveLog(context, LOG_FORMAT, WARN_LOG_LEVEL, tag, value);
    }

    private static void startSaveLog(Context context, String format, String logLevel, String tag, String value){
        String log = String.format(format,
                DateUtil.dateToString(Calendar.getInstance(), DateUtil.DATE_PATTERN_BD_TIMEZONE), logLevel, tag, value);
        Intent intent = new Intent(context, LogFileService.class);
        intent.putExtra(SAVE_LOG, log);
        intent.setAction(SAVE_LOG);
        context.startService(intent);
    }

    public static void sendLog(Context context, HashMap<String, String> headerMap,
                               HashMap<String, String> paramsMap, String fileFieldName, String url){
        LogFileService.headerMap = headerMap;
        LogFileService.paramsMap = paramsMap;
        Intent intent = new Intent(context, LogFileService.class);
        intent.setAction(SEND_LOG);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(FILE_FIELD_NAME_KEY, fileFieldName);
        context.startService(intent);
    }

    public static void sendLog(Context context, HashMap<String, String> headerMap,
                               HashMap<String, String> paramsMap, String fileFieldName, String url, String date){
        LogFileService.headerMap = headerMap;
        LogFileService.paramsMap = paramsMap;
        Intent intent = new Intent(context, LogFileService.class);
        intent.setAction(SEND_DATE_LOG);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(FILE_FIELD_NAME_KEY, fileFieldName);
        intent.putExtra(DATE_STRING_KEY, date);
        context.startService(intent);
    }
}
