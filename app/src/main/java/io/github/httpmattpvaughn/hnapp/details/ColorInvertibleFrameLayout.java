package io.github.httpmattpvaughn.hnapp.details;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ColorInvertibleFrameLayout extends FrameLayout {
    // Paint to filter view with
    private Paint paint = null;

    // Whether we should draw the webView inverted
    private boolean drawInverted = false;

    // Color matrix to invert colors
    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };


    public ColorInvertibleFrameLayout(Context context) {
        super(context);
        initPaint();
    }

    public ColorInvertibleFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public ColorInvertibleFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
    }

    public boolean isDrawInverted() {
        return drawInverted;
    }

    public void setDrawInverted(boolean drawInverted) {
        this.drawInverted = drawInverted;
        if(drawInverted) {
            setLayerType(LAYER_TYPE_HARDWARE, paint);
        } else {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }
}

