package com.raphaelyu.phonemouse;

import java.io.Serializable;
import java.util.Arrays;

import com.raphaelyu.R;

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
import android.widget.ToggleButton;

public class PhoneMouseActivity extends Activity implements SensorEventListener {

	private static final float EPSILON = 0.00f;

	private float mPosX;

	private float mPosY;

	private float mOriginX = 0.0f;

	private float mOriginY = 0.0f;

	private TextView mTvAcceleration;

	private ToggleButton mTbSwitch;

	private CoordinateView mVwCoordinate;

	private SensorManager mSensorManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mVwCoordinate = (CoordinateView) findViewById(R.id.vw_coordinate);
		mTvAcceleration = (TextView) findViewById(R.id.tv_acc);
		mTbSwitch = (ToggleButton) findViewById(R.id.tb_switch);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mTbSwitch.setChecked(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTbSwitch.setChecked(false);
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		boolean valuesUpdated = false;
		float values[] = event.values;

		float Gx = values[0] / 9.81f;
		float Gy = -values[1] / 9.81f;

		if (mTbSwitch.isChecked()) {
			// 过滤传感器的误差
			float tmpX = Gx - mOriginX;
			float tmpY = Gy - mOriginY;
			if (Math.abs(tmpX - mPosX) > EPSILON) {
				mPosX = tmpX;
				valuesUpdated = true;
			}
			if (Math.abs(tmpY - mPosY) > EPSILON) {
				mPosY = tmpY;
				valuesUpdated = true;
			}
			if (valuesUpdated) {
				mVwCoordinate.setPosition((float) mPosX, (float) mPosY);
				mTvAcceleration.setText(String.format("%.4f %.4f", mPosX, mPosY));
				mVwCoordinate.invalidate();
			}
		} else {
			mOriginX = Gx;
			mOriginY = Gy;
		}
	}
}