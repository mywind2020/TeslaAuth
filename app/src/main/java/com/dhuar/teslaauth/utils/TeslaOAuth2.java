package com.dhuar.teslaauth.utils;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TeslaOAuth2 {


    private static TeslaOAuth2 instance;

    private TeslaOAuth2() {
    }

    public static synchronized TeslaOAuth2 getInstance() {
        if(instance==null) {
            instance = new TeslaOAuth2();
        }
        return instance;
    }


    private String codeVerifier = null;
    private String codeChallenge = null;
    private String state = null;
    private String url = null;




    public String getUrl() {
        return url;
    }

    private OkHttpClient client = new OkHttpClient();

    public void init() {
        try {

            codeVerifier = "Kt-0HyWFhyeaX9j6czzWuK-j0CNSRXx7ZhSmAjysbSo";
            codeChallenge = generateCodeChallenge(codeVerifier);

            codeVerifier = "hPYiuONdUb_82mbP6quIc2tPPQvKWusbagc-UnPMOWc";
            codeChallenge = "R0-CV2ucXcLqNHATLo83P-_jKdsuAyCDTZhlssC20Uw";

            state = generateState();

            url = "https://auth.tesla.com/oauth2/v3/authorize?client_id=ownerapi&code_challenge=" + codeChallenge + "&code_challenge_method=S256&redirect_uri=https%3A%2F%2Fauth.tesla.com%2Fvoid%2Fcallback&response_type=code&scope=openid+email+offline_access&state=" + state;
            System.out.println("codeVerifier: " + codeVerifier + "    length:" + codeVerifier.length());
            System.out.println("codeChallenge: " + codeChallenge);
            System.out.println("state: " + state);
            System.out.println("url:" + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier.getBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            String hex = toHex(digest);
            System.out.println("hex:" + hex);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hex.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String generateState() {
        Random random = new Random();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String randomString(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getCode(String url) {
//        String url = "https://auth.tesla.com/void/callback?code=c7dc7f8196d001632558d6632558d6243632558d6b6d60f82c0632558d67&state=aGZzZGpzZnNk&issuer=https%3A%2F%2Fauth.tesla.com%2Foauth2%2Fv3";
        // 解析 URL
        Uri parsedUrl = Uri.parse(url);
        // 获取 code 参数的值
        String code = parsedUrl.getQueryParameter("code");
        return code;
    }

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";



    public void requestToken(String code,Callback callback) {
        String url = "https://auth.tesla.cn/oauth2/v3/token";
        JSONObject json = new JSONObject();
        try {
            json.put("grant_type","authorization_code");
            json.put("client_id","ownerapi");
            json.put("code",code);
            json.put("code_verifier",codeVerifier);
            json.put("redirect_uri","https://auth.tesla.com/void/callback");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void refreshAccessToken(String refreshToken,Callback callback) {
        String url = "https://auth.tesla.cn/oauth2/v3/token";
        JSONObject json = new JSONObject();
        try {
            json.put("grant_type","refresh_token");
            json.put("client_id","ownerapi");
            json.put("refresh_token",refreshToken);
            json.put("scope","openid email offline_access");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }



}
