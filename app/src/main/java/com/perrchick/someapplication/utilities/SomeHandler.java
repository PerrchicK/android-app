package com.perrchick.someapplication.utilities;

import android.os.Handler;
import android.os.Message;

/**
 * Created by perrchick on 03/12/2016.
 */
public class SomeHandler extends Handler {
    private final SomeHandlerListener listener;

    public SomeHandler(SomeHandlerListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        listener.handlerDidGetMessage(this, msg);
    }
}
