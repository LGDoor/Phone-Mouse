package com.raphaelyu.airmouse;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;


@SuppressWarnings("serial")
public final class EventQueue extends ArrayBlockingQueue<MouseEvent> {

    public static final int QUEUE_CAPACITY = 2000;

    private static EventQueue instance = new EventQueue(QUEUE_CAPACITY);

    private EventQueue(int capacity, boolean fair, Collection<? extends MouseEvent> c) {
        super(capacity, fair, c);
    }

    private EventQueue(int capacity, boolean fair) {
        super(capacity, fair);
    }

    private EventQueue(int capacity) {
        super(capacity);
    }

    public static EventQueue getInstance() {
        return instance;
    }
}
