package com.airbnb.lottie;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by minf on 2017/2/22 0022.
 */

public class ExpressionFactory {


    private static HashMap<IPattern, Class<? extends MagicPointFF>> matchChain = new HashMap<>();



    static MagicPointFF matcher(String s) {

        if (TextUtils.isEmpty(s))
            return null;

        Set<IPattern> set = matchChain.keySet();
        try {
            for (IPattern iPattern : set) {
                if (iPattern.matcher(s)) {
                    Class<? extends MagicPointFF> c = matchChain.get(iPattern);
                    return c.newInstance();
                }
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void add(IPattern iPattern, Class<? extends MagicPointFF> c) {
        matchChain.put(iPattern, c);
    }
}
