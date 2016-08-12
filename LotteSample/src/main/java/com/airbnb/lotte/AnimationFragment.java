package com.airbnb.lotte;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimationFragment extends Fragment {
    private static final String ARG_FILE_NAME = "file_name";

    static AnimationFragment newInstance(String fileName) {
        AnimationFragment frag = new AnimationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_NAME, fileName);
        frag.setArguments(args);
        return frag;
    }

    @BindView(R.id.file_name) TextView fileNameView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animation, container, false);
        ButterKnife.bind(this, view);

        String fileName = getArguments().getString(ARG_FILE_NAME);
        fileNameView.setText(fileName);

        return view;
    }
}
