package com.airbnb.lotte.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lotte.LotteAnimationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AnimationFragment extends Fragment {
    private static final String ARG_FILE_NAME = "file_name";

    static AnimationFragment newInstance(String fileName) {
        AnimationFragment frag = new AnimationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        frag.setArguments(args);
        return frag;
    }

    @BindView(R.id.animation_view) LotteAnimationView animationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animation, container, false);
        ButterKnife.bind(this, view);

        String fileName = getArguments().getString(ARG_FILE_NAME);
        animationView.setAnimation(fileName);

        return view;
    }

    @OnClick(R.id.play)
    public void onPlayClicked() {
        animationView.play();
    }
}
