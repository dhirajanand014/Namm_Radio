package aegismatrix.com.namm_radio_buttons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.widget.Button;

import aegismatrix.com.namm_radio.R;

/**
 * Created by Dhiraj on 20-10-2017.
 */

public class StationButton extends Button {
    private Paint mFillPaint = new Paint();
    private RectF mRectF = new RectF();
    private Path mTrapazoidPath = new Path(), mImagePath = new Path();
    private ShapeDrawable mTrapezoid;
    private Bitmap tempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.india);
    private static float D_ZERO = 0.0f;
    private static float D_HUNDRED = 100.0f;
    private static float D_TWO_HUNDRED = 200.0f;
    private static float D_ONE_TWENTY = 120.0f;

    public StationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFillPaint.setColor(Color.GREEN);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setStrokeWidth(1.0f);
        mFillPaint.setShadowLayer(10.0f, 10.0f, 10.0f, Color.parseColor("#7f000000"));
        CornerPathEffect effect = new CornerPathEffect(12.0f);
        mFillPaint.setPathEffect(effect);
        mTrapazoidPath.moveTo(D_ZERO, D_ZERO);
        mTrapazoidPath.lineTo(D_ONE_TWENTY, D_ZERO);
        mTrapazoidPath.lineTo(D_TWO_HUNDRED, D_HUNDRED);
        mTrapazoidPath.lineTo(D_ZERO, D_HUNDRED);
        mTrapazoidPath.lineTo(D_ZERO, D_ZERO);
        mTrapazoidPath.close();
        mTrapezoid = new ShapeDrawable(new PathShape(mTrapazoidPath, D_TWO_HUNDRED, D_HUNDRED));
        mTrapezoid.getPaint().setStyle(Paint.Style.FILL);
        mTrapezoid.getPaint().setStrokeWidth(1.0f);
        mTrapezoid.getPaint().setPathEffect(effect);
        mTrapezoid.getPaint().setColor(Color.parseColor("#90343746"));
        mImagePath = new Path();
        mImagePath.setFillType(Path.FillType.EVEN_ODD);
        mImagePath.moveTo(D_TWO_HUNDRED + 71.0f, D_ZERO);
        mImagePath.lineTo(getWidth(), D_ZERO);
        mImagePath.lineTo(getWidth(), getHeight());
        mImagePath.lineTo(D_TWO_HUNDRED + 71.0f, D_ZERO);
        mImagePath.close();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTrapezoid.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mTrapezoid.draw(canvas);
        canvas.drawBitmap(tempBitmap, tempBitmap.getWidth(), tempBitmap.getHeight(), mFillPaint);
        canvas.drawPath(mImagePath, mFillPaint);
        canvas.save();
        canvas.restore();
        invalidate();
    }

}
