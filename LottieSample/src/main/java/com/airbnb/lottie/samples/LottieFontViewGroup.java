package com.airbnb.lottie.samples;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.model.LottieComposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LottieFontViewGroup extends FrameLayout {

    private final Map<Character, LottieComposition> compositionMap = new HashMap<>();
    private final List<View> views = new ArrayList<>();

    @Nullable private LottieAnimationView cursorView;

    public LottieFontViewGroup(Context context) {
        super(context);
        init();
    }

    public LottieFontViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LottieFontViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusableInTouchMode(true);
        LottieComposition.fromAssetFileName(getContext(), "Amelie/BlinkingCursor.json", new LottieComposition.OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition composition) {
                cursorView = new LottieAnimationView(getContext());
                cursorView.setLayoutParams(new LottieFontViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                cursorView.setComposition(composition);
                cursorView.playAnimation();
                addView(cursorView);
                views.add(cursorView);
            }
        });
    }

    private void addSpace() {
        addView(createSpaceView());
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        if (index == -1) {
            views.add(child);
        } else {
            views.add(index, child);
        }
    }

    private void removeLastView() {
        if (!views.isEmpty()) {
            int position = views.size() - 2;
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

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        BaseInputConnection fic = new BaseInputConnection(this, false);
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_NULL;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT;
        return fic;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            addSpace();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            removeLastView();
            return true;
        }

        if (event.getKeyCode() < KeyEvent.KEYCODE_A || event.getKeyCode() > KeyEvent.KEYCODE_Z) {
            return super.onKeyDown(keyCode, event);
        }


        final char letter = Character.toUpperCase((char) event.getUnicodeChar());
        if (compositionMap.containsKey(letter)) {
            addComposition(compositionMap.get(letter));
        } else {
            String fileName = "Amelie/" + letter + ".json";
            LottieComposition.fromAssetFileName(getContext(), fileName, new LottieComposition.OnCompositionLoadedListener() {
                @Override
                public void onCompositionLoaded(LottieComposition composition) {
                    compositionMap.put(letter, composition);
                    addComposition(composition);
                }
            });
        }

        return true;
    }

    private void addComposition(LottieComposition composition) {
        LottieAnimationView lottieAnimationView = new LottieAnimationView(getContext());
        lottieAnimationView.setLayoutParams(new LottieFontViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        lottieAnimationView.setComposition(composition);
        lottieAnimationView.playAnimation();
        if (cursorView == null) {
            addView(lottieAnimationView);
        } else {
            int index = indexOfChild(cursorView);
            addView(lottieAnimationView, index);
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
