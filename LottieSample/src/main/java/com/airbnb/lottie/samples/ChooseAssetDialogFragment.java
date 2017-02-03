package com.airbnb.lottie.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseAssetDialogFragment extends DialogFragment {

  static ChooseAssetDialogFragment newInstance() {
    return new ChooseAssetDialogFragment();
  }

  @BindView(R.id.recycler_view) RecyclerView recyclerView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_choose_asset, container, false);
    ButterKnife.bind(this, view);
    getDialog().setTitle("Choose an Asset");

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    recyclerView.setAdapter(new AssetsAdapter());
  }

  final class AssetsAdapter extends RecyclerView.Adapter<StringViewHolder> {

    private List<String> files = Collections.emptyList();

    AssetsAdapter() {
      try {
        files = AssetUtils.getJsonAssets(getContext(), "");
      } catch (IOException e) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), R.string.invalid_assets, Snackbar.LENGTH_LONG).show();
      }
    }

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new StringViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(StringViewHolder holder, int position) {
      String fileName = files.get(position);
      holder.bind(fileName);
    }

    @Override
    public int getItemCount() {
      return files.size();
    }
  }

  final class StringViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title) TextView titleView;

    StringViewHolder(ViewGroup parent) {
      super(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_file, parent, false));
      ButterKnife.bind(this, itemView);
    }

    void bind(final String fileName) {
      titleView.setText(fileName);
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          getTargetFragment().onActivityResult(
              getTargetRequestCode(),
              Activity.RESULT_OK,
              new Intent().putExtra(AnimationFragment.EXTRA_ANIMATION_NAME, fileName));
          dismiss();
        }
      });
    }
  }
}
