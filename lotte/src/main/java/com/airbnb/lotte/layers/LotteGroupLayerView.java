package com.airbnb.lotte.layers;

import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeFill;
import com.airbnb.lotte.model.LotteShapeGroup;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.model.LotteShapeTransform;
import com.airbnb.lotte.model.LotteShapeTrimPath;

public class LotteGroupLayerView extends LotteAnimatableLayer {

    public LotteGroupLayerView(LotteShapeGroup item, @Nullable LotteShapeTransform transform, @Nullable LotteShapeFill fill,
            @Nullable LotteShapeStroke stroke, @Nullable LotteShapeTrimPath trimPath, long duration) {
        super(duration);
        // TODO
    }
}
