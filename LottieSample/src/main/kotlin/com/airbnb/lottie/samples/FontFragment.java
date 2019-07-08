package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.TextDelegate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class FontFragment extends Fragment {

  public static FontFragment newInstance() {
      return new FontFragment();
  }

  @BindView(R.id.dynamic_text) LottieAnimationView nameAnimationView;

  private TextDelegate textDelegate;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_font, container, false);
    ButterKnife.bind(this, view);

    textDelegate = new TextDelegate(nameAnimationView);
    nameAnimationView.setTextDelegate(textDelegate);

    return view;
  }

  @OnTextChanged(R.id.name_edit_text) void onNameChanged(CharSequence name) {
    textDelegate.setText("NAME", name.toString());
  }
}
