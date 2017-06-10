package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WarningsDialogFragment extends DialogFragment {
  private static final String ARG_WARNINGS = "warnings";

  static WarningsDialogFragment newInstance(ArrayList<String> warnings) {
    Bundle args = new Bundle();
    args.putStringArrayList(ARG_WARNINGS, warnings);
    WarningsDialogFragment frag = new WarningsDialogFragment();
    frag.setArguments(args);
    return frag;
  }

  @BindView(R.id.recycler_view) RecyclerView recyclerView;
  @BindView(R.id.ok_button) Button okButton;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_warnings, container, false);
    ButterKnife.bind(this, view);
    okButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dismiss();
      }
    });
    recyclerView.setAdapter(new Adapter(getArguments().getStringArrayList(ARG_WARNINGS)));
    return view;
  }

  private static final class Adapter extends RecyclerView.Adapter<VH> {

    private final List<String> warnings;

    private Adapter(List<String> warnings) {
      this.warnings = warnings;
    }

    @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
      return new VH(parent);
    }

    @Override public void onBindViewHolder(VH holder, int position) {
      holder.bind(warnings.get(position), position != getItemCount() - 1);
    }

    @Override public int getItemCount() {
      return warnings.size();
    }
  }

  static final class VH extends RecyclerView.ViewHolder {

    @BindView(R.id.warning) TextView warningView;
    @BindView(R.id.divider) View divider;

    VH(ViewGroup parent) {
      super(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.view_holder_warning, parent, false));
      ButterKnife.bind(this, itemView);
    }

    void bind(String warning, boolean showDivider) {
      warningView.setText(warning);
      divider.setVisibility(showDivider ? View.VISIBLE : View.GONE);
    }
  }
}
