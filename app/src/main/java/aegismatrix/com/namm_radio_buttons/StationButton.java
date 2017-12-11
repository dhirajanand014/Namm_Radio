package aegismatrix.com.namm_radio_buttons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Dhiraj on 20-10-2017.
 */

public class StationButton extends Button {
    private Paint mFillPaint = new Paint(), mStrokePaint = new Paint();
    private RectF mRectF = new RectF();
    private Path path = new Path();
    private ShapeDrawable mTrapezoid;
    private static float D_ZERO = 0.0f;
    private static float D_HUNDRED = 100.0f;
    private static float D_TWO_HUNDRED = 200.0f;

    public StationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFillPaint.setColor(Color.parseColor("#90343746"));
        mFillPaint.setStyle(Paint.Style.FILL);
        float radius = getHeight() / 2;
        mRectF.set(getLeft(), getTop(), getLeft() + radius, getBottom() + radius);
        path.moveTo(D_ZERO, D_ZERO);
        path.lineTo(D_HUNDRED, D_ZERO);
        path.lineTo(D_TWO_HUNDRED, D_HUNDRED);
        path.lineTo(D_ZERO, D_HUNDRED);
        path.lineTo(D_ZERO, D_ZERO);

        mTrapezoid = new ShapeDrawable(new PathShape(path, 200.0f, 100.0f));
        mTrapezoid.getPaint().setStyle(Paint.Style.FILL);
        mTrapezoid.getPaint().setStrokeWidth(1.0f);
        mTrapezoid.getPaint().setColor(Color.GREEN);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mTrapezoid.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mFillPaint.setColor(Color.parseColor("#90343746"));
        mFillPaint.setStyle(Paint.Style.FILL);
        float radius = getHeight() / 2;

        // canvas.drawArc(mRectF, 90, 180, false, mFillPaint);
        // canvas.drawRect(getLeft() + radius, getTop(), getRight() - radius - 50, getBottom() - 23, mFillPaint);
//        mStrokePaint.setColor(Color.BLACK);
//        mStrokePaint.setStyle(Paint.Style.STROKE);
//        mStrokePaint.setStrokeWidth(2);
//        mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
//        canvas.drawArc(mRectF, 90, 180, false, mStrokePaint);
//        path.arcTo();
//        path.moveTo(getLeft() + radius, getLeft() + radius);
//        path.lineTo(100.0f, 0.0f);
//        path.lineTo(200.0f, 100.0f);
//        path.lineTo(0.0f, 100.0f);
//        path.lineTo(0.0f, 0.0f);
//        mRectF.set(200, 400, 450, 600);
//        canvas.drawRoundRect(mRectF, 50, 100, mFillPaint);
        mTrapezoid.draw(canvas);
    }
}
