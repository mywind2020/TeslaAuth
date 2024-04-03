package com.dhuar.teslaauth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dhuar.teslaauth.databinding.FragmentFirstBinding;
import com.dhuar.teslaauth.utils.TeslaOAuth2;
import com.dhuar.teslaauth.utils.Utils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private SharedPreferences mSharedPreferences;

    private ClipboardManager clipboardManager;
    private TeslaOAuth2 teslaOAuth2 = TeslaOAuth2.getInstance();

    private static final int MSG_SHOW_TOKEN = 1;
    private static final int MSG_ENABLE_REFRESH_BTN = 2;

    private String accessToken;
    private String refreshToken;
    private Long expires;

    private MyCountDownTimer myCountDownTimer;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_TOKEN:
                    showToken();
                    break;
                case MSG_ENABLE_REFRESH_BTN:
                    binding.refreshAccessTokenBtn.setEnabled(true);
                default:
                    break;
            }
        }
    };

    private void showToken() {
        accessToken = mSharedPreferences.getString("access_token","");
        refreshToken = mSharedPreferences.getString("refresh_token","");
        expires = mSharedPreferences.getLong("expires",0);

        if((expires-System.currentTimeMillis())/1000 < 3600) {
            refreshAccessToken();
            return;
        }

        binding.accessTokenView.setText(accessToken);
        binding.refreshTokenView.setText(refreshToken);
//        binding.accessTokenExpiresView.setText();
        if(myCountDownTimer!=null) {
            myCountDownTimer.cancel();
        }

        myCountDownTimer = new MyCountDownTimer(expires-System.currentTimeMillis(),1000);
        myCountDownTimer.start();

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }




    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.accessTokenCopyBtn.setOnClickListener(mOnClickListener);
        binding.refreshTokenCopyBtn.setOnClickListener(mOnClickListener);
        mSharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);



        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        binding.refreshAccessTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.refreshAccessTokenBtn.setEnabled(false);
                refreshAccessToken();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(myCountDownTimer!=null) {
            myCountDownTimer.cancel();
            myCountDownTimer=null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        expires = mSharedPreferences.getLong("expires",0);
        if(expires==0) {
            binding.unauthorizedLayout.setVisibility(View.VISIBLE);
            binding.authorizedLayout.setVisibility(View.INVISIBLE);
        } else {
            binding.unauthorizedLayout.setVisibility(View.INVISIBLE);
            binding.authorizedLayout.setVisibility(View.VISIBLE);
            showToken();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId()== R.id.access_token_copy_btn) {
                copyToClipboard(binding.accessTokenView);
                Toast.makeText(getContext(), R.string.access_token_copied, Toast.LENGTH_SHORT).show();
            } else if(view.getId() == R.id.refresh_token_copy_btn) {
                copyToClipboard(binding.refreshTokenView);
                Toast.makeText(getContext(), R.string.refresh_token_copied, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void copyToClipboard(TextView textView) {
        String text = textView.getText().toString();
        ClipData clipData = ClipData.newPlainText("label", text);
        clipboardManager.setPrimaryClip(clipData);

    }

    public void refreshAccessToken() {
        TeslaOAuth2.getInstance().refreshAccessToken(refreshToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(MSG_ENABLE_REFRESH_BTN);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println(response.body().string());
                    throw new IOException("Unexpected code " + response);
                }
                Utils.writeToken(mSharedPreferences,response.body().string());
                mHandler.sendEmptyMessage(MSG_SHOW_TOKEN);
                mHandler.sendEmptyMessage(MSG_ENABLE_REFRESH_BTN);
            }
        });
    }

    class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish() {
            //tv.setText("finish");
        }
        @Override
        public void onTick(long millisUntilFinished) {
            System.out.println("millisUntilFinished:" + millisUntilFinished);
            binding.accessTokenExpiresView.setText("expire:" + Utils.convertSecondsToHHMMSS((int) millisUntilFinished/1000));
        }
    }
}