package com.airbnb.lottie.samples;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.widget.TextView;

import com.airbnb.lottie.layers.LottieDrawable;
import com.airbnb.lottie.model.LottieComposition;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FontActivity extends AppCompatActivity {

    private Map<Character, LottieComposition> compositionMap = new HashMap<>();

    @BindView(R.id.text_view) TextView textView;

    private final SpannableStringBuilder ssb = new SpannableStringBuilder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            ssb.append("    ");
            textView.setText(ssb);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DEL && ssb.length() > 0) {
            ssb.delete(ssb.length() - 1, ssb.length());
            textView.setText(ssb);
            return true;
        }

        if (event.getKeyCode() < KeyEvent.KEYCODE_A || event.getKeyCode() > KeyEvent.KEYCODE_Z) {
            return false;
        }


        final char letter = Character.toUpperCase((char) event.getUnicodeChar());
        if (compositionMap.containsKey(letter)) {
            addComposition(compositionMap.get(letter));
        } else {
            String fileName = "Amelie/" + letter + ".json";
            LottieComposition.fromFile(this, fileName, new LottieComposition.OnCompositionLoadedListener() {
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
        LottieDrawable drawable = new LottieDrawable(composition, new Drawable.Callback() {
            @Override
            public void invalidateDrawable(Drawable who) {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        // This may not render the last frame if we don't post this.
                        textView.setText(ssb);
                    }
                });
            }

            @Override
            public void scheduleDrawable(Drawable who, Runnable what, long when) {

            }

            @Override
            public void unscheduleDrawable(Drawable who, Runnable what) {

            }
        });
        drawable.playAnimation();
        drawable.setBounds(0, 0, 100, 100);
        ImageSpan span = new ImageSpan(drawable);
        ssb.append("_");
        ssb.setSpan(span, ssb.length() - 1, ssb.length(), 0);
        textView.setText(ssb);
    }
}
