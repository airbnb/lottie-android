package com.airbnb.lottie.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListFragment extends Fragment {

    static ListFragment newInstance() {
        return new ListFragment();
    }

    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private final FileAdapter adapter = new FileAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        recyclerView.setAdapter(adapter);
        try {
            adapter.setFiles(AssetUtils.getJsonAssets(getContext(), ""));
        } catch (IOException e) {
            //noinspection ConstantConditions
            Snackbar.make(container, R.string.invalid_assets, Snackbar.LENGTH_LONG).show();
        }

        return view;
    }

    private void onNetworkClicked() {
        showFragment(NetworkFragment.newInstance());
    }

    private void onLocalFileClicked() {
        showFragment(LocalFileFragment.newInstance());
    }

    private void onFileClicked(String fileName) {
        showFragment(AnimationFragment.newInstance(fileName));
    }

    private void onViewTestClicked() {
        showFragment(ViewAnimationFragment.newInstance());
    }

    private void showFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.hold, R.anim.hold, R.anim.slide_out_right)
                .remove(this)
                .replace(R.id.content_2, fragment)
                .commit();
    }

    private void onFontClicked() {
        startActivity(new Intent(getContext(), FontActivity.class));
    }

    final class FileAdapter extends RecyclerView.Adapter<StringViewHolder> {
        static final int VIEW_TYPE_NETWORK = 0;
        static final int VIEW_TYPE_LOCAL_FILE = 1;
        static final int VIEW_TYPE_VIEW_TEST = 2;
        static final int VIEW_TYPE_FONT = 3;
        static final int VIEW_TYPE_FILE = 4;

        @Nullable private List<String> files = null;

        void setFiles(@Nullable List<String> files) {
            this.files = files;
            notifyDataSetChanged();
        }

        @Override
        public StringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StringViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(StringViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_NETWORK:
                    holder.bind("Demo: Load from network", R.drawable.ic_network);
                    break;
                case VIEW_TYPE_LOCAL_FILE:
                    holder.bind("Demo: Load from local file", R.drawable.ic_local_file);
                    break;
                case VIEW_TYPE_LOCAL_FILE:
                    holder.bind("Demo: Load from local file", R.drawable.ic_local_file);
                    break;
                case VIEW_TYPE_VIEW_TEST:
                    holder.bind("Demo: Animate View", R.drawable.ic_view);
                    break;
                case VIEW_TYPE_FONT:
                    holder.bind("Demo: Animated Typography", "Amelie/A.json");
                    break;
                default:
                    //noinspection ConstantConditions
                    String fileName = files.get(position - VIEW_TYPE_FILE);
                    holder.bind(fileName);
            }
        }

        @Override
        public int getItemCount() {
            return (files == null ? 0 : files.size()) + VIEW_TYPE_FILE - 1;
        }

        @Override
        public int getItemViewType(int position) {
            return Math.min(position, VIEW_TYPE_FILE);
        }
    }

    final class StringViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name) TextView fileNameView;
        @BindView(R.id.animation_view) LottieAnimationView animationView;

        StringViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_file, parent, false));
            ButterKnife.bind(this, itemView);
        }

        void bind(String fileName) {
            bind(fileName, fileName, 0);
        }

        void bind(String fileName, @DrawableRes int icon) {
            bind(fileName, fileName, icon);
        }

        void bind(String fileName, String title) {
            bind(fileName, title, 0);
        }

        void bind(String title, final String fileName, @DrawableRes int icon) {
            fileNameView.setText(title);
            if (fileName.contains(".json")) {
                animationView.setAnimation(fileName, LottieAnimationView.CacheStrategy.Strong);
                animationView.setProgress(1f);
            } else if (icon > 0) {
                // animationView.setScaleType(ImageView.ScaleType.CENTER);
                animationView.setImageResource(icon);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (getItemViewType()) {
                        case FileAdapter.VIEW_TYPE_NETWORK:
                            onNetworkClicked();
                            break;
                        case FileAdapter.VIEW_TYPE_LOCAL_FILE:
                            onLocalFileClicked();
                            break;
                        case FileAdapter.VIEW_TYPE_VIEW_TEST:
                            onViewTestClicked();
                            break;
                        case FileAdapter.VIEW_TYPE_FONT:
                            onFontClicked();
                            break;
                        default:
                            onFileClicked(fileName);
                    }
                }
            });
        }
    }
}
