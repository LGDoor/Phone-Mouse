package com.raphaelyu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CoordinateView extends View {

	private static final float scale = 200.0f;

	private float mPosX;

	private float mPosY;

	public CoordinateView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.RED);

		canvas.drawCircle(mPosX + canvas.getWidth() / 2, mPosY + canvas.getHeight() / 2, 5, paint);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 2, paint);
	}

	public void setPosition(float x, float y) {
		mPosX = scale * x;
		mPosY = scale * y;
	}
}
