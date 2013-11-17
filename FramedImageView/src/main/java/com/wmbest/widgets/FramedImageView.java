package com.wmbest.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FramedImageView extends ImageView {

    private Type  mType  = Type.CIRCLE;
    private Paint mPaint = new Paint();
    private RectF mBounds;

    private Drawable mFrame;
    private float    mRadius;
    private Shape    mShape;


    public FramedImageView(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs, 0);
        setup(aContext, aAttrs);
    }

    public FramedImageView(Context aContext, AttributeSet aAttrs, int defStyle) {
        super(aContext, aAttrs, defStyle);
        setup(aContext, aAttrs);
    }

    @Override
    public void setImageURI(Uri aUri) {
        super.setImageURI(aUri);
        initPaint();
    }

    @Override
    public void setImageResource(int aRes) {
        super.setImageResource(aRes);
        initPaint();
    }

    @Override
    public void setImageDrawable(Drawable aDrawable) {
        super.setImageDrawable(aDrawable);
        initPaint();
    }

    private void setup(Context aContext, AttributeSet aAttrs) {
        TypedArray a = aContext.obtainStyledAttributes(aAttrs, R.styleable.FramedImageView);

        int type = a.getInt(R.styleable.FramedImageView_type, 0);
        mType = Type.values()[type];
        mFrame = a.getDrawable(R.styleable.FramedImageView_frame);

        switch (mType) {
            case ROUNDED:
                initRounded(a);
                break;
            case SHAPE:
                mType = Type.NONE;
//                mShape = a.getDrawable(R.styleable.FramedImageView_shape);
                break;
        }

        a.recycle();
    }

    private void initRounded(TypedArray aAttr) {
        mRadius = aAttr.getDimension(R.styleable.FramedImageView_radius, 0);
        if (mRadius == 0) {
            float tl, tr, bl, br;
            tl = aAttr.getDimension(R.styleable.FramedImageView_radiusTopLeft, 0);
            tr = aAttr.getDimension(R.styleable.FramedImageView_radiusTopRight, 0);
            bl = aAttr.getDimension(R.styleable.FramedImageView_radiusBottomLeft, 0);
            br = aAttr.getDimension(R.styleable.FramedImageView_radiusBottomRight, 0);
            if (tl + tr + bl + br > 0) {
                float[] outerRadii = new float[8];
                outerRadii[0] = outerRadii[1] = tl;
                outerRadii[2] = outerRadii[3] = tr;
                outerRadii[4] = outerRadii[5] = br;
                outerRadii[6] = outerRadii[7] = bl;
                mShape = new RoundRectShape(outerRadii, null, null);
                mType = Type.SHAPE;
            } else {
                mType = Type.NONE;
            }
        }
    }

    private void initPaint() {
        if (getDrawable() == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;
        Bitmap b = drawableToBitmap(getDrawable());
        BitmapShader shader = new BitmapShader(b,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Matrix m = new Matrix();
        float scaleX = getWidth() / ((float) b.getWidth());
        float scaleY = getHeight() / ((float) b.getHeight());

        m.setScale(scaleX, scaleY);

        shader.setLocalMatrix(m);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(shader);

        mBounds = new RectF(0, 0, b.getWidth(), b.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPaint.getShader() == null) initPaint();

        if (mFrame != null) {
            mFrame.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }

        switch (mType) {
            case SHAPE:
                drawShape(canvas);
                break;
            case ROUNDED:
                drawRounded(canvas);
                break;
            case CIRCLE:
                drawCircle(canvas);
                break;
            default:
                super.onDraw(canvas);
        }

        if (mFrame != null) {
            mFrame.draw(canvas);
        }
    }

    private void drawShape(Canvas aCanvas) {
        mShape.resize(mBounds.width(), mBounds.height());
        mShape.draw(aCanvas, mPaint);
    }

    private void drawRounded(Canvas aCanvas) {
        aCanvas.drawRoundRect(mBounds, mRadius, mRadius, mPaint);
    }

    private void drawCircle(Canvas aCanvas) {
        int center = getWidth() / 2;
        aCanvas.drawCircle(center, center, center, mPaint);
    }

    public static Bitmap drawableToBitmap (Drawable aDrawable) {
        if (aDrawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) aDrawable).getBitmap();
        }

        int width = aDrawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = aDrawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        aDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        aDrawable.draw(canvas);

        return bitmap;
    }

    public static enum Type {
        NONE, SQUARE, CIRCLE, ROUNDED, SHAPE
    }
}

