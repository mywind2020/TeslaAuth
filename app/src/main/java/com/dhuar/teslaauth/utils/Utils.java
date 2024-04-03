package com.dhuar.teslaauth.utils;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    public static void writeToken(SharedPreferences sharedPreferences,String json) {
        try {
            JSONObject result = new JSONObject(json);
            String accessToken = result.getString("access_token");
            String refreshToken = result.getString("refresh_token");
            System.out.println("expires_in:" + result.getInt("expires_in"));
            Long expires = System.currentTimeMillis() + result.getInt("expires_in") * 1000;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("access_token",accessToken);
            editor.putString("refresh_token",refreshToken);
            editor.putLong("expires",expires);
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String convertSecondsToHHMMSS(int totalSeconds) {
        System.out.println("convertSecondsToHHMMSS:" + totalSeconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        //System.out.println(String.format("%02d:%02d:%02d",hours,minutes,seconds));
        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }
}
