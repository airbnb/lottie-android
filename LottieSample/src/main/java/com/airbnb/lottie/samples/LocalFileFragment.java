package com.airbnb.lottie.samples;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.model.LottieComposition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalFileFragment extends Fragment {
    private static final String TAG = LocalFileFragment.class.getSimpleName();
    private static final String DIAMOND_JSON = "https://gist.githubusercontent.com/gpeal/f34c9d18cb9a036e29a6b4860bba2d86/raw/018c1ee30565b93d48b10f46eefb785ed120f0e9/Diamond.json";
    private static final int REQUEST_CODE_CHOOSE_FILE = 1337;

    static LocalFileFragment newInstance() {
        return new LocalFileFragment();
    }

    @BindView(R.id.restart) Button restartButton;
    @BindView(R.id.animation_view) LottieAnimationView animationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_file, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.load)
    void onLoadClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*.json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a JSON file"), REQUEST_CODE_CHOOSE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            onError(new IllegalStateException("Failed to retrieve file"));
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_FILE:
                Uri uri = data.getData();
                try {
                    String path = getPath(uri);
                    onFileLoaded(path);
                } catch (URISyntaxException e) {
                    onError(e);
                }
                break;
        }
    }

    private void onFileLoaded(String fileName) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            onError(e);
            return;
        }

        LottieComposition.fromInputStream(getContext(), fis, new LottieComposition.OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition composition) {
                onRestartClicked();
                animationView.setComposition(composition);
                restartButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.restart)
    void onRestartClicked() {
        animationView.setProgress(0f);
        animationView.playAnimation();
    }

    private String getPath(Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor;

            try {
                cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void onError(Exception e) {
        Snackbar.make(getView(), "Failed to load file", Snackbar.LENGTH_LONG).show();
        restartButton.setVisibility(View.GONE);
    }
}
