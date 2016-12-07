package com.airbnb.lottie.samples;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class LottieFontViewGroup extends FrameLayout {

    private final List<View> views = new ArrayList<>();

    public LottieFontViewGroup(Context context) {
        super(context);
    }

    public LottieFontViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LottieFontViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void addSpace() {
        addView(createSpaceView());
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        views.add(child);
    }

    void removeLastView() {
        if (!views.isEmpty()) {
            int position = views.size() - 1;
            removeView(views.get(position));
            views.remove(position);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (views.isEmpty()) {
            return;
        }
        int currentX = getPaddingTop();
        int currentY = getPaddingLeft();

        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            if (!fitsOnCurrentLine(currentX, view)) {
                if (view.getTag() != null && view.getTag().equals("Space")) {
                    continue;
                }
                currentX = getPaddingLeft();
                currentY += view.getMeasuredHeight();
            }
            view.layout(currentX, currentY, currentX + view.getMeasuredWidth(), currentY + view.getMeasuredHeight());
            currentX += view.getWidth();
        }
    }

    private boolean fitsOnCurrentLine(int currentX, View view) {
        return currentX + view.getMeasuredWidth() < getWidth() - getPaddingRight();
    }

    private View createSpaceView() {
        View spaceView = new View(getContext());
        spaceView.setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.font_space_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        spaceView.setTag("Space");
        return spaceView;
    }
}
