package com.raphaelyu.phonemouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CoordinateView extends View {

	private static final int size = 200;

	private float mPosX;

	private float mPosY;

	public CoordinateView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(size, size);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.RED);

		canvas.drawCircle(mPosX + size / 2, mPosY + size / 2, 5, paint);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawCircle(size / 2, size / 2, 2, paint);
	}

	public void setPosition(float x, float y) {
		mPosX = size / 2 * x;
		mPosY = size / 2 * y;
	}
}
