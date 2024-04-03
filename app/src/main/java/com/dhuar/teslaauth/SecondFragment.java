package com.dhuar.teslaauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dhuar.teslaauth.databinding.FragmentSecondBinding;
import com.dhuar.teslaauth.utils.TeslaOAuth2;
import com.dhuar.teslaauth.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    private static final String TAG = "SecondFragment";

    private static final int MSG_To_HOME = 1;

    private TeslaOAuth2 teslaOAuth2 = TeslaOAuth2.getInstance();

    private SharedPreferences mSharedPreferences;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_To_HOME:
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_FirstFragment);
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        teslaOAuth2.init();
        WebSettings settings = binding.webview.getSettings();
        settings.setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webview,true);
        binding.webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "shouldOverrideUrlLoading:" + url);
                if(url.contains("/void/callback")) {
                    String code = teslaOAuth2.getCode(url);
                    System.out.println("code:" + code);
                    teslaOAuth2.requestToken(code,new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                System.out.println(response.body().string());
                                throw new IOException("Unexpected code " + response);
                            }
                            Utils.writeToken(mSharedPreferences,response.body().string());
                            mHandler.sendEmptyMessage(MSG_To_HOME);
                        }
                    });
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        binding.webview.loadUrl(teslaOAuth2.getUrl());


//        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




}