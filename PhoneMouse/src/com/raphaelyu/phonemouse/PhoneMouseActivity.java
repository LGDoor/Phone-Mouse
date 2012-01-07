package com.raphaelyu.phonemouse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.raphaelyu.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;

public class PhoneMouseActivity extends Activity implements SensorEventListener, OnTouchListener {
    final static boolean TEST_MODE = false;
    final static String TEST_SERVER_IP = "192.168.1.5";

    /* 协议常量 */
    final static short PHONE_MOUSE_PORT = 5329;
    final static int MAX_PACKET_LENGTH = 64;
    final static byte PACKET_TYPE_DISCOVER = 0x1;
    final static byte PACKET_TYPE_REPLY = 0x2;
    final static byte PACKET_TYPE_MOVE = 0x10;
    final static byte PACKET_TYPE_PRESS = 0x11;
    final static byte PACKET_TYPE_RELEASE = 0x12;
    final static byte PACKET_MOUSE_BUTTON_LEFT = 1;
    final static byte PACKET_MOUSE_BUTTON_RIGHT = 2;
    final static byte PACKET_MOUSE_BUTTON_MIDDLE = 3;

    private static final float EPSILON = 0.00f;

    private float mLastX = 0.0f;

    private float mLastY = 0.0f;

    private ToggleButton mTbSwitch;

    private SensorManager mSensorManager;

    private SocketAddress mServerAddr = null;

    private ViewGroup mLayoutNoServer;

    private ViewGroup mLayoutMouse;

    private Button mBtnRetry;

    private Button mBtnLeft;

    private Button mBtnRight;

    private class DiscoverTask extends AsyncTask<Void, Void, Void> {

        Dialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = ProgressDialog
                    .show(PhoneMouseActivity.this, null, "searching...", true, true);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (mServerAddr != null) {
                mLayoutMouse.setVisibility(View.VISIBLE);
                mLayoutNoServer.setVisibility(View.GONE);
            } else {
                // TODO
            }
            mDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Test mode
            // - specified server address

            if (TEST_MODE) {
                mServerAddr = new InetSocketAddress(TEST_SERVER_IP, PHONE_MOUSE_PORT);
                return null;
            }

            byte[] buf = new byte[1];
            InetAddress broadcastIP;
            try {
                broadcastIP = Inet4Address.getByName("255.255.255.255");
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(8000);

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastIP,
                            PHONE_MOUSE_PORT);
                    buf[0] = PACKET_TYPE_DISCOVER;
                    socket.send(packet);
                    socket.receive(packet);
                    if (packet.getLength() == 1) {
                        byte[] buffer = packet.getData();
                        if (buffer[0] != PACKET_TYPE_REPLY) {
                            mServerAddr = null;
                            return null;
                        }
                    } else {
                        mServerAddr = null;
                        return null;
                    }
                    mServerAddr = packet.getSocketAddress();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }

    private ByteBuffer mPacketBuffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
    DatagramChannel mChannel;

    private void onMoveCommand(float x, float y) {
        if (mServerAddr != null) {
            mPacketBuffer.clear();
            mPacketBuffer.put(PACKET_TYPE_MOVE);
            mPacketBuffer.putLong(System.currentTimeMillis());
            mPacketBuffer.putFloat(x);
            mPacketBuffer.putFloat(y);
            mPacketBuffer.flip();

            sendPacket();
        }
    }

    private void onPressCommand(int button) {
        if (mServerAddr != null) {
            mPacketBuffer.clear();
            mPacketBuffer.put(PACKET_TYPE_PRESS);
            mPacketBuffer.putLong(System.currentTimeMillis());
            mPacketBuffer.putInt(button);
            mPacketBuffer.flip();

            sendPacket();
        }
    }

    private void onReleaseCommand(int button) {
        if (mServerAddr != null) {
            mPacketBuffer.clear();
            mPacketBuffer.put(PACKET_TYPE_RELEASE);
            mPacketBuffer.putLong(System.currentTimeMillis());
            mPacketBuffer.putInt(button);
            mPacketBuffer.flip();

            sendPacket();
        }
    }

    private void sendPacket() {
        if (mServerAddr != null) {
            try {
                mChannel.send(mPacketBuffer, mServerAddr);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createContentView();

        try {
            mChannel = DatagramChannel.open();
            mChannel.configureBlocking(false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createContentView() {
        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBtnRetry = (Button) findViewById(R.id.btn_retry);
        mBtnLeft = (Button) findViewById(R.id.btn_mouse_left);
        mBtnRight = (Button) findViewById(R.id.btn_mouse_right);
        mTbSwitch = (ToggleButton) findViewById(R.id.tb_switch);
        mLayoutMouse = (ViewGroup) findViewById(R.id.layout_mouse);
        mLayoutNoServer = (ViewGroup) findViewById(R.id.layout_no_server);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mTbSwitch.setChecked(false);
        mLayoutMouse.setVisibility(View.GONE);
        mBtnRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DiscoverTask().execute();
            }
        });
        mBtnLeft.setOnTouchListener(this);
        mBtnRight.setOnTouchListener(this);
    }

    @Override
    protected void onDestroy() {
        try {
            mChannel.close();
        } catch (IOException e) {
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME);
        new DiscoverTask().execute();
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

        // 转动与移动镜面对称？
        float newX = -values[0] / 9.81f;

        // 世界坐标系Y轴正向与屏幕坐标系相反
        float newY = -values[1] / 9.81f;

        if (mTbSwitch.isChecked()) {
            // 过滤传感器的误差
            float deltaX = 0f;
            float deltaY = 0f;
            if (Math.abs(newX - mLastX) > EPSILON) {
                deltaX = newX - mLastX;
                mLastX = newX;
                valuesUpdated = true;
            }
            if (Math.abs(newY - mLastY) > EPSILON) {
                deltaY = newY - mLastY;
                mLastY = newY;
                valuesUpdated = true;
            }
            if (valuesUpdated) {
                onMoveCommand(deltaX, deltaY);
            }
        } else {
            mLastX = newX;
            mLastY = newY;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTbSwitch.isChecked()) {
        int button = -1;
        int action = event.getActionMasked();

        switch (v.getId()) {
        case R.id.btn_mouse_left:
            button = PACKET_MOUSE_BUTTON_LEFT;
            break;
        case R.id.btn_mouse_right:
            button = PACKET_MOUSE_BUTTON_RIGHT;
            break;
        }

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            onPressCommand(button);
            break;
        case MotionEvent.ACTION_UP:
            onReleaseCommand(button);
            break;
            }
        }
        return false;
    }
}