package com.airbnb.lotte.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lotte.LotteAnimationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GridFragment extends Fragment {

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    @BindView(R.id.grid) RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new Adapter());
        return view;
    }

    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private static final String[] FILE_NAMES = {
                "Icon_Lightbulb_Still.json",
                "Mask_22.json",
                "Mask_23.json",
                "bigClock.json",
                "Icon_Diamond_Still.json",
                "Mask_20.json",
                "ellipseTrim.json",
        };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(FILE_NAMES[position]);
        }

        @Override
        public int getItemCount() {
            return FILE_NAMES.length;
        }
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name) TextView nameView;
        @BindView(R.id.animation_view) LotteAnimationView animationView;

        public ViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_grid_item, parent, false));
            ButterKnife.bind(this, itemView);
        }

        void bind(String name) {
            nameView.setText(name);
            animationView.setAnimation(name);
        }
    }
}
