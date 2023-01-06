package com.example.kobe_anlaigg.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MyRequest {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public byte[] get(String url) throws Exception {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().bytes();
        }
    }

    public String post(String url, String data, String filepath) throws Exception {
        RequestBody body;
        if (filepath == null || filepath.isEmpty()) {
            FormBody.Builder builder = new FormBody.Builder();
            JSONObject jsonObject=new JSONObject(data);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                builder.add(key,jsonObject.get(key).toString());
            }
            body=builder.build();
        } else {
            // 创建带文件的请求体
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            JSONObject jsonObject = new JSONObject(data);
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                builder.addFormDataPart(key,jsonObject.get(key).toString());
            }
            // 添加文件
            File file = new File(filepath);
            String fileName = file.getName();
            String contentType = URLConnection.guessContentTypeFromName(fileName);
            RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), file);
            builder.addFormDataPart("file", fileName, requestBody);
            body = builder.build();
        }
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}