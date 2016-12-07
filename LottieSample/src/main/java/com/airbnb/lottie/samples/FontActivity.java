package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.model.LottieComposition;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FontActivity extends AppCompatActivity {

    private Map<Character, LottieComposition> compositionMap = new HashMap<>();

    @BindView(R.id.font_view) LottieFontViewGroup fontView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            fontView.addSpace();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            fontView.removeLastView();
            return true;
        }

        if (event.getKeyCode() < KeyEvent.KEYCODE_A || event.getKeyCode() > KeyEvent.KEYCODE_Z) {
            return super.onKeyDown(keyCode, event);
        }


        final char letter = Character.toUpperCase((char) event.getUnicodeChar());
        if (compositionMap.containsKey(letter)) {
            addComposition(compositionMap.get(letter));
        } else {
            String fileName = "Amelie/" + letter + ".json";
            LottieComposition.fromAssetFileName(this, fileName, new LottieComposition.OnCompositionLoadedListener() {
                @Override
                public void onCompositionLoaded(LottieComposition composition) {
                    compositionMap.put(letter, composition);
                    addComposition(composition);
                }
            });
        }

        return true;
    }

    private void addComposition(LottieComposition composition) {
        LottieAnimationView lottieAnimationView = new LottieAnimationView(this);
        lottieAnimationView.setLayoutParams(new LottieFontViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        lottieAnimationView.setComposition(composition);
        lottieAnimationView.playAnimation();
        fontView.addView(lottieAnimationView);
    }
}
