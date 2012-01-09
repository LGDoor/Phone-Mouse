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

import com.raphaelyu.phonemouse.R;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PhoneMouseActivity extends Activity implements SensorEventListener, OnTouchListener,
        OnClickListener {
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

    private static final float EPSILON = 0.0015f;

    private float mLastX = 0.0f;

    private float mLastY = 0.0f;

    private boolean mPausing;

    private ToggleButton mTbSwitch;

    private SensorManager mSensorManager;

    private SocketAddress mServerAddr = null;

    private ViewGroup mLayoutNoServer;

    private ViewGroup mLayoutMouse;

    private Button mBtnRetry;

    private Button mBtnManual;

    private View mBtnLeft;

    private View mBtnRight;

    private EditText mEtIpAddr;

    private class DiscoverTask extends AsyncTask<Void, Void, Void> {

        Dialog mDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = ProgressDialog.show(PhoneMouseActivity.this, null,
                    getString(R.string.searching), true, true);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (mServerAddr != null) {
                mLayoutNoServer.setVisibility(View.GONE);
                mLayoutMouse.setVisibility(View.VISIBLE);
            } else {
                mLayoutMouse.setVisibility(View.GONE);
                mLayoutNoServer.setVisibility(View.VISIBLE);
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
                    for (int i = 0; i < 3; i++) {
                        socket.send(packet);
                    }
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

            for (int i = 0; i < 3; i++) {
                sendPacket();
                mPacketBuffer.rewind();
            }
        }
    }

    private void onReleaseCommand(int button) {
        if (mServerAddr != null) {
            mPacketBuffer.clear();
            mPacketBuffer.put(PACKET_TYPE_RELEASE);
            mPacketBuffer.putLong(System.currentTimeMillis());
            mPacketBuffer.putInt(button);
            mPacketBuffer.flip();

            for (int i = 0; i < 3; i++) {
                sendPacket();
                mPacketBuffer.rewind();
            }
        }
    }

    private void sendPacket() {
        if (mServerAddr != null && !mPausing) {
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
        mBtnManual = (Button) findViewById(R.id.btn_manual);
        mBtnLeft = (View) findViewById(R.id.btn_mouse_left);
        mBtnRight = (View) findViewById(R.id.btn_mouse_right);
        mTbSwitch = (ToggleButton) findViewById(R.id.tb_switch);
        mLayoutMouse = (ViewGroup) findViewById(R.id.layout_mouse);
        mLayoutNoServer = (ViewGroup) findViewById(R.id.layout_no_server);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mTbSwitch.setChecked(false);
        mLayoutNoServer.setVisibility(View.GONE);
        mLayoutMouse.setVisibility(View.GONE);
        mBtnRetry.setOnClickListener(this);
        mBtnManual.setOnClickListener(this);
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
    protected void onStart() {
        super.onStart();
        new DiscoverTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPausing = false;
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPausing = true;
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTbSwitch.setChecked(false);
        mLayoutMouse.setVisibility(View.GONE);
        mLayoutNoServer.setVisibility(View.GONE);
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
            int action = event.getAction();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_retry:
            new DiscoverTask().execute();
            break;
        case R.id.btn_manual:
            Builder builder = new Builder(this);
            builder.setTitle("IP");
            mEtIpAddr = new EditText(this);
            mEtIpAddr.setKeyListener(new NumberKeyListener() {
                public int getInputType() {
                    return InputType.TYPE_CLASS_NUMBER;
                }

                protected char[] getAcceptedChars() {
                    return new char[] {
                            '0',
                            '1',
                            '2',
                            '3',
                            '4',
                            '5',
                            '6',
                            '7',
                            '8',
                            '9',
                            '.' };
                }
            });
            builder.setView(mEtIpAddr);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String ip = mEtIpAddr.getText().toString();
                    if (ip.equals("")) {
                        return;
                    }
                    try {
                        mServerAddr = new InetSocketAddress(Inet4Address.getByName(ip),
                                PHONE_MOUSE_PORT);
                        dialog.dismiss();
                        mLayoutNoServer.setVisibility(View.GONE);
                        mLayoutMouse.setVisibility(View.VISIBLE);
                    } catch (UnknownHostException e) {
                        Toast.makeText(PhoneMouseActivity.this, R.string.invalid_addr,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            // mLayoutMouse.setVisibility(View.GONE);
            // mLayoutNoServer.setVisibility(View.VISIBLE);
            break;
        }
    }
}