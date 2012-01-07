package com.raphaelyu.phonemouse;

import java.awt.event.InputEvent;

public class MouseEvent {

    public static final int TYPE_MOVE = 1;
    public static final int TYPE_PRESS = 2;
    public static final int TYPE_RELEASE = 3;

    public static final int BUTTON_LEFT = InputEvent.BUTTON1_MASK;
    public static final int BUTTON_MIDDLE = InputEvent.BUTTON3_MASK;
    public static final int BUTTON_RIGHT = InputEvent.BUTTON2_MASK;

    public static MouseEvent createMoveEvent(long timestamp, float x, float y) {
        MouseEvent event = new MouseEvent();
        event.timestamp = timestamp;
        event.type = TYPE_MOVE;
        event.x = x;
        event.y = y;
        return event;
    }

    public static MouseEvent createPressEvent(long timestamp, int button) {
        MouseEvent event = new MouseEvent();
        event.timestamp = timestamp;
        event.type = TYPE_PRESS;
        event.button = button;
        return event;
    }

    public static MouseEvent createReleaseEvent(long timestamp, int button) {
        MouseEvent event = new MouseEvent();
        event.timestamp = timestamp;
        event.type = TYPE_RELEASE;
        event.button = button;
        return event;
    }

    public static MouseEvent createWheelEvent(long timestamp) {
        throw new UnsupportedOperationException();
        // MouseEvent event = new MouseEvent();
        // event.timestamp=timestamp;
        // return event;
    }

    private MouseEvent() {
    }

    public int type;
    public long timestamp;
    public float x;
    public float y;
    public int button;
}