package com.airbnb.lottie.samples;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListFragment extends Fragment {

  static ListFragment newInstance() {
    return new ListFragment();
  }

  private static final String SHORTCUT_VIEWER = "com.airbnb.lottie.samples.shortcut.VIEWER";
  private static final String SHORTCUT_TYPOGRAPHY = "com.airbnb.lottie.samples.shortcut.TYPOGRAPHY";
  private static final String SHORTCUT_TUTORIAL = "com.airbnb.lottie.samples.shortcut.TUTORIAL";
  private static final String SHORTCUT_FULLSCREEN = "com.airbnb.lottie.samples.shortcut.FULLSCREEN";

  @BindView(R.id.container) ViewGroup container;
  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.animation_view) LottieAnimationView animationView;

  private final FileAdapter adapter = new FileAdapter();

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleShortcut(getActivity().getIntent().getAction());
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_list, container, false);
    ButterKnife.bind(this, view);

    recyclerView.setAdapter(adapter);

    return view;
  }

  /**
   * Starts the relevant activity/fragment based on which shortcut has been pressed
   * @param intentAction specific shortcut action
   */
  private void handleShortcut(String intentAction) {
    switch (intentAction) {
      case SHORTCUT_VIEWER:
        onViewerClicked();
        break;
      case SHORTCUT_TYPOGRAPHY:
        onTypographyClicked();
        break;
      case SHORTCUT_TUTORIAL:
        onAppIntroPagerClicked();
        break;
      case SHORTCUT_FULLSCREEN:
        onFullScreenClicked();
        break;
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    animationView.setProgress(0f);
    animationView.playAnimation();
  }

  @Override
  public void onStop() {
    super.onStop();
    animationView.cancelAnimation();
  }

  private void onViewerClicked() {
    showFragment(AnimationFragment.newInstance());
  }

  private void onTypographyClicked() {
    startActivity(new Intent(getContext(), TypographyDemoActivity.class));
  }

  private void onAppIntroPagerClicked() {
    startActivity(new Intent(getContext(), AppIntroActivity.class));
  }

  private void onFullScreenClicked() {
    startActivity(new Intent(getContext(), FullScreenActivity.class));
  }

  private void onFontClicked() {
    showFragment(FontFragment.newInstance());
  }

  private void onOpenLottieFilesClicked() {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse("http://www.lottiefiles.com"));
    startActivity(i);
  }

  private void showFragment(Fragment fragment) {
    getFragmentManager().beginTransaction()
        .addToBackStack(null)
        .setCustomAnimations(R.anim.slide_in_right, R.anim.hold, R.anim.hold, R.anim.slide_out_right)
        .remove(this)
        .replace(R.id.content_2, fragment)
        .commit();
  }

  private final class FileAdapter extends RecyclerView.Adapter<StringViewHolder> {
    private static final String TAG_VIEWER = "viewer";
    private static final String TAG_TYPOGRAPHY = "typography";
    private static final String TAG_APP_INTRO = "app_intro";
    private static final String TAG_FULL_SCREEN = "full_screen";
    private static final String TAG_FONT = "font";
    private static final String TAG_OPEN_LOTTIE_FILES = "open_lottie_files";

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new StringViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(StringViewHolder holder, int position) {
      switch (position) {
        case 0:
          holder.bind("Animation Viewer", TAG_VIEWER);
          break;
        case 1:
          holder.bind("Animated Typography", TAG_TYPOGRAPHY);
          break;
        case 2:
          holder.bind("Animated App Tutorial", TAG_APP_INTRO);
          break;
        case 3:
          holder.bind("Full screen animation", TAG_FULL_SCREEN);
          break;
        case 4:
          holder.bind("Custom fonts and dynamic text", TAG_FONT);
          break;
        case 5:
          holder.bind("Open lottiefiles.com", TAG_OPEN_LOTTIE_FILES);
          break;
      }
    }

    @Override
    public int getItemCount() {
      return 6;
    }
  }

  final class StringViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title) TextView titleView;

    StringViewHolder(ViewGroup parent) {
      super(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_file, parent, false));
      ButterKnife.bind(this, itemView);
    }

    void bind(String title, String tag) {
      titleView.setText(title);
      itemView.setTag(tag);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (FileAdapter.TAG_VIEWER.equals(v.getTag())) {
            onViewerClicked();
          } else if (FileAdapter.TAG_TYPOGRAPHY.equals(v.getTag())) {
            onTypographyClicked();
          } else if (FileAdapter.TAG_APP_INTRO.equals(v.getTag())) {
            onAppIntroPagerClicked();
          } else if (FileAdapter.TAG_FULL_SCREEN.equals(v.getTag())) {
            onFullScreenClicked();
          } else if (FileAdapter.TAG_FONT.equals(v.getTag())) {
            onFontClicked();
          } else if (FileAdapter.TAG_OPEN_LOTTIE_FILES.equals(v.getTag())) {
            onOpenLottieFilesClicked();
          }
        }
      });
    }
  }
}
