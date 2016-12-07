package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkFragment extends Fragment {
    private static final String TAG = NetworkFragment.class.getSimpleName();
    private static final String DIAMOND_JSON = "https://gist.githubusercontent.com/gpeal/f34c9d18cb9a036e29a6b4860bba2d86/raw/018c1ee30565b93d48b10f46eefb785ed120f0e9/Diamond.json";

    static NetworkFragment newInstance() {
        return new NetworkFragment();
    }

    private final OkHttpClient client = new OkHttpClient();

    @BindView(R.id.edit_text) EditText editText;
    @BindView(R.id.animation_view) LottieAnimationView animationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.go)
    void onGoClicked() {
        if (editText.getText().length() == 0) {
            animationView.setProgress(0f);
            animationView.playAnimation();
            return;
        }

        String url = editText.getText().toString();
        Request request;
        try {
             request = new Request.Builder()
                    .url(url)
                    .build();
        } catch (IllegalArgumentException e) {
            onLoadFailed(e);
            return;
        }


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onLoadFailed(new IOException("Unexpected code " + response));
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    animationView.setAnimation(json);
                    animationView.playAnimation();
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            editText.setText(null);
                        }
                    });
                } catch (JSONException e) {
                    onLoadFailed(e);
                }
            }
        });
    }

    @OnClick(R.id.diamond_sample)
    void onDiamondSampleClicked() {
        editText.setText(DIAMOND_JSON);
    }

    private void onLoadFailed(Exception e) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), "Error loading json. Check logcat.", Snackbar.LENGTH_LONG).show();
        Log.e(TAG, "Error loading json.", e);
    }
}
