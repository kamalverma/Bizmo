package com.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FastImageView extends ImageView {

public FastImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

}

public FastImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

}

public FastImageView(Context context) {
    super(context);

}

@Override
    public void requestLayout() {
        /*
         * Do nothing here
         */
    }
}
