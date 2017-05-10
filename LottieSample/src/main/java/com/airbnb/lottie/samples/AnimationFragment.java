package com.airbnb.lottie.samples;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnimationFragment extends Fragment {
  private static final String TAG = AnimationFragment.class.getSimpleName();

  private static final int RC_ASSET = 1337;
  private static final int RC_FILE = 1338;
  private static final int RC_URL = 1339;
  private static final int RC_QR = 1340;
  private static final int RC_CAMERA = 1341;

  static final String EXTRA_ANIMATION_NAME = "animation_name";
  static final String EXTRA_URL = "json_url";
  private static final float SCALE_SLIDER_FACTOR = 50f;

  static AnimationFragment newInstance() {
    return new AnimationFragment();
  }

  private final Map<String, String> assetFolders = new HashMap<String, String>() {{
    put("WeAccept.json", "Images/WeAccept");
  }};

  private OkHttpClient client;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.instructions) ViewGroup instructionsContainer;
  @BindView(R.id.animation_container) ViewGroup animationContainer;
  @BindView(R.id.animation_view) LottieAnimationView animationView;
  @BindView(R.id.seek_bar) AppCompatSeekBar seekBar;
  @BindView(R.id.scale_seek_bar) AppCompatSeekBar scaleSeekBar;
  @BindView(R.id.scale_text) TextView scaleTextView;
  @BindView(R.id.invert_colors) ImageButton invertButton;
  @BindView(R.id.play_button) ImageButton playButton;
  @BindView(R.id.loop) ImageButton loopButton;
  @BindView(R.id.animation_name) TextView animationNameView;
  @BindView(R.id.qr_code) TextView qrCodeTextView;
  @BindView(R.id.sample_animations) TextView sampleAnimationsTextView;
  @BindView(R.id.load_animation) TextView loadAnimationTextView;
  @BindView(R.id.load_from_json) TextView loadFromJsonTextView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_animation, container, false);
    ButterKnife.bind(this, view);

    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_back);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getFragmentManager().popBackStack();
      }
    });
    setHasOptionsMenu(true);
    postUpdatePlayButtonText();
    onLoopChanged();

    setDrawableLeft(qrCodeTextView, R.drawable.ic_qr_scan);
    setDrawableLeft(sampleAnimationsTextView, R.drawable.ic_assets);
    setDrawableLeft(loadAnimationTextView, R.drawable.ic_file);
    setDrawableLeft(loadFromJsonTextView, R.drawable.ic_network);

    animationView.addAnimatorListener(new Animator.AnimatorListener() {
      @Override public void onAnimationStart(Animator animation) {
        startRecordingDroppedFrames();
      }

      @Override public void onAnimationEnd(Animator animation) {
        recordDroppedFrames();
        postUpdatePlayButtonText();
      }

      @Override public void onAnimationCancel(Animator animation) {
        postUpdatePlayButtonText();
      }

      @Override public void onAnimationRepeat(Animator animation) {
        recordDroppedFrames();
        startRecordingDroppedFrames();
      }
    });
    animationView.addAnimatorUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override public void onAnimationUpdate(ValueAnimator animation) {
            seekBar.setProgress((int) (animation.getAnimatedFraction() * 100));
          }
        });

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!animationView.isAnimating()) {
          animationView.setProgress(progress / 100f);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        animationView.setScale(progress / SCALE_SLIDER_FACTOR);
        scaleTextView.setText(String.format(Locale.US, "%.2f", animationView.getScale()));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    return view;
  }

  private void setDrawableLeft(TextView textView, @DrawableRes int resId) {
    //noinspection RestrictedApi
    Drawable drawable = AppCompatDrawableManager.get().getDrawable(getActivity(), resId);
    textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
  }

  @Override public void onStop() {
    animationView.cancelAnimation();
    super.onStop();
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_animation, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.isCheckable()) {
      item.setChecked(!item.isChecked());
    }
    switch (item.getItemId()) {
      case R.id.hardware_acceleration:
        animationView.useExperimentalHardwareAcceleration(item.isChecked());
        return true;
      case R.id.merge_paths:
        animationView.enableMergePathsForKitKatAndAbove(item.isChecked());
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case RC_ASSET:
        final String assetName = data.getStringExtra(EXTRA_ANIMATION_NAME);
        animationView.setImageAssetsFolder(assetFolders.get(assetName));
        LottieComposition.Factory.fromAssetFileName(getContext(), assetName,
            new OnCompositionLoadedListener() {
              @Override public void onCompositionLoaded(LottieComposition composition) {
                setComposition(composition, assetName);
              }
            });
        break;
      case RC_FILE:
        onFileLoaded(data.getData());
        break;
      case RC_URL:

        break;
      case RC_QR:
        loadUrl(data.getExtras().getString(EXTRA_URL));
        break;
    }
  }

  private void setComposition(LottieComposition composition, String name) {
    instructionsContainer.setVisibility(View.GONE);
    seekBar.setProgress(0);
    animationView.setComposition(composition);
    animationNameView.setText(name);
    scaleTextView.setText(String.format(Locale.US, "%.2f", animationView.getScale()));
    scaleSeekBar.setProgress((int) (animationView.getScale() * SCALE_SLIDER_FACTOR));

  }

  @OnClick(R.id.play_button)
  void onPlayClicked() {
    if (animationView.isAnimating()) {
      animationView.pauseAnimation();
      postUpdatePlayButtonText();
    } else {
      if (animationView.getProgress() == 1f) {
        animationView.setProgress(0f);
      }
      animationView.resumeAnimation();
      postUpdatePlayButtonText();
    }
  }

  @OnClick(R.id.loop)
  void onLoopChanged() {
    loopButton.setActivated(!loopButton.isActivated());
    animationView.loop(loopButton.isActivated());
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == RC_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager
        .PERMISSION_GRANTED) {
      startActivityForResult(new Intent(getContext(), QRScanActivity.class), RC_QR);
    } else {
      Toast.makeText(getContext(), R.string.permission_required, Toast.LENGTH_LONG).show();
    }

  }

  @OnClick(R.id.qrscan)
  void onQRScanClicked() {
    animationView.cancelAnimation();
    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {

      requestPermissions(new String[]{Manifest.permission.CAMERA}, RC_CAMERA);

    } else {
      startActivityForResult(new Intent(getContext(), QRScanActivity.class), RC_QR);
    }
  }

  @OnClick(R.id.restart)
  void onRestartClicked() {
    boolean restart = animationView.isAnimating();
    animationView.cancelAnimation();
    animationView.setProgress(0f);
    if (restart) {
      animationView.playAnimation();
    }
  }

  @OnClick(R.id.invert_colors)
  void onInvertClicked() {
    animationContainer.setActivated(!animationContainer.isActivated());
    invertButton.setActivated(animationContainer.isActivated());
  }

  @OnClick(R.id.load_asset)
  void onLoadAssetClicked() {
    animationView.cancelAnimation();
    android.support.v4.app.DialogFragment assetFragment = ChooseAssetDialogFragment.newInstance();
    assetFragment.setTargetFragment(this, RC_ASSET);
    assetFragment.show(getFragmentManager(), "assets");
  }

  @OnClick(R.id.load_file)
  void onLoadFileClicked() {
    animationView.cancelAnimation();
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("*/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);

    try {
      startActivityForResult(Intent.createChooser(intent, "Select a JSON file"), RC_FILE);
    } catch (android.content.ActivityNotFoundException ex) {
      // Potentially direct the user to the Market with a Dialog
      Toast.makeText(getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
    }
  }

  @OnClick(R.id.load_url)
  void onLoadUrlClicked() {
    animationView.cancelAnimation();
    final EditText urlView = new EditText(getContext());
    new AlertDialog.Builder(getContext())
        .setTitle("Enter a URL")
        .setView(urlView)
        .setPositiveButton("Load", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            loadUrl(urlView.getText().toString());
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .show();
  }

  private void postUpdatePlayButtonText() {
    new Handler().post(new Runnable() {
      @Override public void run() {
        updatePlayButtonText();
      }
    });
  }

  private void updatePlayButtonText() {
    playButton.setActivated(animationView.isAnimating());
  }

  private void onFileLoaded(final Uri uri) {
    InputStream fis;

    try {
      switch (uri.getScheme()) {
        case "file":
          fis = new FileInputStream(uri.getPath());
          break;
        case "content":
          fis = getContext().getContentResolver().openInputStream(uri);
          break;
        default:
          onLoadError();
          return;
      }
    } catch (FileNotFoundException e) {
      onLoadError();
      return;
    }

    LottieComposition.Factory
        .fromInputStream(getContext(), fis, new OnCompositionLoadedListener() {
          @Override public void onCompositionLoaded(LottieComposition composition) {
            setComposition(composition, uri.getPath());
          }
        });
  }

  private void loadUrl(String url) {
    Request request;
    try {
      request = new Request.Builder()
          .url(url)
          .build();
    } catch (IllegalArgumentException e) {
      onLoadError();
      return;
    }


    if (client == null) {
      client = new OkHttpClient();
    }
    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        onLoadError();
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          onLoadError();
        }

        try {
          JSONObject json = new JSONObject(response.body().string());
          LottieComposition.Factory
              .fromJson(getResources(), json, new OnCompositionLoadedListener() {
                @Override public void onCompositionLoaded(LottieComposition composition) {
                  setComposition(composition, "Network Animation");
                }
              });
        } catch (JSONException e) {
          onLoadError();
        }
      }
    });
  }

  private void onLoadError() {
    //noinspection ConstantConditions
    Snackbar.make(getView(), "Failed to load animation", Snackbar.LENGTH_LONG).show();
  }

  private void startRecordingDroppedFrames() {
    getApplication().startRecordingDroppedFrames();
  }

  private void recordDroppedFrames() {
    Pair<Integer, Long> droppedFrames = getApplication().stopRecordingDroppedFrames();
    Log.d(TAG, "Dropped frames: " + droppedFrames.first);
  }

  private ILottieApplication getApplication() {
    return (ILottieApplication) getActivity().getApplication();
  }
}