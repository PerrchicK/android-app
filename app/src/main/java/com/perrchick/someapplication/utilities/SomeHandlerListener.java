package com.perrchick.someapplication.utilities;

import android.os.Handler;
import android.os.Message;

/**
 * Created by perrchick on 03/12/2016.
 */

public interface SomeHandlerListener {
    void handlerDidGetMessage(Handler handler, Message message);
}
