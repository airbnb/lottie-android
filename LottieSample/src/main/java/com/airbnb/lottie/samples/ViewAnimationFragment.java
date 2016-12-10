package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieViewAnimator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewAnimationFragment extends Fragment {

    static Fragment newInstance() {
        return new ViewAnimationFragment();
    }

    @BindView(R.id.message_bubble) View messageBubble;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_animation, container, false);
        ButterKnife.bind(this, view);

        messageBubble.setTag(R.id.lottie_layer_name, "Tip");
        LottieViewAnimator.of(getContext(), "Tip.json", messageBubble)
                .loop(true)
                .start();

        return view;
    }
}
