package com.raphaelyu.phonemouse;

import android.app.Activity;

public class ConnectActivity extends Activity {

}

// TODO: 协议 定长 type:1 byte

/*
 * 协议
 * 
 * DISCOVER: "PHONEMOUSE"
 * REPLY: "HERE"
 * HEARTBEAT: X
 * MOVE: timestamp:8 pos_x:4 pos_y:4
 */
// TODO: Service