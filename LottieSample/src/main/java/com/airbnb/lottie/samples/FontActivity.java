package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FontActivity extends AppCompatActivity {

  @BindView(R.id.scroll_view) ScrollView scrollView;
  @BindView(R.id.font_view) LottieFontViewGroup fontView;

  private final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
    @Override
    public void onGlobalLayout() {
      scrollView.fullScroll(View.FOCUS_DOWN);
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_font);
    ButterKnife.bind(this);

    fontView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
  }


  @Override
  protected void onDestroy() {
    fontView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
    super.onDestroy();
  }
}
