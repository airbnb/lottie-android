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
                "bigClock.json",
                "Icon_Diamond_Still.json",
                "Icon_Lightbulb_Still.json",
                "ellipseTrim.json",
                "Mask_01.json",
                "Mask_02.json",
                "Mask_03.json",
                "Mask_04.json",
                "Mask_05.json",
                "Mask_06.json",
                "Mask_06b.json",
                "Mask_07.json",
                "Mask_08.json",
                "Mask_09.json",
                "Mask_10.json",
                "Mask_11.json",
                "Mask_12.json",
                "Mask_13.json",
                "Mask_14.json",
                "Mask_15.json",
                "Mask_16.json",
                "Mask_17.json",
                "Mask_18.json",
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
