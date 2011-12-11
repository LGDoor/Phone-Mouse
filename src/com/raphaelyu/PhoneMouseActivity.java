package com.raphaelyu;

import java.io.Serializable;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PhoneMouseActivity extends Activity implements SensorEventListener {

	private static final float EPSILON = 0.0f;

	private float mPosX;

	private float mPosZ;

	private MotionSampler mSampler;

	private TextView mTvAcceleration;

	private CoordinateView mVwCoordinate;

	private SensorManager mSensorManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTvAcceleration = (TextView) findViewById(R.id.tv_acc);
		mVwCoordinate = (CoordinateView) findViewById(R.id.vw_coordinate);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (savedInstanceState != null) {
			mSampler = (MotionSampler) savedInstanceState.getSerializable("sampler");
		} else {
			mSampler = new MotionSampler();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("sampler", mSampler);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			boolean valuesUpdated = false;
			float values[] = event.values;

			// 过滤传感器的误差
			if (Math.abs(values[0] - mPosX) > EPSILON) {
				mPosX = values[0];
				valuesUpdated = true;
			}
			if (Math.abs(values[2] - mPosZ) > EPSILON) {
				mPosZ = values[2];
				valuesUpdated = true;
			}
			if (valuesUpdated) {
				mSampler.update(System.nanoTime(), mPosX, mPosZ);
				mVwCoordinate.setPosition((float) mSampler.getSpeedX(),
						(float) mSampler.getSpeedY());
				mTvAcceleration.setText(String.format("%.4f %.4f\n%.4f %.4f\n",
						mSampler.getAccelX(), mSampler.getAccelY(), mSampler.getSpeedX(),
						mSampler.getSpeedY()));
				mVwCoordinate.invalidate();
			}
		}
	}

	static class MotionSampler implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2900016726798775939L;

		private long mTimestamp = 0l;

		private double mAccelX = 0.0;

		private double mAccelY = 0.0;

		private double mSpeedX = 0.0;

		private double mSpeedY = 0.0;

		private double mPosX = 0.0;

		private double mPosY = 0.0;

		/**
		 * 采样获取一个新的瞬时加速度，采样点之间以直线拟合
		 * 
		 * @param timestamp
		 *            时间戳，单位为纳秒
		 * @param newAccelX
		 * @param newAccelY
		 */
		public void update(long timestamp, double newAccelX, double newAccelY) {
			if (mTimestamp == 0) {
				mTimestamp = timestamp;
				mAccelX = newAccelX;
				mAccelY = newAccelY;
			} else {
				double duration = (timestamp - mTimestamp) / 1e9;

				// 积分计算
				double newSpeedX = mSpeedX + (mAccelX + newAccelX) * duration / 2;
				double newSpeedY = mSpeedY + (mAccelY + newAccelY) * duration / 2;
				double newPosX = mPosX + (mSpeedX + newSpeedX) * duration / 2;
				double newPosY = mPosY + (mSpeedY + newSpeedY) * duration / 2;

				// 更新数据
				mPosX = newPosX;
				mPosY = newPosY;
				mSpeedX = newSpeedX;
				mSpeedY = newSpeedY;
				mAccelX = newAccelX;
				mAccelY = newAccelY;
				mTimestamp = timestamp;
			}
		}

		public long getTimestamp() {
			return mTimestamp;
		}

		public double getAccelX() {
			return mAccelX;
		}

		public double getAccelY() {
			return mAccelY;
		}

		public double getSpeedX() {
			return mSpeedX;
		}

		public double getSpeedY() {
			return mSpeedY;
		}

		public double getPosX() {
			return mPosX;
		}

		public double getPosY() {
			return mPosY;
		}
	}
}