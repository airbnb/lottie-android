package com.airbnb.lottie.samples;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.airbnb.lottie.samples.R.id.play;

public class CycleFragment extends Fragment {

    static CycleFragment newInstance() {
        return new CycleFragment();
    }

    @BindView(R.id.animation_view) LottieAnimationView animationView;
    @BindView(R.id.seek_bar) AppCompatSeekBar seekBar;
    @BindView(play) Button playButton;
    @BindView(R.id.loop_button) ToggleButton loopButton;
    @BindView(R.id.frames_per_second) TextView fpsView;
    @BindView(R.id.dropped_frames) TextView droppedFramesView;
    @BindView(R.id.dropped_frames_per_second) TextView droppedFramesPerSecondView;

    private List<String> jsonAssets;
    private int nextAssetIndex;

    private boolean canceled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cycle, container, false);
        ButterKnife.bind(this, view);

        updatePlayButtonText();
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                startRecordingDroppedFrames();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                recordDroppedFrames();
                if (!canceled) {
                    animationView.post(new Runnable() {
                        @Override
                        public void run() {
                            playNext();
                        }
                    });
                }
                canceled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                recordDroppedFrames();
                startRecordingDroppedFrames();
            }
        });
        animationView.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                seekBar.setProgress((int) (animation.getAnimatedFraction() * 100));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                animationView.setProgress(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        try {
            jsonAssets = AssetUtils.getJsonAssets(getContext(), "");
        } catch (IOException e) {
            //noinspection ConstantConditions
            Snackbar.make(container, R.string.invalid_assets, Snackbar.LENGTH_LONG).show();
        }

        setNextAnimation();

        return view;
    }

    private void setNextAnimation() {
        if (jsonAssets.isEmpty()) {
            return;
        }
        animationView.setAnimation(jsonAssets.get(nextAssetIndex));
        nextAssetIndex = (nextAssetIndex + 1) % jsonAssets.size();
    }

    private void playNext() {
        setNextAnimation();
        animationView.playAnimation();
    }

    @Override
    public void onStop() {
        animationView.cancelAnimation();
        super.onStop();
    }

    @OnClick(play)
    public void onPlayClicked() {
        if (animationView.isAnimating()) {
            animationView.cancelAnimation();
            updatePlayButtonText();
        } else {
            animationView.playAnimation();
            updatePlayButtonText();
        }
    }

    private void updatePlayButtonText() {
        playButton.setText(animationView.isAnimating() ? "Cancel" : "Play");
    }

    @OnCheckedChanged(R.id.loop_button)
    public void onLoopChanged(boolean loop) {
        animationView.loop(loop);
        if (!loop) {
            animationView.cancelAnimation();
        }
    }

    private void startRecordingDroppedFrames() {
        getApplication().startRecordingDroppedFrames();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void recordDroppedFrames() {
        Pair<Integer, Long> droppedFrames = getApplication().stopRecordingDroppedFrames();
        int targetFrames = (int) ((droppedFrames.second / 1000000000f) * 60);
        int actualFrames = targetFrames - droppedFrames.first;
        fpsView.setText(String.format("Fps: %.0f", actualFrames / (animationView.getDuration() / 1000f)));
        droppedFramesView.setText("Dropped frames: " + droppedFrames.first);
        float droppedFps = droppedFrames.first / (droppedFrames.second / 1000000000f);
        droppedFramesPerSecondView.setText(String.format("Dropped frames per second: %.0f", droppedFps));
    }

    private ILottieApplication getApplication() {
        return (ILottieApplication) getActivity().getApplication();
    }
}
